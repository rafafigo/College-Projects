# Makefile Lib - Versão 2
# IST LEIC-T Sistemas Operativos 18/19
# Miguel Levezinho, 90756
# Rafael Figueiredo, 90770

SOURCES = vector.c queue.c list.c pair.c
OBJS = $(SOURCES:%.c=%.o)
CC   = gcc
CFLAGS =-Wall -std=gnu99 -pthread

all: $(OBJS)

$(OBJS):
	$(CC) $(CFLAGS) -c -o $@ $<

vector.o: vector.c vector.h types.h utility.h
queue.o: queue.c queue.h types.h
list.o: list.c list.h types.h
pair.o: pair.c pair.h

clean:
	@echo Cleaning...
	rm -f $(OBJS)
