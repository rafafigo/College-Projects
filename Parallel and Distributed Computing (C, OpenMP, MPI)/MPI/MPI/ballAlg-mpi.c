#include "ballAlg-mpi.h"
#include "lib/MergeSort.h"
#include "lib/PointArithmetic.h"
#include "lib/gen_points.h"
#include <mpi.h>
#include <omp.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>

int nProcs;
int myRank;
int nid;
int nDims;
node *nodes;
int nodesCapacity;
int nNodes = 0;
MPI_Datatype mpiMedianInfo;

int main(int argc, char *argv[]) {

    double execTime = -omp_get_wtime();
    MPI_Init(&argc, &argv);

    medianInfo dummyMedianInfo;
    int lengths[3] = {1, 2, 2};
    MPI_Aint displacements[3];
    MPI_Aint baseAddress;
    MPI_Get_address(&dummyMedianInfo, &baseAddress);
    MPI_Get_address(&dummyMedianInfo.medX, &displacements[0]);
    MPI_Get_address(dummyMedianInfo.medRanks, &displacements[1]);
    MPI_Get_address(dummyMedianInfo.medIdx, &displacements[2]);
    displacements[0] = MPI_Aint_diff(displacements[0], baseAddress);
    displacements[1] = MPI_Aint_diff(displacements[1], baseAddress);
    displacements[2] = MPI_Aint_diff(displacements[2], baseAddress);
    MPI_Datatype datatypes[3] = {MPI_DOUBLE, MPI_INT, MPI_INT};
    MPI_Type_create_struct(3, lengths, displacements, datatypes, &mpiMedianInfo);
    MPI_Type_commit(&mpiMedianInfo);

    MPI_Comm_size(MPI_COMM_WORLD, &nProcs);
    MPI_Comm_rank(MPI_COMM_WORLD, &myRank);
    nid = myRank + 1;

    int nPts;
    double **pts = get_points(argc, argv, &nDims, (long *) &nPts, myRank, nProcs);
    double *initialP = pts[nPts];
    nodesCapacity = nPts;
    nodes = (node *) malloc(sizeof(node) * nodesCapacity);

    int nTeammates = 1;
    int *teammatesRanks = nProcs > 1 ? calcInitialTeammates(MY_STATE(nPts), &nTeammates) : NULL;

#pragma omp parallel
#pragma omp single
    {
        nTeammates == 1
                ? buildTreeOMP(pts, nPts, omp_get_num_threads())
                : buildTreeMPI(&initialP, pts, nPts, teammatesRanks, nTeammates);
    }
    if (myRank == 0) {
        if (nNodes > 0) nodes[nNodes - 1].id = 0;
        execTime += omp_get_wtime();
        fprintf(stderr, "%.1lf\n", execTime);
        fflush(stderr);
    }

    int nNodesGlobal = nNodes;
    if (nProcs > 1) MPI_Reduce(&nNodes, &nNodesGlobal, 1, MPI_INT, MPI_SUM, 0, MPI_COMM_WORLD);
    if (myRank == 0) {
        printf("%d %d\n", nDims, nNodesGlobal);
        fflush(stdout);
    }
    dumpTree();

    if (nProcs > 1) free(teammatesRanks);
    free((*pts));
    free(pts);
    MPI_Finalize();
    exit(EXIT_SUCCESS);
}

void dumpTree() {

    if (myRank != 0) MPI_Recv(NULL, 0, MPI_INT, myRank - 1, PRINT_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

    for (int i = 0; i < nNodes; i++) {
        printf("%d %d %d %.6lf", nodes[i].id, nodes[i].idL, nodes[i].idR, nodes[i].radius);
        for (int j = 0; j < nDims; j++) printf(" %.6lf", nodes[i].center[j]);
        printf("\n");
        free(nodes[i].center);
    }
    fflush(stdout);
    free(nodes);

    if (myRank != nProcs - 1) MPI_Send(NULL, 0, MPI_INT, myRank + 1, PRINT_TAG, MPI_COMM_WORLD);
}

int createNode(double *center, double radius, int idL, int idR) {

    int myNid;
    int myNNodes;
#pragma omp critical(createNode)
    {
        myNid = nid;
        myNNodes = nNodes;

        nid += nProcs;
        if (nNodes++ >= nodesCapacity) {
            nodesCapacity *= 2;
            nodes = (node *) realloc(nodes, sizeof(node) * nodesCapacity);
        }
    }
    node *new = &nodes[myNNodes];
    new->id = myNid;
    new->center = center;
    new->radius = radius;
    new->idL = idL;
    new->idR = idR;
    return myNid;
}

int buildTreeMPI(double **initialP, double **pts, int nPts, const int *teammatesRanks, int nTeammates) {

    if (MY_STATE(nPts) == FINISHED) return -1;

    double *center = (double *) malloc(sizeof(double) * nDims);
    if (nPts == 1 && nTeammates == 1) {
        copyPoint(pts[0], center);
        return createNode(center, 0, -1, -1);
    }

    double *pA = calcFurthestPoint(pts, nPts, *initialP, teammatesRanks, nTeammates, POINT_A_TAG);
    double *pB = calcFurthestPoint(pts, nPts, pA, teammatesRanks, nTeammates, POINT_B_TAG);
    double *myXs = (double *) malloc(sizeof(double) * nPts);
    double *diffBA = (double *) malloc(sizeof(double) * nDims);
    double *projPts = (double *) malloc(sizeof(double) * nDims * nPts);
    double **projs = (double **) malloc(sizeof(double *) * nPts);
    double **tmp = (double **) malloc(sizeof(double *) * nPts);
    double **ptsL = (double **) malloc(sizeof(double *) * nPts);
    double **ptsR = (double **) malloc(sizeof(double *) * nPts);

    sub(pB, pA, diffBA);
    double diffBAMod = innerProduct(diffBA, diffBA);

    for (int i = 0; i < nPts; i++) {
        projs[i] = &projPts[i * nDims];
        project(pA, pts[i], diffBA, diffBAMod, projs[i]);
    }

    msort(projs, nPts, tmp);
    for (int i = 0; i < nPts; i++) myXs[i] = projs[i][0];

    medianInfo medInfo;
    if (teammatesRanks[0] == myRank) {
        medInfo = bcastMedianInfo(teammatesRanks, nTeammates, myXs, nPts);
    } else {
        medInfo = recvMedianInfo(teammatesRanks[0], projs, myXs, &nPts);
    }

    int nPtsL = 0;
    int nPtsR = 0;
    filterLR(pts, nPts, projPts, medInfo.medX, ptsL, ptsR, &nPtsL, &nPtsR);
    ptsL = realloc(ptsL, sizeof(double *) * nPtsL);
    ptsR = realloc(ptsR, sizeof(double *) * nPtsR);

    double radius = 0;
    if (teammatesRanks[0] == myRank) {
        calcCenter(medInfo, projs, center);
        calcRadius(pts, nPts, center, teammatesRanks, nTeammates, &radius);
    } else {
        calcCandidateRadius(teammatesRanks[0], pts, nPts, center);
    }

    free(pA);
    free(pB);
    free(myXs);
    free(diffBA);
    free(projPts);
    free(projs);
    free(tmp);

    int myNid = buildTreeLoop(initialP, center, radius, &ptsL, &ptsR, &nPtsL, &nPtsR, teammatesRanks, nTeammates);

    free(ptsL);
    free(ptsR);
    return myNid;
}

int buildTreeLoop(double **initialP, double *center, double radius, double ***ptsL, double ***ptsR, int *nPtsL, int *nPtsR, const int *teammatesRanks, int nTeammates) {

    int idL = -1, idR = -1;
    if (nTeammates == 1) {
        int nThreads = omp_get_num_threads();
#pragma omp task shared(idL)
        idL = buildTreeOMP((*ptsL), (*nPtsL), nThreads / 2);
        idR = buildTreeOMP((*ptsR), (*nPtsR), nThreads - nThreads / 2);
#pragma omp taskwait
        return createNode(center, radius, idL, idR);
    }

    int teammateId = 0;
    for (int i = 0; i < nTeammates; i++) {
        if (teammatesRanks[i] == myRank) {
            teammateId = i;
            break;
        }
    }
    if (teammateId % 2 == 0) {
        if (teammateId == nTeammates - 1) {
            exchangePoints(teammatesRanks[teammateId - 1], ptsR, NULL, nPtsR, NULL, false);
        } else {
            exchangePoints(teammatesRanks[teammateId + 1], ptsR, ptsL, nPtsR, nPtsL, false);
        }
    } else {
        exchangePoints(teammatesRanks[teammateId - 1], ptsL, ptsR, nPtsL, nPtsR, true);
        if (teammateId == nTeammates - 2) {
            exchangePoints(teammatesRanks[teammateId + 1], NULL, ptsR, NULL, nPtsR, false);
        }
    }

    int myState = MY_STATE(teammateId % 2 == 0 ? (*nPtsL) : (*nPtsR));
    bcastToMyTeam(&myState, 1, teammatesRanks, nTeammates, MPI_INT, TEAMMATE_STATE_TAG);
    if (myState == FINISHED && teammatesRanks[0] != myRank) return -1;

    int newNTeammates[2];
    int *newTeammatesRanks[2];
    for (int i = 0; i < 2; i++) {
        newTeammatesRanks[i] = calcNewTeammates(myState, teammatesRanks, nTeammates, &newNTeammates[i], i);
    }

    if (myState == FINISHED && teammatesRanks[0] == myRank) {
        if (newNTeammates[0] > 0) MPI_Recv(&idL, 1, MPI_INT, newTeammatesRanks[0][0], BRANCH_ID_LEFT, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        if (newNTeammates[1] > 0) MPI_Recv(&idR, 1, MPI_INT, newTeammatesRanks[1][0], BRANCH_ID_RIGHT, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

        for (int i = 0; i < 2; i++) free(newTeammatesRanks[i]);
        return createNode(center, radius, idL, idR);
    }

    if (newTeammatesRanks[teammateId % 2][0] == myRank) {
        *initialP = teammateId % 2 == 0 ? (*ptsL)[0] : (*ptsR)[0];
        bcastToMyTeam(*initialP, nDims, newTeammatesRanks[teammateId % 2], newNTeammates[teammateId % 2], MPI_DOUBLE, INITIAL_POINT_TAG);
    } else {
        MPI_Recv(*initialP, nDims, MPI_DOUBLE, newTeammatesRanks[teammateId % 2][0], INITIAL_POINT_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
    }

    int myNid;
    if (teammateId % 2 == 0) {
        idL = buildTreeMPI(initialP, (*ptsL), (*nPtsL), newTeammatesRanks[0], newNTeammates[0]);
        myNid = idL;
        if (newTeammatesRanks[0][0] == myRank) {
            if (teammatesRanks[0] == myRank) {
                if (newNTeammates[1] > 0) {
                    MPI_Recv(&idR, 1, MPI_INT, newTeammatesRanks[1][0], BRANCH_ID_RIGHT, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
                }
                myNid = createNode(center, radius, idL, idR);
            } else {
                MPI_Request request;
                MPI_Isend(&idL, 1, MPI_INT, teammatesRanks[0], BRANCH_ID_LEFT, MPI_COMM_WORLD, &request);
                MPI_Request_free(&request);
            }
        }
    } else {
        idR = buildTreeMPI(initialP, (*ptsR), (*nPtsR), newTeammatesRanks[1], newNTeammates[1]);
        myNid = idR;
        if (newTeammatesRanks[1][0] == myRank) {
            MPI_Request request;
            MPI_Isend(&idR, 1, MPI_INT, teammatesRanks[0], BRANCH_ID_RIGHT, MPI_COMM_WORLD, &request);
            MPI_Request_free(&request);
        }
    }

    for (int i = 0; i < 2; i++) free(newTeammatesRanks[i]);
    return myNid;
}

int *calcInitialTeammates(int myState, int *nTeammates) {

    *nTeammates = nProcs;
    int *teammatesRanks = malloc(sizeof(int) * (*nTeammates));
    for (int i = 0; i < (*nTeammates); i++) teammatesRanks[i] = i;

    bcastToMyTeam(&myState, 1, teammatesRanks, (*nTeammates), MPI_INT, TEAMMATE_STATE_TAG);
    *nTeammates = calcWorkingTeammates(myState, teammatesRanks, (*nTeammates));
    teammatesRanks = (int *) realloc(teammatesRanks, sizeof(int) * (*nTeammates));
    return teammatesRanks;
}

int *calcNewTeammates(int myState, const int *teammatesRanks, int nTeammates, int *newNTeammates, int iParity) {

    int *newTeammatesRanks = (int *) malloc(sizeof(int) * nTeammates);
    *newNTeammates = 0;
    for (int i = 0; i < nTeammates; i++) {
        if (i % 2 == iParity) newTeammatesRanks[(*newNTeammates)++] = teammatesRanks[i];
    }
    *newNTeammates = calcWorkingTeammates(myState, newTeammatesRanks, (*newNTeammates));
    newTeammatesRanks = (int *) realloc(newTeammatesRanks, sizeof(int) * (*newNTeammates));
    return newTeammatesRanks;
}

int calcWorkingTeammates(int state, int *teammatesRanks, int nTeammates) {

    int teammateState;
    int nTeammatesWorking = 0;
    for (int i = 0; i < nTeammates; i++) {
        if (teammatesRanks[i] != myRank) {
            MPI_Recv(&teammateState, 1, MPI_INT, teammatesRanks[i], TEAMMATE_STATE_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        } else {
            teammateState = state;
        }
        if (teammateState == WORKING) teammatesRanks[nTeammatesWorking++] = teammatesRanks[i];
    }
    return nTeammatesWorking;
}

double *calcFurthestPoint(double **pts, int nPts, const double *pivot, const int *teammatesRanks, int nTeammates, int bcastTag) {

    double *P = (double *) malloc(sizeof(double) * nDims);
    double *pCmp = (double *) malloc(sizeof(double) * nDims);

    double dSquared;
    int pIdx = calcFurthestIdx(pts, nPts, pivot, &dSquared);
    copyPoint(pts[pIdx], P);
    bcastToMyTeam(P, nDims, teammatesRanks, nTeammates, MPI_DOUBLE, bcastTag);

    for (int i = 0; i < nTeammates; i++) {
        if (teammatesRanks[i] != myRank) {
            MPI_Recv(pCmp, nDims, MPI_DOUBLE, teammatesRanks[i], bcastTag, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            double dSquaredCmp = distanceSquared(pivot, pCmp);
            if (dSquaredCmp > dSquared) {
                dSquared = dSquaredCmp;
                copyPoint(pCmp, P);
            }
        }
    }

    free(pCmp);
    return P;
}

void bcastToMyTeam(void *buf, int bufSize, const int *teammatesRanks, int nTeammates, MPI_Datatype datatype, int bcastTag) {

    MPI_Request request;
    for (int i = 0; i < nTeammates; i++) {
        if (teammatesRanks[i] != myRank) {
            MPI_Isend(buf, bufSize, datatype, teammatesRanks[i], bcastTag, MPI_COMM_WORLD, &request);
            MPI_Request_free(&request);
        }
    }
}

int calcFurthestIdx(double **pts, int nPts, const double *pivot, double *dSquared) {

    *dSquared = -1;
    int pIdx = 0;
    for (int i = 0; i < nPts; i++) {
        if (pts[i] == pivot) continue;
        double dSquaredCmp = distanceSquared(pivot, pts[i]);
        if ((*dSquared) < dSquaredCmp) {
            pIdx = i;
            *dSquared = dSquaredCmp;
        }
    }
    return pIdx;
}

medianInfo bcastMedianInfo(const int *teammatesRanks, int nTeammates, double *myXs, int nXs) {

    int nTeammatesXsSum = 0;
    int *nTeammatesXs = (int *) calloc(nTeammates, sizeof(int));
    int *iTeammatesXs = (int *) calloc(nTeammates, sizeof(int));
    double **teammatesXs = (double **) malloc(sizeof(double *) * nTeammates);

    teammatesXs[0] = myXs;
    nTeammatesXs[0] = nXs;
    nTeammatesXsSum += nTeammatesXs[0];
    for (int i = 1; i < nTeammates; i++) {
        MPI_Recv(&nTeammatesXs[i], 1, MPI_INT, teammatesRanks[i], PROJECTIONS_LEN_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        teammatesXs[i] = (double *) malloc(sizeof(double) * nTeammatesXs[i]);
        MPI_Recv(teammatesXs[i], nTeammatesXs[i], MPI_DOUBLE, teammatesRanks[i], PROJECTIONS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        nTeammatesXsSum += nTeammatesXs[i];
    }
    for (int i = 0; i < nTeammatesXsSum / 2 - 1; i++) {
        iTeammatesXs[teammateMinX(nTeammates, teammatesXs, iTeammatesXs, nTeammatesXs)]++;
    }

    medianInfo medInfo;
    medInfo.medX = 0;
    for (int i = 0; i < 2; i++) {
        int teammateId = teammateMinX(nTeammates, teammatesXs, iTeammatesXs, nTeammatesXs);
        if (i == 1 || nTeammatesXsSum % 2 == 0) {
            medInfo.medX += teammatesXs[teammateId][iTeammatesXs[teammateId]];
        }
        medInfo.medRanks[i] = teammatesRanks[teammateId];
        medInfo.medIdx[i] = iTeammatesXs[teammateId]++;
    }
    if (nTeammatesXsSum % 2 == 0) medInfo.medX /= 2;
    else {
        medInfo.medRanks[0] = -1;
        medInfo.medIdx[0] = -1;
    }
    bcastToMyTeam(&medInfo, 1, teammatesRanks, nTeammates, mpiMedianInfo, MEDIAN_REQUEST);

    for (int i = 1; i < nTeammates; i++) free(teammatesXs[i]);
    free(nTeammatesXs);
    free(iTeammatesXs);
    free(teammatesXs);
    return medInfo;
}

int teammateMinX(int nTeammates, double **teammatesXs, const int *iTeammatesXs, const int *nTeammatesXs) {

    int iMinX = -1;
    for (int i = 0; i < nTeammates; i++) {
        if (iTeammatesXs[i] < nTeammatesXs[i] && (iMinX < 0 || teammatesXs[i][iTeammatesXs[i]] < teammatesXs[iMinX][iTeammatesXs[iMinX]])) {
            iMinX = i;
        }
    }
    return iMinX;
}

medianInfo recvMedianInfo(int leaderRank, double **projs, const double *myXs, int *nXs) {

    MPI_Request request;
    MPI_Isend(nXs, 1, MPI_INT, leaderRank, PROJECTIONS_LEN_TAG, MPI_COMM_WORLD, &request);
    MPI_Request_free(&request);
    MPI_Isend(myXs, (*nXs), MPI_DOUBLE, leaderRank, PROJECTIONS_TAG, MPI_COMM_WORLD, &request);
    MPI_Request_free(&request);

    medianInfo medInfo;
    MPI_Recv(&medInfo, 1, mpiMedianInfo, leaderRank, MEDIAN_REQUEST, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

    int i = 0;
    double *med = (double *) malloc(sizeof(double) * nDims * 2);
    for (int j = 0; j < 2; j++) {
        if (medInfo.medRanks[j] == myRank) copyPoint(projs[medInfo.medIdx[j]], &med[nDims * i++]);
    }
    if (i > 0) {
        MPI_Isend(med, nDims * i, MPI_DOUBLE, leaderRank, MEDIAN_REPLY, MPI_COMM_WORLD, &request);
        MPI_Request_free(&request);
    }
    return medInfo;
}

void filterLR(double **pts, int nPts, const double *projPts, double medX, double **ptsL, double **ptsR, int *nPtsL, int *nPtsR) {

    bool seenEqual = false;
    for (int i = 0; i < nPts; i++) {
        if (projPts[i * nDims] < medX) {
            ptsL[(*nPtsL)++] = pts[i];
        } else if (projPts[i * nDims] == medX) {
            if (!seenEqual) {
                seenEqual = true;
                ptsR[(*nPtsR)++] = pts[i];
            } else {
                ptsL[(*nPtsL)++] = pts[i];
            }
        } else {
            ptsR[(*nPtsR)++] = pts[i];
        }
    }
}

void calcCenter(medianInfo medInfo, double **projs, double *center) {

    double *med = (double *) malloc(sizeof(double) * nDims * 2);

    if (medInfo.medRanks[0] < 0) {
        if (medInfo.medRanks[1] == myRank) {
            copyPoint(projs[medInfo.medIdx[1]], center);
        } else {
            MPI_Recv(med, nDims, MPI_DOUBLE, medInfo.medRanks[1], MEDIAN_REPLY, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            copyPoint(med, center);
        }
    } else if (medInfo.medRanks[0] == medInfo.medRanks[1]) {
        if (medInfo.medRanks[0] == myRank) {
            midPoint(projs[medInfo.medIdx[0]], projs[medInfo.medIdx[1]], center);
        } else {
            MPI_Recv(med, nDims * 2, MPI_DOUBLE, medInfo.medRanks[0], MEDIAN_REPLY, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            midPoint(med, &med[nDims], center);
        }
    } else {
        for (int i = 0; i < 2; i++) {
            if (medInfo.medRanks[i] == myRank) {
                copyPoint(projs[medInfo.medIdx[i]], &med[i * nDims]);
            } else {
                MPI_Recv(&med[i * nDims], nDims, MPI_DOUBLE, medInfo.medRanks[i], MEDIAN_REPLY, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            }
        }
        midPoint(med, &med[nDims], center);
    }

    free(med);
}

void calcRadius(double **pts, int nPts, double *center, const int *teammatesRanks, int nTeammates, double *radius) {

    bcastToMyTeam(center, nDims, teammatesRanks, nTeammates, MPI_DOUBLE, RADIUS_TAG);

    double dSquared;
    int pIdx = calcFurthestIdx(pts, nPts, center, &dSquared);
    *radius = distance(center, pts[pIdx]);

    double candidateRadius;
    for (int i = 0; i < nTeammates; i++) {
        if (teammatesRanks[i] != myRank) {
            MPI_Recv(&candidateRadius, 1, MPI_DOUBLE, teammatesRanks[i], RADIUS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
            if (candidateRadius > (*radius)) *radius = candidateRadius;
        }
    }
}

void calcCandidateRadius(int leaderRank, double **pts, int nPts, double *center) {

    MPI_Recv(center, nDims, MPI_DOUBLE, leaderRank, RADIUS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);

    double dSquared;
    int pIdx = calcFurthestIdx(pts, nPts, center, &dSquared);
    double candidateRadius = distance(center, pts[pIdx]);

    MPI_Request request;
    MPI_Isend(&candidateRadius, 1, MPI_DOUBLE, leaderRank, RADIUS_TAG, MPI_COMM_WORLD, &request);
    MPI_Request_free(&request);
}

void exchangePoints(int teammateRank, double ***ptsToSend, double ***ptsToRecv, int *nPtsToSend, int *nPtsToRecv, bool mergeLeft) {

    if (nPtsToSend != NULL) {
        MPI_Request request;
        int nFlattedToSend = (*nPtsToSend) * nDims;
        MPI_Isend(&nFlattedToSend, 1, MPI_INT, teammateRank, POINTS_LEN_TAG, MPI_COMM_WORLD, &request);
        MPI_Request_free(&request);
        double *flattedToSend = (double *) malloc(sizeof(double) * nFlattedToSend);
        flat(ptsToSend, nPtsToSend, flattedToSend);
        MPI_Isend(flattedToSend, nFlattedToSend, MPI_DOUBLE, teammateRank, POINTS_TAG, MPI_COMM_WORLD, &request);
        MPI_Request_free(&request);
    }

    if (nPtsToRecv != NULL) {
        int nFlattedToRecv;
        MPI_Recv(&nFlattedToRecv, 1, MPI_INT, teammateRank, POINTS_LEN_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        double *flattedToRecv = (double *) malloc(sizeof(double) * nFlattedToRecv);
        MPI_Recv(flattedToRecv, nFlattedToRecv, MPI_DOUBLE, teammateRank, POINTS_TAG, MPI_COMM_WORLD, MPI_STATUS_IGNORE);
        unflat(ptsToRecv, nPtsToRecv, flattedToRecv, nFlattedToRecv, mergeLeft);
    }
}

void flat(double ***pts, int *nPts, double *flatted) {

    for (int i = 0; i < (*nPts); i++) copyPoint((*pts)[i], &flatted[i * nDims]);
}

void unflat(double ***pts, int *nPts, double *flatted, int nFlatted, bool mergeLeft) {

    *pts = (double **) realloc((*pts), ((*nPts) + nFlatted / nDims) * sizeof(double *));
    if (mergeLeft) {
        for (int i = (*nPts) - 1; i >= 0; i--) (*pts)[i + nFlatted / nDims] = (*pts)[i];
        for (int i = 0; i < nFlatted / nDims; i++) (*pts)[i] = &flatted[i * nDims];
    } else {
        for (int i = 0; i < nFlatted / nDims; i++) (*pts)[(*nPts) + i] = &flatted[i * nDims];
    }
    *nPts += nFlatted / nDims;
}

// OMP Version
int buildTreeOMP(double **pts, int nPts, int nThreads) {

    if (nPts < 1) return -1;

    double *center = (double *) malloc(sizeof(double) * nDims);
    if (nPts == 1) {
        copyPoint(pts[0], center);
        return createNode(center, 0, -1, -1);
    }

    double dSquared;
    int pAIdx = calcFurthestIdx(pts, nPts, pts[0], &dSquared);
    int pBIdx = calcFurthestIdx(pts, nPts, pts[pAIdx], &dSquared);
    double *diffBA = (double *) malloc(sizeof(double) * nDims);
    double *projPts = (double *) malloc(nDims * nPts * sizeof(double));
    double **projs = (double **) malloc(sizeof(double *) * nPts);
    double **tmp = (double **) malloc(sizeof(double *) * nPts);

    sub(pts[pBIdx], pts[pAIdx], diffBA);
    double diffBAMod = innerProduct(diffBA, diffBA);

    for (int i = 0; i < nPts; i++) {
        projs[i] = &projPts[i * nDims];
        if (i == pAIdx || i == pBIdx) {
            copyPoint(pts[i], projs[i]);
        } else {
            project(pts[pAIdx], pts[i], diffBA, diffBAMod, projs[i]);
        }
    }

    msort(projs, nPts, tmp);

    if (nPts % 2 == 0) {
        midPoint(projs[nPts / 2 - 1], projs[nPts / 2], center);
    } else {
        copyPoint(projs[nPts / 2], center);
    }

    double **ptsL = projs;
    double **ptsR = tmp;
    int nPtsL = 0;
    int nPtsR = 0;
    filterLR(pts, nPts, projPts, center[0], ptsL, ptsR, &nPtsL, &nPtsR);
    double radius = distance(center, pts[calcFurthestIdx(pts, nPts, center, &dSquared)]);

    free(diffBA);
    free(projPts);

    int idL, idR;
#pragma omp task shared(idL) if (nThreads > 1)
    idL = buildTreeOMP(ptsL, nPtsL, nThreads / 2);
    idR = buildTreeOMP(ptsR, nPtsR, nThreads - nThreads / 2);
#pragma omp taskwait

    free(projs);
    free(tmp);
    return createNode(center, radius, idL, idR);
}
