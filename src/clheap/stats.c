/* Statistics stubs */
#include <stdio.h>
#include <signal.h>
#include "misc.h"	/* for MAKE_STATS */
#include "flexthread.h"	/* for flex_mutex_t */
#include "stats.h"	/* DECLARE_STATS_LOCAL */

/* allocate and initialize the statistics counters */
DECLARE_STATS_LOCAL(stk)
DECLARE_STATS_LOCAL(gbl)
DECLARE_STATS_LOCAL(thr)
size_t thr_bytes_overflow = 0;
int threads_created = 0;

static void stat_signal_handler(int sig) { print_statistics(); }

void print_statistics(void) {
#ifdef MAKE_STATS
  printf("\n"
	 "HEAPSIZE: %ld  POOLSIZE: %ld  THRALLOC: %d  STKALLOC: %d\n"
	 "Stack allocation:        %8ld bytes %8ld objects\n"
	 "Thread-local allocation: %8ld bytes %8ld objects\n"
	 "Global allocation:       %8ld bytes %8ld objects\n"
	 "Threads created:         %8d\n"
	 "Average heap size/thread:%8ld bytes\n"
	 "Thread heap overflow:    %8ld bytes\n",
	 (long) HEAPSIZE, (long) POOLSIZE,
#ifdef REALLY_DO_THR_ALLOC
	 1,
#else
	 0,
#endif
#ifdef REALLY_DO_STK_ALLOC
	 1,
#else
	 0,
#endif
	 (long) stk_bytes_alloc, (long) stk_objs_alloc,
	 (long) thr_bytes_alloc, (long) thr_objs_alloc,
	 (long) gbl_bytes_alloc, (long) gbl_objs_alloc,
	 threads_created,
	 threads_created ? (long) thr_bytes_alloc/threads_created: 0L,
	 (long) thr_bytes_overflow);
  fflush(stdout);
  /* print out statistics on demand */
  signal(SIGALRM, stat_signal_handler);
#endif
}
