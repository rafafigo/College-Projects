# Makefile SeqSolver, versao 2
# Sistemas Operativos, DEI/IST/ULisboa 2018-19
# Miguel Levezinho, 90756
# Rafael Figueiredo, 90770

SOURCES1 = router.c maze.c grid.c coordinate.c CircuitRouter-SeqSolver.c
OBJS2 = $(SOURCES1:%.c=%.o)
SOURCES+= $(SOURCES1) ../lib/vector.c ../lib/queue.c ../lib/list.c ../lib/pair.c
OBJS = $(SOURCES:%.c=%.o)
CC   = gcc
CFLAGS =-Wall -std=gnu99 -I../
LDFLAGS=-lm
TARGET = CircuitRouter-SeqSolver

all: $(TARGET)

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) $^ -o $(TARGET) $(LDFLAGS) 

CircuitRouter-SeqSolver.o: CircuitRouter-SeqSolver.c maze.h router.h ../lib/list.h ../lib/timer.h ../lib/types.h
router.o: router.c router.h coordinate.h grid.h ../lib/queue.h ../lib/vector.h
maze.o: maze.c maze.h coordinate.h grid.h ../lib/list.h ../lib/queue.h ../lib/pair.h ../lib/types.h ../lib/vector.h
grid.o: grid.c grid.h coordinate.h ../lib/types.h ../lib/vector.h
coordinate.o: coordinate.c coordinate.h ../lib/pair.h ../lib/types.h

$(OBJS2):
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	@echo Cleaning...
	rm -f $(OBJS2)


