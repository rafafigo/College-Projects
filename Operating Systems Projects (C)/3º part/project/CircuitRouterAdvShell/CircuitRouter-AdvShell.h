/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * IST LEIC-T Sistemas Operativos 18/19
 * Exercicio 3 - CircuitRouter-AdvShell.c
 *
 * Authors:
 * Miguel Levezinho  - 90756
 * Rafael Figueiredo - 90770
 */

#ifndef CIRCUITROUTER_SHELL_H
#define CIRCUITROUTER_SHELL_H

#include "lib/vector.h"
#include <sys/types.h>
#include "lib/timer.h"

typedef struct {
    int pid;
    TIMER_T timeStart;
    TIMER_T timeStop;
} child_t;

#endif /* CIRCUITROUTER_SHELL_H */
