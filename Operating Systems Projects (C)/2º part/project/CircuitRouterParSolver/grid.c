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
 * grid.c
 *
 * =============================================================================
 *
 * IST LEIC-T Sistemas Operativos 18/19
 * Exercicio 2 - grid.c
 *
 * Adaptation:
 * Miguel Levezinho  - 90756
 * Rafael Figueiredo - 90770
 */


#include <assert.h>
#include <errno.h>
#include <fcntl.h>
#include <getopt.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <time.h>
#include "coordinate.h"
#include "grid.h"
#include "lib/types.h"
#include "lib/vector.h"
#include "math.h"


const unsigned long CACHE_LINE_SIZE = 32UL;


/* =============================================================================
 * grid_alloc
 * =============================================================================
 */
grid_t* grid_alloc (long width, long height, long depth){
    grid_t* gridPtr;

    gridPtr = (grid_t*)malloc(sizeof(grid_t));
    if (gridPtr) {
        gridPtr->width  = width;
        gridPtr->height = height;
        gridPtr->depth  = depth;
        long n = width * height * depth;
        long* points_unaligned = (long*)malloc(n * sizeof(long) + CACHE_LINE_SIZE);
        assert(points_unaligned);
        gridPtr->points_unaligned = points_unaligned;
        gridPtr->points = (long*)((char*)(((unsigned long)points_unaligned
                                          & ~(CACHE_LINE_SIZE-1)))
                                  + CACHE_LINE_SIZE);

        // Create array of mutexes for each grid point
        gridPtr->mutex_points = (pthread_mutex_t*) malloc(n * sizeof(pthread_mutex_t));
        assert(gridPtr->mutex_points);

        memset(gridPtr->points, GRID_POINT_EMPTY, (n * sizeof(long)));
    }

    return gridPtr;
}

/* =============================================================================
 * grid_free
 * =============================================================================
 */
void grid_free (grid_t* gridPtr){
    free(gridPtr->points_unaligned);
    free(gridPtr->mutex_points);
    free(gridPtr);
}


/* =============================================================================
 * grid_copy
 * =============================================================================
 */
void grid_copy (grid_t* dstGridPtr, grid_t* srcGridPtr){
    assert(srcGridPtr->width  == dstGridPtr->width);
    assert(srcGridPtr->height == dstGridPtr->height);
    assert(srcGridPtr->depth  == dstGridPtr->depth);

    long n = srcGridPtr->width * srcGridPtr->height * srcGridPtr->depth;
    memcpy(dstGridPtr->points, srcGridPtr->points, (n * sizeof(long)));
}


/* =============================================================================
 * grid_isPointValid
 * =============================================================================
 */
bool_t grid_isPointValid (grid_t* gridPtr, long x, long y, long z){
    if (x < 0 || x >= gridPtr->width  ||
        y < 0 || y >= gridPtr->height ||
        z < 0 || z >= gridPtr->depth)
    {
        return FALSE;
    }

    return TRUE;
}


/* =============================================================================
 * grid_getPointRef
 * =============================================================================
 */
long* grid_getPointRef (grid_t* gridPtr, long x, long y, long z){
    return &(gridPtr->points[(z * gridPtr->height + y) * gridPtr->width + x]);
}

/* =============================================================================
 * grid_getMutexRef - Returns the address of point mutex in memory
 * =============================================================================
 */
pthread_mutex_t* grid_getMutexRef(grid_t* gridPtr, long x, long y, long z) {
    return &(gridPtr->mutex_points[(z * gridPtr->height + y) * gridPtr->width + x]);
}


/* =============================================================================
 * grid_getPointIndices
 * =============================================================================
 */
void grid_getPointIndices (grid_t* gridPtr, long* gridPointPtr, long* xPtr, long* yPtr, long* zPtr){
    long height = gridPtr->height;
    long width  = gridPtr->width;
    long area = height * width;
    long index3d = (gridPointPtr - gridPtr->points);
    (*zPtr) = index3d / area;
    long index2d = index3d % area;
    (*yPtr) = index2d / width;
    (*xPtr) = index2d % width;
}


/* =============================================================================
 * grid_getPoint
 * =============================================================================
 */
long grid_getPoint (grid_t* gridPtr, long x, long y, long z){
    return *grid_getPointRef(gridPtr, x, y, z);
}


/* =============================================================================
 * grid_isPointEmpty
 * =============================================================================
 */
bool_t grid_isPointEmpty (grid_t* gridPtr, long x, long y, long z){
    long value = grid_getPoint(gridPtr, x, y, z);
    return ((value == GRID_POINT_EMPTY) ? TRUE : FALSE);
}


/* =============================================================================
 * grid_isPointFull
 * =============================================================================
 */
bool_t grid_isPointFull (grid_t* gridPtr, long x, long y, long z){
    long value = grid_getPoint(gridPtr, x, y, z);
    return ((value == GRID_POINT_FULL) ? TRUE : FALSE);
}


/* =============================================================================
 * grid_setPoint
 * =============================================================================
 */
void grid_setPoint (grid_t* gridPtr, long x, long y, long z, long value){
    (*grid_getPointRef(gridPtr, x, y, z)) = value;
}


/* =============================================================================
 * grid_addPath
 * =============================================================================
 */
void grid_addPath (grid_t* gridPtr, vector_t* pointVectorPtr){
    long i;
    long n = vector_getSize(pointVectorPtr);

    for (i = 0; i < n; i++) {
        coordinate_t* coordinatePtr = (coordinate_t*)vector_at(pointVectorPtr, i);
        long x = coordinatePtr->x;
        long y = coordinatePtr->y;
        long z = coordinatePtr->z;
        grid_setPoint(gridPtr, x, y, z, GRID_POINT_FULL);
    }
}

/* Auxiliary function that releases the mutexes of the points that were reserved by a thread.
 *
 * index          - Index of the last locked mutex. Release is done in inverse order of acquisition;
 * gridPtr        - Pointer to the grid;
 * pointVectorPtr - Pointer to the vector that stores the path of points;
 * gridPointPtr   - Pointer to a point of the grid;
 * validPath      - Specifies if the released points should be filled, to set the path final.
 */
static void grid_pointsUnlock(int index, grid_t* gridPtr, vector_t* pointVectorPtr, long* gridPointPtr, bool_t validPath)
{
    int i;
    long x, y, z;

    for (i = index; i > 0; i--)
    {
        gridPointPtr = (long*) vector_at(pointVectorPtr, i);
        grid_getPointIndices(gridPtr, gridPointPtr, &x, &y, &z);

        if (validPath)
            *gridPointPtr = GRID_POINT_FULL;

        if (pthread_mutex_unlock(grid_getMutexRef(gridPtr, x, y, z)))
        {
            perror("Error unlocking grid point mutex!");
            exit(1);
        }
    }
}

/* =============================================================================
 * grid_addPath_Ptr
 * =============================================================================
 */
bool_t grid_addPath_Ptr (grid_t* gridPtr, vector_t* pointVectorPtr, long* tries)
{
    long i;
    long n = vector_getSize(pointVectorPtr); // Size of the vector of points to aquire and fill
    long* gridPointPtr = NULL;               // Pointer to a point of the grid
    long x, y, z;                            // Indexes of grid point coordinates

    int lock; // Used to aquire the success of a point lock attempt

    long max; // Max number of nanoseconds a thread can sleep for. Increases exponentially with tries
    long rad; // Random number between 0 and max;

    // Loops through all the points in a given path. Each thread will compete to acquire all points of their path
    for (i = 1; i < (n - 1); i++)
    {
        // Acquires the coordinates of a point to use for getting the respective mutex
        gridPointPtr = (long*) vector_at(pointVectorPtr, i);
        grid_getPointIndices(gridPtr, gridPointPtr, &x, &y, &z);

        // Tries to lock the access to the point to reserve it. Ends the program if the function fails by some other reason
        if ((lock = pthread_mutex_trylock(grid_getMutexRef(gridPtr, x, y, z))) != 0 && lock != EBUSY)
        {
            perror("Error trylocking grid point mutex!");
            exit(1);
        }

        // If the point some thread tried to aquire is being used by some other thread (meaning the point will probably be filled)
        if (lock == EBUSY)
        {
            // Releases all the locked points thus far (starts at i - 1 because the current point is the one locked by some other thread)
            grid_pointsUnlock(i - 1, gridPtr, pointVectorPtr, gridPointPtr, FALSE);

            max = 100 * pow(2, ++(*tries));

            // Confines sleep time to 100 000 nanoseconds
            if((rad = random() % max) > 100000)
                rad = 100000;

            struct timespec sleepTime = {0, rad};
            struct timespec remSleepTime = {0, 0};

            // Sleeps for some random number of nanoseconds, between 0 and max, to prevent situations of starvation
            while (nanosleep(&sleepTime, &remSleepTime) == -1)
            {
                // If the function is interrupted by a signal, goes back to sleep for the remaining time (set automatically in remSleepTime). Else exits with an error
                if (errno != EINTR)
                {
                    perror("Error While doing nanosleep!");
                    exit(1);
                }
                sleepTime.tv_nsec = remSleepTime.tv_nsec;
            }
            return FALSE;
        }
        // If a point as already been mapped to the grid as part of a path
        else if (*gridPointPtr == GRID_POINT_FULL)
        {
            // Releases all the locked points thus far
            grid_pointsUnlock(i, gridPtr, pointVectorPtr, gridPointPtr, FALSE);

            *tries = 0;
            return FALSE;
        }
    }
    // Releases all the locked points of the path and fills them 
    grid_pointsUnlock(n - 2, gridPtr, pointVectorPtr, gridPointPtr, TRUE);

    return TRUE;
}

/* =============================================================================
 * grid_print
 * =============================================================================
 */
void grid_print (grid_t* gridPtr, FILE *fp){
    long width  = gridPtr->width;
    long height = gridPtr->height;
    long depth  = gridPtr->depth;
    long z;
    for (z = 0; z < depth; z++) {
        fprintf(fp, "[z = %li]\n", z);
        long x;
        for (x = 0; x < width; x++) {
            long y;
            for (y = 0; y < height; y++) {
                fprintf(fp,"%4li", *grid_getPointRef(gridPtr, x, y, z));
            }
            fputs("\n",fp);
        }
        fputs("\n",fp);
    }
}


/* =============================================================================
 *
 * End of grid.c
 *
 * =============================================================================
 */
