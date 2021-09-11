double distance(const double *p1, const double *p2);

double distanceSquared(const double *p1, const double *p2);

void copyPoint(const double *p, double *copyP);

void midPoint(const double *p1, const double *p2, double *midP);

void sub(const double *p1, const double *p2, double *subP);

void sum(const double *p1, const double *p2, double *sumP);

void scale(double scalar, const double *p, double *scaleP);

double innerProduct(const double *p1, const double *p2);

void project(const double *pA, const double *p, const double *diffBA, double diffBAMod, double *projP);
