/* Statistics stubs */
#include "stats.h"

/* allocate and initialize the statistics counters */
DECLARE_STATS_LOCAL(stk)
DECLARE_STATS_LOCAL(gbl)
DECLARE_STATS_LOCAL(thr)

#ifdef MAKE_STATS
void print_statistics(void) {
  printf("Stack allocation:        %8ld bytes %8ld objects\n"
	 "Thread-local allocation: %8ld bytes %8ld objects\n"
	 "Global allocation:       %8ld bytes %8ld objects\n",
	 (long) stk_bytes_alloc, (long) stk_objs_alloc,
	 (long) thr_bytes_alloc, (long) thr_objs_alloc,
	 (long) gbl_bytes_alloc, (long) gbl_objs_alloc);
}
#endif
