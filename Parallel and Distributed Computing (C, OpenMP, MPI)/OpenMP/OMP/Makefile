CC=gcc
CFLAGS=-fopenmp -O3
LDFLAGS=-lm
TARGET=ballAlg-omp ballQuery

all: $(TARGET)

ballAlg-omp:
	$(CC) $(CFLAGS) $^ -o ballAlg-omp $(LDFLAGS)

ballQuery:
	$(CC) $(CFLAGS) $^ -o ballQuery $(LDFLAGS)

ballAlg-omp: ballAlg-omp.c ballAlg-omp.h lib/PointArithmetic.c lib/PointArithmetic.h lib/gen_points.c lib/gen_points.h lib/MergeSort.c lib/MergeSort.h
ballQuery: lib/ballQuery.c

clean:
	@echo Cleaning...
	rm -f $(TARGET)
