#ifndef __GCstats_H__
#define __GCstats_H__

#include <time.h>
#include <unistd.h>
#include "config.h"
#include "gc.h"
#include <bits/time.h>
#include <stdlib.h> /* for malloc */
#include <stdio.h>  /* for printf */
#include <string.h> /* for memset */

#define WITH_REALTIME_CLOCK 1 

#ifdef WITH_REALTIME_CLOCK
#define CLOCK CLOCK_REALTIME
#else
#define CLOCK CLOCK_MONOTONIC
#endif

#ifdef WITH_REALTIME_JAVA
extern struct timespec total_finalize_time;
#endif

void* malloc_stats(size_t size_in_bytes);
void* calloc_stats(size_t nmemb, size_t elt_size);
void free_stats(void* object_addr);

#ifdef BDW_CONSERVATIVE_GC
void* GC_malloc_stats(size_t size_in_bytes);
void* GC_malloc_atomic_stats(size_t size_in_bytes);
void* GC_malloc_uncollectable_stats(size_t size_in_bytes);
void GC_free_stats(void* object_addr);
#endif

#ifdef WITH_PRECISE_GC
void* precise_malloc_stats(size_t size_in_bytes);
#endif

void setup_GC_stats();
void print_GC_stats();

#endif
