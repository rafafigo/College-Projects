#include "PointArithmetic.h"
#include <math.h>

extern int nDims;

double distanceSquared(const double *p1, const double *p2) {
    double dist = 0;
    for (int i = 0; i < nDims; i++) {
        dist += (p2[i] - p1[i]) * (p2[i] - p1[i]);
    }
    return dist;
}

double distance(const double *p1, const double *p2) {
    return sqrt(distanceSquared(p1, p2));
}

void copyPoint(const double *p, double *copyP) {
    for (int i = 0; i < nDims; i++) {
        copyP[i] = p[i];
    }
}

void midPoint(const double *p1, const double *p2, double *midP) {
    for (int i = 0; i < nDims; i++) {
        midP[i] = (p1[i] + p2[i]) / 2;
    }
}

void sum(const double *p1, const double *p2, double *sumP) {
    for (int i = 0; i < nDims; i++) {
        sumP[i] = p1[i] + p2[i];
    }
}

void scale(double scalar, const double *p, double *scaleP) {
    for (int i = 0; i < nDims; i++) {
        scaleP[i] = scalar * p[i];
    }
}

double innerProduct(const double *p1, const double *p2) {
    double prod = 0;
    for (int i = 0; i < nDims; i++) {
        prod += p1[i] * p2[i];
    }
    return prod;
}

void sub(const double *p1, const double *p2, double *subP) {
    for (int i = 0; i < nDims; i++) {
        subP[i] = p1[i] - p2[i];
    }
}

void project(const double *pA, const double *p, const double *diffBA, double diffBAMod, double *projP) {
    sub(p, pA, projP);
    scale(innerProduct(projP, diffBA) / diffBAMod, diffBA, projP);
    sum(projP, pA, projP);
}
