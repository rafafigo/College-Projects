/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * IST LEIC-T Sistemas Operativos 18/19
 * Exercicio 1 - CircuitRouter-SimpleShell.c
 *
 * Authors:
 * Miguel Levezinho  - 90756
 * Rafael Figueiredo - 90770
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/wait.h>
#include <unistd.h>
#include <errno.h>
#include "lib/commandlinereader.h"

#define VECTOR_SIZE 4
#define BUFFER_SIZE 100
#define DEFAULT_PID_STORAGE_SIZE 20
#define EXIT_CMD "exit"
#define RUN_CMD "run"
#define EXECUTABLE "CircuitRouter-SeqSolver"

/* Auxiliar function that waits for child processes to finish and stores their PID and
 * exit status in vector pids. 
 * Exit status is encypted in the PID. If the program exited normally (exit(0)), PID is stored
 * positive. Otherwise (exit(1) or forced termination), PID is stored negative.
 * If the wait() function returns -1 due to a signal interruption (errno is set to EINTR), the loop
 * reiterates to account for the possible remaining process(es)
 *
 * pids     - Vector that stores PIDs and exit success
 * numPids  - Pointer to the number of already saved PIDs in vector pids
 * numWaits - Specifies the number of processes to wait for. If negative, waits for all processes
 */
static void waitProcesses(int* pids, long* numPids, long numWaits) {

	int pid;
	int status;

	while (numWaits && ((pid = wait(&status)) != -1 || errno == EINTR))
	{
		if (pid == -1)
			continue;
		
		numWaits--;

		if (WIFEXITED(status) && !WEXITSTATUS(status))
			pids[(*numPids)++] = pid;
		else
			pids[(*numPids)++] = pid * -1;
	}
}

/* Auxiliary function that prints the results of all the process runnings.
 *
 * pids    - Vector that stores PIDs and exit success
 * numPids - Number of saved PIDs in pids
 */
static void printProcesses(int* pids, long numPids) {

	int i;

	for (i = 0; i < numPids; i++)
	{
		if (pids[i] < 0)
			printf("CHILD EXITED (PID=%d; return NOK)\n", pids[i] * -1);
		else
			printf("CHILD EXITED (PID=%d; return OK)\n", pids[i]);
	}
	puts("END.");
}

int main(int argc, char** argv)
{  
	int* pids;     							   // Vector that stores PIDs and exit success
	long pids_size = DEFAULT_PID_STORAGE_SIZE; // Size of vector pids

	long createdPids = 0; // Number of created processes
	long savedPids = 0;	  // Number of terminated processes

	int pid = 1; // Process ID for running specific code;

	char buffer[BUFFER_SIZE];	  // Stores a cmd line read from SimpleShell
	char* argVector[VECTOR_SIZE]; // Stores the cmd line separeted in string tokens
	int numTokens;				  // Number of string tokens in argVector

	long maxChildren = -1; // Max number of simultaneous processes running. Default -1 means unlimited

	// Checks if SimpleShell received a max value for simultaneous process count
	if (argc > 2 || (argc > 1 && (maxChildren = atol(argv[1])) <= 0))
	{
		fprintf(stderr, "Invalid arguments for Simple Shell!\n");
		exit(1);
	}

	if ((pids = (int*) malloc(sizeof(int) * pids_size)) == NULL)
	{
		fprintf(stderr, "Failed to allocate memory!\n");
		exit(1);
	}

	while (1)
	{
		// Resizes the vector pids if the number of created processes exceeds its size. Terminates if realloc fails
		if (createdPids == pids_size && (pids = (int*) realloc(pids, sizeof(int) * (pids_size *= 2))) == NULL)
		{
			fprintf(stderr, "Failed to reallocate memory!\n");
			break;
		}

		// Retrieves a cmd line read from SimpleShell. If an error occurs, terminates SimpleShell
		if ((numTokens = readLineArguments(argVector, VECTOR_SIZE, buffer, BUFFER_SIZE)) == -1)
		{
			fprintf(stderr, "Failed to read from Simple Shell!\n");
			break;
		}

 		// Cmd run - run an instance of CircuitRouter-SeqSolver in a child process
		if (numTokens == 2 && strcmp(argVector[0], RUN_CMD) == 0)
		{
			if (maxChildren != -1 && maxChildren-- == 0)
			{
				waitProcesses(pids, &savedPids, 1);
				maxChildren = 0;
			}
			createdPids++;
			pid = fork();
		}
		// Cmd exit - waits for all child processes, prints the results and exits SimpleShell
		else if (numTokens == 1 && (strcmp(argVector[0], EXIT_CMD) == 0))
		{
			waitProcesses(pids, &savedPids, -1);
			printProcesses(pids, savedPids);
			free(pids);
			exit(0);
		}
		// Unkown cmd warns the user and waits for new input
		else
			fprintf(stderr, "Invalid command for Simple Shell!\n");

		// Make only child processes run CircuitRouter-SeqSolver. Terminates SimpleShell if it fails
		if (pid == 0)
		{
			execl(EXECUTABLE, EXECUTABLE, argVector[1], NULL);
			fprintf(stderr, "Error in execl call!\n");
			break;
		}
		// If an error occurs while creating a new process, terminates SimpleShell
		else if (pid < 0)
		{
			fprintf(stderr, "Error while making child process!\n");
			break;
		}
	}
	free(pids);
	exit(1);
}