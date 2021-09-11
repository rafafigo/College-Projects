#include <mpi.h>
#include <stdbool.h>
#define TEAMMATE_STATE_TAG 0
#define POINT_A_TAG 1
#define POINT_B_TAG 2
#define PROJECTIONS_LEN_TAG 3
#define PROJECTIONS_TAG 4
#define MEDIAN_REQUEST 5
#define MEDIAN_REPLY 6
#define RADIUS_TAG 7
#define POINTS_LEN_TAG 8
#define POINTS_TAG 9
#define INITIAL_POINT_TAG 10
#define BRANCH_ID_LEFT 11
#define BRANCH_ID_RIGHT 12
#define PRINT_TAG 13
#define WORKING 0
#define FINISHED 1
#define MY_STATE(nPts) ((nPts) == 0 ? FINISHED : WORKING)

typedef struct {
    double *center;
    double radius;
    int id;
    int idL;
    int idR;
} node;

typedef struct {
    double medX;
    int medRanks[2];
    int medIdx[2];
} medianInfo;

void dumpTree();
int *calcInitialTeammates(int myState, int *nTeammates);
int *calcNewTeammates(int myState, const int *teammatesRanks, int nTeammates, int *newNTeammates, int iParity);
int calcWorkingTeammates(int state, int *teammatesRanks, int nTeammates);
int createNode(double *center, double radius, int idL, int idR);
int buildTreeMPI(double **initialP, double **pts, int nPts, const int *teammatesRanks, int nTeammates);
int buildTreeLoop(double **initialP, double *center, double radius, double ***ptsL, double ***ptsR, int *nPtsL, int *nPtsR, const int *teammatesRanks, int nTeammates);
double *calcFurthestPoint(double **pts, int nPts, const double *pivot, const int *teammatesRanks, int nTeammates, int bcastTag);
int calcFurthestIdx(double **pts, int nPts, const double *pivot, double *dSquared);
void bcastToMyTeam(void *buf, int bufSize, const int *teammatesRanks, int nTeammates, MPI_Datatype datatype, int bcastTag);
medianInfo bcastMedianInfo(const int *teammatesRanks, int nTeammates, double *myXs, int nXs);
int teammateMinX(int nTeammates, double **teammatesXs, const int *iTeammatesXs, const int *nTeammatesXs);
medianInfo recvMedianInfo(int leaderRank, double **projs, const double *myXs, int *nXs);
void calcCenter(medianInfo medInfo, double **projs, double *center);
void filterLR(double **pts, int nPts, const double *projPts, double medX, double **ptsL, double **ptsR, int *nPtsL, int *nPtsR);
void calcRadius(double **pts, int nPts, double *center, const int *teammatesRanks, int nTeammates, double *radius);
void calcCandidateRadius(int leaderRank, double **pts, int nPts, double *center);
void exchangePoints(int teammateRank, double ***ptsToSend, double ***ptsToRecv, int *nPtsToSend, int *nPtsToRecv, bool mergeLeft);
void flat(double ***pts, int *nPts, double *flatted);
void unflat(double ***pts, int *nPts, double *flatted, int nFlatted, bool mergeLeft);

// OMP Version
int buildTreeOMP(double **pts, int nPts, int nThreads);
