typedef struct node *node;

struct node {
    double *center;
    double radius;
    node L;
    node R;
};

long dumpTree(node head);

node createNode(double *center, double radius, node L, node R);

node buildTree(double **pts, long nPts);

void filterLR(double **pts, long nPts, const double *projPts, const double *center, double **ptsL, double **ptsR, long *nPtsL, long *nPtsR);

long furthest(double **pts, long nPts, double **pivotPtr);
