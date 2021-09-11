# Makefile - CircuitRouter-SimpleShell
# Sistemas Operativos, DEI/IST/ULisboa 2018-19

SOURCESSHELL = ../lib/commandlinereader.c CircuitRouter-SimpleShell.c
OBJSSHELL = $(SOURCESSHELL:%.c=%.o)
CC   = gcc
CFLAGS =-Wall -std=gnu99 -I../
LDFLAGS=-lm
TARGET2 = CircuitRouter-SimpleShell

all: $(TARGET2)

$(TARGET2): $(OBJSSHELL)
	$(CC) $(CFLAGS) $^ -o $(TARGET2) $(LDFLAGS)

CircuitRouter-SimpleShell.o: CircuitRouter-SimpleShell.c ../lib/commandlinereader.c ../lib/commandlinereader.h
lib/commandlinereader.o: ../lib/commandlinereader.c ../lib/commandlinereader.h

$(OBJS):
	$(CC) $(CFLAGS) -c -o $@ $<

clean:
	@echo Cleaning....
	rm -f $(OBJSSHELL)
 
