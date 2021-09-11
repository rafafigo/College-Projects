#define RANGE 10

double **create_array_pts(int n_dims, long np);

double **get_points(int argc, char *argv[], int *n_dims, long *np, int myRank, int nProcs);
