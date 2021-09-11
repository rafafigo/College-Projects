CC=gcc
CFLAGS=-fopenmp -O3
LDFLAGS=-lm
TARGET=ballAlg ballQuery

all: $(TARGET)

ballAlg:
	$(CC) $(CFLAGS) $^ -o ballAlg $(LDFLAGS)

ballQuery:
	$(CC) $(CFLAGS) $^ -o ballQuery $(LDFLAGS)

ballAlg: ballAlg.c ballAlg.h lib/PointArithmetic.c lib/PointArithmetic.h lib/gen_points.c lib/gen_points.h lib/MergeSort.c lib/MergeSort.h
ballQuery: lib/ballQuery.c

clean:
	@echo Cleaning...
	rm -f $(TARGET)
