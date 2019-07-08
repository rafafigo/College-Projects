/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This code is an adaptation of the Lee algorithm's implementation originally included in the STAMP Benchmark
 * by Stanford University.
 *
 * The original copyright notice is included below.
 *
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright (C) Stanford University, 2006.  All Rights Reserved.
 * Author: Chi Cao Minh
 *
 * =============================================================================
 *
 * Unless otherwise noted, the following license applies to STAMP files:
 *
 * Copyright (c) 2007, Stanford University
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in
 *       the documentation and/or other materials provided with the
 *       distribution.
 *
 *     * Neither the name of Stanford University nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY STANFORD UNIVERSITY ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL STANFORD UNIVERSITY BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * =============================================================================
 *
 * CircuitRouter-SeqSolver.c
 *
 * =============================================================================
 * 
 * IST LEIC-T Sistemas Operativos 18/19
 * Exercicio 1 - CircuitRouter-SeqSolver.c
 *
 * Adaptation:
 * Miguel Levezinho  - 90756
 * Rafael Figueiredo - 90770
 */

#include <assert.h>
#include <getopt.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include "lib/list.h"
#include "maze.h"
#include "router.h"
#include "lib/timer.h"
#include "lib/types.h"

#define MAX_FILE_NAME_SIZE 50

enum param_types {
    PARAM_BENDCOST = (unsigned char)'b',
    PARAM_XCOST    = (unsigned char)'x',
    PARAM_YCOST    = (unsigned char)'y',
    PARAM_ZCOST    = (unsigned char)'z',
};

enum param_defaults {
    PARAM_DEFAULT_BENDCOST = 1,
    PARAM_DEFAULT_XCOST    = 1,
    PARAM_DEFAULT_YCOST    = 1,
    PARAM_DEFAULT_ZCOST    = 2,
};

char* global_inputFile;
long global_params[256]; /* 256 = ascii limit */


/* =============================================================================
 * displayUsage
 * =============================================================================
 */
static void displayUsage (const char* appName){
    printf("Usage: %s [options]\n", appName);
    puts("\nOptions:                            (defaults)\n");
    printf("    b <INT>    [b]end cost          (%i)\n", PARAM_DEFAULT_BENDCOST);
    printf("    x <UINT>   [x] movement cost    (%i)\n", PARAM_DEFAULT_XCOST);
    printf("    y <UINT>   [y] movement cost    (%i)\n", PARAM_DEFAULT_YCOST);
    printf("    z <UINT>   [z] movement cost    (%i)\n", PARAM_DEFAULT_ZCOST);
    printf("    h          [h]elp message       (false)\n");
    exit(1);
}


/* =============================================================================
 * setDefaultParams
 * =============================================================================
 */
static void setDefaultParams (){
    global_params[PARAM_BENDCOST] = PARAM_DEFAULT_BENDCOST;
    global_params[PARAM_XCOST]    = PARAM_DEFAULT_XCOST;
    global_params[PARAM_YCOST]    = PARAM_DEFAULT_YCOST;
    global_params[PARAM_ZCOST]    = PARAM_DEFAULT_ZCOST;
}


/* =============================================================================
 * parseArgs
 * =============================================================================
 */
static char* parseArgs (long argc, char* const argv[]){

    long i;
    long opt;
    char* inputFile = NULL; // String that will hold the name of the file to open

    int argerr = 0; // Number of non-opt argument errors
    opterr = 0;     // Disable getopt() from printing error messages in stderr

    setDefaultParams();

    while ((opt = getopt(argc, argv, "hb:x:y:z:")) != -1) {
        switch (opt) {
            case 'b':
            case 'x':
            case 'y':
            case 'z':
                global_params[(unsigned char)opt] = atol(optarg);
                break;
            case '?':
            case 'h':
            default:
                // Terminates the program if an invalid opt arg is passed and displays valid options
                displayUsage(argv[0]);
                break;
        }
    }
    // Terminates the program if no non-opt arg is given (inputfile missing)
    if (optind >= argc)
    {
        fprintf(stderr, "Inputfile name not specified!\n");
        argerr++;
    }
    // Iterates through the non-opt args to get the inputfile name. Terminates the program if invalid args are found
    for (i = optind; i < argc; i++)
    {
        if (access(argv[i], F_OK) == -1 || inputFile != NULL)
        {
            fprintf(stderr, "Invalid non-option inputfile: %s\n", argv[i]);
            argerr++;
        }
        else
            inputFile = argv[i];
    }
    if (argerr)
        exit(1);

    return inputFile;
}


/* =============================================================================
 * main
 * =============================================================================
 */
int main(int argc, char** argv){
    /*
     * Initialization
     */
    FILE *ifp;        // Pointer to the input file
    FILE *ofp = NULL; // Pointer to the output file

    char* outputFile;    // String that will hold the name of the file to write
    char* outputFileOld; // String that may hold the name of the backup output file 

    // Parses through the arguments and filters out the input file to read from
    char* inputFile = parseArgs(argc, (char** const)argv);

    // Opens the inputfile for reading (r+ used to make sure the function is opening a file and not a directory)
    if((ifp = fopen(inputFile, "r+")) == NULL)
    {             
        fprintf(stderr, "Error opening %s for reading!\n", inputFile);
        exit(1);
    }

    // Creates an output file name from the input files name
    if ((outputFile = (char*) malloc(sizeof(char) * (strlen(inputFile) + 5))) == NULL)
    {
        fprintf(stderr, "Failed to allocate memory!\n");
        exit(1);
    }
    strcpy(outputFile, inputFile);
    strcat(outputFile, ".res");

    // If an output file already exists
    if (access(outputFile, F_OK) == 0)
    {
        // Creates a second output file name from the input files name
        if ((outputFileOld = (char*) malloc(sizeof(char) * (strlen(inputFile) + 9))) == NULL)
        {
            fprintf(stderr, "Failed to allocate memory!\n");
            exit(1);
        }
        strcpy(outputFileOld, outputFile);
        strcat(outputFileOld, ".old");

        // Swaps the output files names (.res becomes .res.old)
        if(rename(outputFile, outputFileOld) == -1)
        {
        	fprintf(stderr, "Error while renaming %s for %s!\n",outputFile,outputFileOld );
        	exit(1);
        }
        free(outputFileOld);
    }
    // Opens the output file for writing
    if ((ofp = fopen(outputFile, "w")) == NULL)
    {
        fprintf(stderr, "Error creating %s for writing!\n", outputFile);
        exit(1);
    }
    
    free(outputFile);

    maze_t* mazePtr = maze_alloc();
    assert(mazePtr);

    long numPathToRoute = maze_read(mazePtr, ifp, ofp);

    // Closes the input file after everything has been read from it
    if (fclose(ifp) == EOF)
    {
        fprintf(stderr, "Error closing %s!\n", inputFile);
        exit(1);
    }

    router_t* routerPtr = router_alloc(global_params[PARAM_XCOST],
                                       global_params[PARAM_YCOST],
                                       global_params[PARAM_ZCOST],
                                       global_params[PARAM_BENDCOST]);
    assert(routerPtr);
    list_t* pathVectorListPtr = list_alloc(NULL);
    assert(pathVectorListPtr);

    router_solve_arg_t routerArg = {routerPtr, mazePtr, pathVectorListPtr};
    TIMER_T startTime;
    TIMER_READ(startTime);

    router_solve((void *)&routerArg);

    TIMER_T stopTime;
    TIMER_READ(stopTime);

    long numPathRouted = 0;
    list_iter_t it;
    list_iter_reset(&it, pathVectorListPtr);
    while (list_iter_hasNext(&it, pathVectorListPtr)) {
        vector_t* pathVectorPtr = (vector_t*)list_iter_next(&it, pathVectorListPtr);
        numPathRouted += vector_getSize(pathVectorPtr);
	}
    fprintf(ofp, "Paths routed    = %li\n", numPathRouted);
    fprintf(ofp, "Elapsed time    = %f seconds\n", TIMER_DIFF_SECONDS(startTime, stopTime));


    /*
     * Check solution and clean up
     */
    assert(numPathRouted <= numPathToRoute);
    bool_t status = maze_checkPaths(mazePtr, pathVectorListPtr, ofp);
    assert(status == TRUE);
    fprintf(ofp, "Verification passed.\n");

    // Closes the output file after everything has been written on it
    if (fclose(ofp) == EOF)
    {
        fprintf(stderr, "Error closing %s!\n", outputFile);
        exit(1);
    }


    maze_free(mazePtr);
    router_free(routerPtr);

    list_iter_reset(&it, pathVectorListPtr);
    while (list_iter_hasNext(&it, pathVectorListPtr)) {
        vector_t* pathVectorPtr = (vector_t*)list_iter_next(&it, pathVectorListPtr);
        vector_t* v;
        while((v = vector_popBack(pathVectorPtr))) {
            // v stores pointers to longs stored elsewhere; no need to free them here
            vector_free(v);
        }
        vector_free(pathVectorPtr);
    }
    list_free(pathVectorListPtr);


    exit(0);
}


/* =============================================================================
 *
 * End of CircuitRouter-SeqSolver.c
 *
 * =============================================================================
 */
