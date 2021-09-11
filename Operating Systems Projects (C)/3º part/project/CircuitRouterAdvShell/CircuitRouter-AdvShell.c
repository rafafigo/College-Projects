/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * IST LEIC-T Sistemas Operativos 18/19
 * Exercicio 3 - CircuitRouter-AdvShell.c
 *
 * Authors:
 * Miguel Levezinho  - 90756
 * Rafael Figueiredo - 90770
 */

#include "CircuitRouter-AdvShell.h"
#include <errno.h>
#include <fcntl.h>
#include "lib/commandlinereader.h"
#include "lib/timer.h"
#include "lib/vector.h"
#include <limits.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/select.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <unistd.h>

#define EXIT_CMD "exit"
#define RUN_CMD "run"

#define BUFFER_SIZE 150
#define DEFAULT_PID_STORAGE_SIZE 100
#define VECTOR_SIZE 4

#define ERROR_FIFO "Command not supported\n"
#define EXECUTABLE "CircuitRouter-SeqSolver"
#define SHELL_PATH "CircuitRouter-AdvShell.pipe"


long maxChildren = -1; // Max number of simultaneous processes running. Default -1 means unlimited
child_t* pids;         // Vector with structure that stores PIDs, exit success and time
long numPids = 0;      // Number of terminated processes


/* Auxiliar function that searches for the pid id in the structure in order to set the time stop and the status
 * Exit status in  pids:
 * Exit status is encypted in the PID. If the program exited normally (exit(0)), PID is stored
 * positive. Otherwise (exit(1) or forced termination), PID is stored negative.
 *
 * pid      - Tells the pid of the child that terminated
 * stopTime - Time that the child finished
 */
static void addPid(int pid, int status, TIMER_T stopTime)
{
	int i;

	for (i = 0; i < numPids; i++)
        if (pid == pids[i].pid)
            break;

    if (WIFEXITED(status) && !WEXITSTATUS(status))
        pids[i].pid = pid;
    else
        pids[i].pid = pid * -1;

    pids[i].timeStop = stopTime;
}

/* Auxiliar function that handles SIGCHLD
 * Ignores all the signals by SIGCHLD until the end of the function
 * By using it while we are making sure that if another signal is catched we don't waste time with the callback function
 * and for that reason a more precise time
 */
static void sigchldHandler(int sig)
{
    TIMER_T stopTime;
    int pid;
    int status;

    while ((pid = waitpid(-1, &status, WNOHANG)) > 0)
    {
        TIMER_READ(stopTime);

        addPid(pid,status,stopTime);
        maxChildren++;
    }
}

/* Auxiliar function that waits for child processes to finish and stores their PID
 * If the wait() function returns -1 due to a signal interruption (errno is set to EINTR), the loop
 * reiterates to account for the possible remaining process(es)
 *
 * numWaits - Specifies the number of processes to wait for. If negative, waits for all processes
 */
static void waitProcesses(int numWaits)
{
	TIMER_T stopTime;
    int pid;
    int status;

    while ((pid = wait(&status)) != -1 || errno == EINTR)
    {
    	TIMER_READ(stopTime);
        if (pid == -1)
            continue;

        addPid(pid,status,stopTime);
        if (numWaits == 1)
        	break;
    }
}

/* Auxiliary function that prints the results of all the processes runnings.
 */
static void printProcesses()
{
    int i;

    for (i = 0; i < numPids; i++)
    {
        if (pids[i].pid < 0)
            printf("CHILD EXITED (PID=%d; return NOK; %d s)\n", 
            	pids[i].pid * -1, (int) TIMER_DIFF_SECONDS(pids[i].timeStart, pids[i].timeStop));
        else
            printf("CHILD EXITED (PID=%d; return OK; %d s)\n", 
            	pids[i].pid, (int) TIMER_DIFF_SECONDS(pids[i].timeStart, pids[i].timeStop));
    }
    puts("END.");
}

/* Auxiliary funtion that runs an instance of CircuitRouter-SeqSolver in a child process.
 * In case of being a fifo, closes the stdout and replaces with fifo in the child process.
 * On failure, return -1.
 * 
 * pathName    - PathName of Client in order to open fifo (if NULL, the run was sent from the Shell)
 * arg         - Pointer to string that contains inputfile name
 * mask        - Mask used to block signals using sigprocmask()
 */
static int run_cmd(char* pathName, char* arg, sigset_t mask)
{
	TIMER_T startTime;

    int fifo_client; // Named pipe from CLient
    int pid = 1;     // Process ID for running specific code

    // SIGCHILD handleing is blocked during access to global fields 
    sigprocmask(SIG_BLOCK, &mask, NULL);

    // If true, the size of maxChildren was reached and the program needs to wait until at least one child process returns
    if (maxChildren != -1 && maxChildren-- == 0)
    {
        waitProcesses(1);
        maxChildren = 0;
    }
    
    TIMER_READ(startTime);;
    pid = fork();

    // Make child processes run CircuitRouter-SeqSolver
    if (pid == 0)
    {
        // If the run cmd was sent by a client, opens the fifo and redirects the stdout to it
        if (pathName != NULL)
        {
            if ((fifo_client = open(pathName, O_WRONLY)) < 0)
            {
                perror("Error while opening client fifo");
                return -1;
            } 
            close(1);
            dup(fifo_client);
            close(fifo_client);
        }

        execl(EXECUTABLE, EXECUTABLE, arg, NULL);
        perror("Error in execl call!");
        return -1;

    } else if (pid > 0)
    {
    	pids[numPids].pid = pid;
    	pids[numPids++].timeStart = startTime;
    	sigprocmask(SIG_UNBLOCK, &mask, NULL);

    } else
    {
        perror("Error while making child process!");
        return -1;
    }

    return 0;
}

 /* Auxiliary funtion that is called when a message was sent by a client.
  * On failure, returns -1.
  *
  * argVector   - Pointer to pointer of chars in this case each pointer of chars is a token (done by readLineArguments)
  * buffer      - Buffer to keep the message that was sent by a client
  * fifo_shell  - file descriptor from shell
  * mask        - Mask used to block signals
  */
static int clientMessage(char** argVector, char* buffer, int fifo_shell, sigset_t mask)
{
	int numTokens = 0;
	int fifo_client;

    // argvector[0] is always the path of fifo_client
	if ((numTokens = readLineArguments(argVector, VECTOR_SIZE, buffer, BUFFER_SIZE, fifo_shell)) == -1)
	{
		perror("Failed to read from Client");
		return -1;

	} else if (numTokens == 3 && strcmp(argVector[1], RUN_CMD) == 0)
    {
    	 // Executes run command
        if (run_cmd(argVector[0], argVector[2], mask) == -1)
            return -1;

    } else if (numTokens > 0)
    {
        // Opens client fifo in order to send an error message
        if ((fifo_client = open(argVector[0], O_WRONLY)) < 0)
        {
            perror("Error while opening client fifo");
            return -1;
        } 

        // Sends error message to client
        if (write(fifo_client, ERROR_FIFO, strlen(ERROR_FIFO) + 1) <= 0)
        {
            close(fifo_client);
            perror("Error while writing to fifo");
            return -1;
        }
        close(fifo_client);

    } else // Something went wrong on readlineArguments
    {
    	perror("Error while reading command");
    	return -1;
    }

    return 0;
}

 /* Auxiliary function that is called when a message was sent by stdin.
  * On failure, returns 1. On success, 0 is return for termination and 
  *
  * argVector   - Pointer to pointer of chars in this case each pointer of chars is a token (done by readLineArguments)
  * file_Server - Pointer to file descriptor of shell
  * mask        - Mask used to block signals
  */
static int inputMessage(char** argVector,char* buffer,sigset_t mask)
{
	int numTokens;

    if ((numTokens = readLineArguments(argVector, VECTOR_SIZE, buffer, BUFFER_SIZE, 0)) == -1)
    {
    	perror("Failed to read from AdvShell!");
    	return 1;

    } else if (numTokens == 2 && strcmp(argVector[0], RUN_CMD) == 0)
    {
    	// Executes run command
		if (run_cmd(NULL, argVector[1], mask) == -1)
            return 1;
    }
    /* Cmd exit from stdin - waits for all child processes, */
    /* Prints the results and exits SimpleShell (blocks SIGCHLD) */
    else if (numTokens == 1 && (strcmp(argVector[0], EXIT_CMD) == 0))
    {
        sigprocmask(SIG_BLOCK, &mask, NULL);
		waitProcesses(-1);
		printProcesses();
		return 0;

	} else
		perror("Invalid command for Advanced Shell!");

	return 2;
}

/* Auxiliar function that waits for a message to be ready for reading.
 * On failure, returns -1.
 *
 * fifo_shell - Descriptor of shell
 */
static int doSelect(int fifo_shell, fd_set readFds, fd_set* testFds)
{
    // Copies the mask because select changes its value
    *testFds = readFds;

    // Waits for a message to be received from the descriptores 0 and fifo_shell
    while (select(fifo_shell + 1, testFds, 0, 0, 0) == -1)
    {
    	if (errno == EINTR)
    		continue;

    	perror("Error while doing select!");
    	return -1;
    }
    return 0;
}

int main (int argc, char** argv) 
{
	long pids_size = DEFAULT_PID_STORAGE_SIZE; // Size of vector pids

	char buffer[BUFFER_SIZE];	  // Stores a cmd line read from AdvShell or sent by a client
	char* argVector[VECTOR_SIZE]; // Stores the cmd line separeted in string tokens

	int exitStatus = 1; // Exit status
    int fifo_shell;     // Descriptor of fifo from shell that is known by the client

    fd_set readFds; // Mask that is used to activate the bits of the the descriptors that can transmit a message
    fd_set testFds; // Mask used in the select to know if any of the descriptors are waiting to transmit a message

    // Deletes the pipe from file system
    unlink(SHELL_PATH);

    struct sigaction signal; // Represents the new action to take upon receiving SIGCHLD signals

    signal.sa_handler = sigchldHandler;  // Function that will handle the signal
    sigemptyset(&signal.sa_mask);        // Cleans the mask set of affected signals
    sigaddset(&signal.sa_mask, SIGCHLD); // Sets SIGCHLD in the mask of affected signals

    // Sets the behavior of handling to ignore stops in child processes and to follow BSD semantics
    signal.sa_flags = SA_NOCLDSTOP | SA_RESTART;

    sigset_t mask = signal.sa_mask; // Auxiliary mask used to block SIGCHLD while accessing global fields

    // Assigns a function to handle SIGCHLD
    if (sigaction(SIGCHLD, &signal, NULL) == -1)
    {	
    	perror("Failed to set signal handler using sigaction!");
    	exit(exitStatus);
    }
    // Checks if AdvShell received wrong values for simultaneous process count
    if (argc > 2 || (argc > 1 && (maxChildren = atol(argv[1])) <= 0))
    {
        perror("Invalid arguments for Advanced Shell!");
        exit(exitStatus);
    }
    // Alocate memory for the vector
    if ((pids = (child_t*) malloc(sizeof(child_t) * pids_size)) == NULL)
    {
        perror("Failed to allocate memory!");
        exit(exitStatus);
    }
    // Creates fifo with shell path
    if (mkfifo(SHELL_PATH, 0777) == -1)
    {
        perror("Error while creating fifo from shell!");
        exit(exitStatus);
    }

    // Opens shell fifo with read and write permissions.
    // Altough the fifo is being used for reading only, this flag will prevent the shell from staying open whitout writers,
    // making the call to read not return 0.
	if ((fifo_shell = open(SHELL_PATH, O_RDWR)) == -1)
	{
	    unlink(SHELL_PATH);
	    perror("Error while opening fifo from shell!");
	    exit(exitStatus);
	}

    // Composition of the mask representing the descriptors to be monitored
    FD_ZERO(&readFds);            // Clean the set of file descriptors to monitor
    FD_SET(0, &readFds);          // Add stdin to the set
    FD_SET(fifo_shell, &readFds); // Add the named pipe to the set

    while (1)
    {
        // Resizes the vector pids if the number of created processes exceeds its size. Terminates if realloc fails
        if (numPids == (pids_size - 2) && (pids = (child_t*) realloc(pids, sizeof(child_t) * (pids_size *= 2))) == NULL)
        {
            perror("Failed to reallocate memory!");
            break;
        }

        if (doSelect(fifo_shell, readFds, &testFds) == -1)
        	break;

        // If a message was sent by a client or the last client closed the server
        if (FD_ISSET(fifo_shell, &testFds))
            if (clientMessage(argVector, buffer, fifo_shell, mask) == -1)
            	break;
        
        // If a message was sent by stdin
        if (FD_ISSET(0, &testFds))
        {
        	exitStatus = inputMessage(argVector, buffer, mask);
            if (exitStatus != 2)
                break;
            exitStatus = 1;
        }		
    }
    free(pids);
	close(fifo_shell);
    unlink(SHELL_PATH);
    exit(exitStatus);
}