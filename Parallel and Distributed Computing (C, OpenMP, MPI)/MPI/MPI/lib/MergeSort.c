#include "MergeSort.h"
#include <string.h>

void msort(double **points, int nPoints, double **tmp) {
    if (nPoints > 1) {
        msort(points, (nPoints / 2), tmp);
        msort(points + (nPoints / 2), nPoints - (nPoints / 2), tmp);
        merge(points, nPoints, tmp);
    }
}

void merge(double **points, int nPoints, double **tmp) {
    int i = 0;
    int l = 0;
    int r = nPoints / 2;

    // Sorts the First Points in Left & Right Arrays in Tmp
    while (l < nPoints / 2 && r < nPoints) {
        tmp[i++] = points[points[l][0] < points[r][0] ? l++ : r++];
    }
    // Copies to Tmp the Remaining Points in Left Array
    while (l < nPoints / 2) tmp[i++] = points[l++];
    // Copies to Tmp the Remaining Points in Right Array
    while (r < nPoints) tmp[i++] = points[r++];
    memcpy(points, tmp, nPoints * sizeof(double *));
}
