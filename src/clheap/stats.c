/* Statistics stubs */
#include "misc.h"	/* for MAKE_STATS */
#include "flexthread.h"	/* for flex_mutex_t */
#include "stats.h"	/* DECLARE_STATS_LOCAL */

/* allocate and initialize the statistics counters */
DECLARE_STATS_LOCAL(stk)
DECLARE_STATS_LOCAL(gbl)
DECLARE_STATS_LOCAL(thr)

void print_statistics(void) {
#ifdef MAKE_STATS
  printf("Stack allocation:        %8ld bytes %8ld objects\n"
	 "Thread-local allocation: %8ld bytes %8ld objects\n"
	 "Global allocation:       %8ld bytes %8ld objects\n",
	 (long) stk_bytes_alloc, (long) stk_objs_alloc,
	 (long) thr_bytes_alloc, (long) thr_objs_alloc,
	 (long) gbl_bytes_alloc, (long) gbl_objs_alloc);
#endif
}
