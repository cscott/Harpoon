/* RTJconfig.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "config.h"

/* Output debugging information for RTJ */
/*  #define RTJ_DEBUG 1 */

/* Look at the timing of calls to GC_malloc and GC_free */
/*  #define WITH_GC_STATS 1 */

/* Look at the timing for RTJ internal calls */
/*  #define RTJ_TIMER 1 */

/* Output debugging information about memory references 
 * allocated through RTJ_malloc (like dmalloc, except at RTJ_malloc point)
 */
/*  #define RTJ_DEBUG_REF 1 */

#if (!defined(WITH_REALTIME_JAVA)) && (!defined(WITH_REALTIME_JAVA_STUBS))
#error Must have either RTJ or RTJ stubs defined in order to include this file.
#endif

#if defined(RTJ_DEBUG) || defined(RTJ_DEBUG_GC)
#ifdef RTJ_TIMER
#error RTJ_DEBUG or RTJ_DEBUG_GC will upset the timings of RTJ_TIMER.
#endif
#ifdef WITH_GC_STATS
#error RTJ_DEBUG or RTJ_DEBUG_GC will upset the timings of WITH_GC_STATS.
#endif
#endif

#if defined(WITH_NOHEAP_SUPPORT) && defined(BDW_CONSERVATIVE_GC)
#error NoHeapRealtimeThreads are not supported with the BDW conservative GC.
#endif

#if defined(WITH_REALTIME_JAVA) && !defined(WITH_THREADS)
#error Realtime Java requires thread support.
#endif

#ifdef WITH_GC_STATS
#include "GCstats.h"
#endif

#ifdef WITH_DMALLOC
#ifdef WITH_GC_STATS
#error DMALLOC with GC_stats not implemented.
#else
#include "dmalloc.h"
/* To prevent MACRO conflicts */
#define RTJ_MALLOC(size) _malloc_leap(__FILE__, __LINE__, size)
#define RTJ_MALLOC_UNCOLLECTABLE(size) _malloc_leap(__FILE__, __LINE__, size)
#define RTJ_FREE(ptr) _free_leap(__FILE__, __LINE__, ptr)
#endif
#else
#ifdef BDW_CONSERVATIVE_GC 
#ifdef WITH_GC_STATS 
#define RTJ_MALLOC(size) GC_malloc_stats(size)
#define RTJ_MALLOC_UNCOLLECTABLE(size) GC_malloc_uncollectable_stats(size)
#define RTJ_FREE(ptr) GC_free_stats(ptr)
#else 
#define RTJ_MALLOC(size) GC_malloc(size)
#define RTJ_MALLOC_UNCOLLECTABLE(size) GC_malloc_uncollectable(size)
#define RTJ_FREE(ptr) GC_free(ptr)
#endif 
#else 
#ifdef WITH_PRECISE_GC
#ifdef WITH_GC_STATS
#define RTJ_MALLOC(size) precise_malloc_stats(size)
#define RTJ_MALLOC_UNCOLLECTABLE(size) malloc_stats(size)
#define RTJ_FREE(ptr) free_stats(ptr)
#else
#define RTJ_MALLOC(size) precise_malloc(size)
#define RTJ_MALLOC_UNCOLLECTABLE(size) malloc(size)
#define RTJ_FREE(ptr) free(ptr)
#endif
#else
#ifdef WITH_GC_STATS
#define RTJ_MALLOC(size) malloc_stats(size)
#define RTJ_MALLOC_UNCOLLECTABLE(size) malloc_stats(size)
#define RTJ_FREE(ptr) free_stats(ptr)
#else
#define RTJ_MALLOC(size) malloc(size)
#define RTJ_MALLOC_UNCOLLECTABLE(size) malloc(size)
#define RTJ_FREE(ptr) free(ptr) 
#endif
#endif
#endif
#endif

