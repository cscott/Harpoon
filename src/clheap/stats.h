/* Statistics. */

#ifndef INCLUDED_NIFTY_STATS_H
#define INCLUDED_NIFTY_STATS_H

#include "fni-stats.h"

#define DECLARE_NIFTY_STATS(which)\
	DECLARE_STATS_EXTERN(which##_bytes_alloc)\
	DECLARE_STATS_EXTERN(which##_objs_alloc)
#define UPDATE_NIFTY_STATS(which, size) \
	do {\
	  DECLARE_NIFTY_STATS(which)\
	  INCREMENT_STATS(which##_bytes_alloc, size);\
	  INCREMENT_STATS(which##_objs_alloc, 1);\
	} while(0)

DECLARE_NIFTY_STATS(stk)
DECLARE_NIFTY_STATS(gbl)
DECLARE_NIFTY_STATS(thr)
DECLARE_STATS_EXTERN(thr_bytes_overflow)
DECLARE_STATS_EXTERN(thread_heaps_created)
DECLARE_STATS_EXTERN(threads_created)

#endif /* INCLUDED_NIFTY_STATS_H */
