#include "ballAlg-omp.h"
#include "lib/MergeSort.h"
#include "lib/PointArithmetic.h"
#include "lib/gen_points.h"
#include <omp.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int nDims;
long nNodes = 0;

int main(int argc, char *argv[]) {
    long nPts;
    double execTime = -omp_get_wtime();

    double **pts = get_points(argc, argv, &nDims, &nPts);
    double *ptsElem = pts[0];

    node head;
#pragma omp parallel
#pragma omp single
    head = buildTree(pts, nPts, 1);

    execTime += omp_get_wtime();
    fprintf(stderr, "%.1lf\n", execTime);

    printf("%d %ld\n", nDims, nNodes);
    dumpTree(head);
    free(ptsElem);
    exit(EXIT_SUCCESS);
}

int dumpTree(node head) {
    static long i = 0;
    if (head == NULL) return -1;
    long id = i++;
    long L = dumpTree(head->L);
    long R = dumpTree(head->R);
    printf("%ld %ld %ld %.6lf", id, L, R, head->radius);
    for (long j = 0; j < nDims; j++) {
        printf(" %.6lf", head->center[j]);
    }
    printf("\n");
    free(head->center);
    free(head);
    return id;
}

node createNode(double *center, double radius, node L, node R) {
#pragma omp atomic
    nNodes++;
    node new = (node) malloc(sizeof(struct node));
    new->center = center;
    new->radius = radius;
    new->L = L;
    new->R = R;
    return new;
}

node buildTree(double **pts, long nPts, int nDepth) {
    if (nPts < 1) {
        free(pts);
        return NULL;
    }
    double *center = (double *) malloc(sizeof(double) * nDims);
    if (nPts == 1) {
        copyPoint(pts[0], center);
        free(pts);
        return createNode(center, 0, NULL, NULL);
    }
    long pAIdx = furthest(pts, nPts, pts);
    long pBIdx = furthest(pts, nPts, pts + pAIdx);
    double *diffBA = (double *) malloc(sizeof(double) * nDims);
    sub(pts[pBIdx], pts[pAIdx], diffBA);
    double diffBAMod = innerProduct(diffBA, diffBA);
    double *projPts = (double *) malloc(nDims * nPts * sizeof(double));
    double **projs = (double **) malloc(sizeof(double *) * nPts);

    for (long i = 0; i < nPts; i++) {
        projs[i] = &projPts[i * nDims];
        if (i == pAIdx || i == pBIdx) {
            copyPoint(pts[i], projs[i]);
        } else {
            project(pts[pAIdx], pts[i], diffBA, diffBAMod, projs[i]);
        }
    }

    double **tmp = (double **) malloc(sizeof(double *) * nPts);
    msort(projs, nPts, tmp);

    if (nPts % 2 == 0) {
        midPoint(projs[nPts / 2 - 1], projs[nPts / 2], center);
    } else {
        copyPoint(projs[nPts / 2], center);
    }

    double **ptsL = projs;
    double **ptsR = tmp;
    long nPtsL = 0;
    long nPtsR = 0;
    filterLR(pts, nPts, projPts, center, ptsL, ptsR, &nPtsL, &nPtsR);
    double radius = distance(center, pts[furthest(pts, nPts, &center)]);

    free(pts);
    free(projPts);
    free(diffBA);

    int myDepth = nDepth * 2;
    node L, R;
#pragma omp task shared(L) if (nDepth < omp_get_num_threads())
    L = buildTree(ptsL, nPtsL, myDepth);
    R = buildTree(ptsR, nPtsR, myDepth);
#pragma omp taskwait
    return createNode(center, radius, L, R);
}

void filterLR(double **pts, long nPts, const double *projPts, const double *center, double **ptsL, double **ptsR, long *nPtsL, long *nPtsR) {
    for (long i = 0; i < nPts; i++) {
        if ((projPts + (i * nDims))[0] < center[0]) {
            ptsL[(*nPtsL)++] = pts[i];
        } else {
            ptsR[(*nPtsR)++] = pts[i];
        }
    }
}

long furthest(double **pts, long nPts, double **pivotPtr) {
    double maxDist = -1;
    long pIdx = 0;
    for (long i = 0; i < nPts; i++) {
        if (pts[i] == *pivotPtr) continue;
        double dist = distanceSquared(*pivotPtr, pts[i]);
        if (maxDist < dist) {
            pIdx = i;
            maxDist = dist;
        }
    }
    return pIdx;
}
