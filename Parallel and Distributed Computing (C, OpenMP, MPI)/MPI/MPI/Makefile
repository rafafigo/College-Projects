CC=mpicc
CFLAGS=-fopenmp -O3
LDFLAGS=-lm
TARGET=ballAlg-mpi ballQuery

all: $(TARGET)

ballAlg-mpi:
	$(CC) $(CFLAGS) $^ -o ballAlg-mpi $(LDFLAGS)

ballQuery:
	$(CC) $(CFLAGS) $^ -o ballQuery $(LDFLAGS)

ballAlg-mpi: ballAlg-mpi.c ballAlg-mpi.h lib/PointArithmetic.c lib/PointArithmetic.h lib/gen_points.c lib/gen_points.h lib/MergeSort.c lib/MergeSort.h
ballQuery: lib/ballQuery.c

clean:
	@echo Cleaning...
	rm -f $(TARGET)
