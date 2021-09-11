#include "gen_points.h"
#include <stdio.h>
#include <stdlib.h>

double **create_array_pts(int n_dims, long np) {
    double *_p_arr;
    double **p_arr;

    _p_arr = (double *) malloc(n_dims * np * sizeof(double));
    p_arr = (double **) malloc(np * sizeof(double *));
    if ((_p_arr == NULL) || (p_arr == NULL)) {
        printf("Error allocating array of points, exiting.\n");
        exit(4);
    }

    for (long i = 0; i < np; i++)
        p_arr[i] = &_p_arr[i * n_dims];

    return p_arr;
}


double **get_points(int argc, char *argv[], int *n_dims, long *np, int myRank, int nProcs) {
    double **pt_arr;
    unsigned seed;
    long i;
    int j;

    if (argc != 4) {
        printf("Usage: %s <n_dims> <n_points> <seed>\n", argv[0]);
        exit(1);
    }

    *n_dims = atoi(argv[1]);
    if (*n_dims < 2) {
        printf("Illegal number of dimensions (%d), must be above 1.\n", *n_dims);
        exit(2);
    }

    long nPtsGlobal = atol(argv[2]);
    if (nPtsGlobal < 1) {
        printf("Illegal number of points (%ld), must be above 0.\n", *np);
        exit(3);
    }

    seed = atoi(argv[3]);
    srandom(seed);

    int iDiv = nPtsGlobal / nProcs;
    int iMod = nPtsGlobal % nProcs;
    int padding = iDiv * myRank + (myRank < iMod ? myRank : iMod);
    *np = iDiv + (myRank < iMod ? 1 : 0);

    pt_arr = (double **) create_array_pts(*n_dims, *np + 1);

    for (j = 0; j < *n_dims; j++) {
        pt_arr[0][j] = pt_arr[*np][j] = RANGE * ((double) random()) / RAND_MAX;
    }

    for (i = 1; i < padding; i++) {
        for (j = 0; j < *n_dims; j++) random();
    }

    for (i = (myRank == 0 ? 1 : 0); i < *np; i++) {
        for (j = 0; j < *n_dims; j++) {
            pt_arr[i][j] = RANGE * ((double) random()) / RAND_MAX;
        }
    }

#ifdef DEBUG
    for (i = 0; i < *np; i++) {
        for (j = 0; j < *n_dims; j++) printf(" %.6lf", pt_arr[i][j]);
        printf("\n");
    }
#endif

    return pt_arr;
}
