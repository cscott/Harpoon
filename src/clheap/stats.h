/* Statistics. */

#ifndef INCLUDED_NIFTY_STATS_H
#define INCLUDED_NIFTY_STATS_H

#include "config.h" /* for WITH_THREADS */
#include "misc.h"   /* for MAKE_STATS */
#include <stdlib.h> /* for size_t */

#ifdef WITH_THREADS
# define LOCK_STATS(which) \
	flex_mutex_lock(& which##_stat_mutex)
# define UNLOCK_STATS(which) \
	flex_mutex_unlock(& which##_stat_mutex)
# define DECLARE_LOCK_EXTERN(which) \
	extern flex_mutex_t which##_stat_mutex
# define DECLARE_LOCK_LOCAL(which) \
	flex_mutex_t which##_stat_mutex = FLEX_MUTEX_INITIALIZER
#else
# define LOCK_STATS(which)
# define UNLOCK_STATS(which)
# define DECLARE_LOCK_EXTERN(which)
# define DECLARE_LOCK_LOCAL(which)
#endif

#ifdef MAKE_STATS
# define DECLARE_STATS_EXTERN(which) \
	extern size_t which##_bytes_alloc, which##_objs_alloc; \
	DECLARE_LOCK_EXTERN(which);
# define DECLARE_STATS_LOCAL(which) \
	size_t which##_bytes_alloc = 0, which##_objs_alloc = 0; \
	DECLARE_LOCK_LOCAL(which);
# define UPDATE_STATS(which, size) \
	do {\
	  LOCK_STATS(which);\
	  which##_bytes_alloc+=size;\
	  which##_objs_alloc++;\
	  UNLOCK_STATS(which);\
	} while(0)
#else
# define DECLARE_STATS_EXTERN(which)
# define DECLARE_STATS_LOCAL(which)
# define UPDATE_STATS(which, size) /* no op */
#endif

DECLARE_STATS_EXTERN(stk)
DECLARE_STATS_EXTERN(gbl)
DECLARE_STATS_EXTERN(thr)

void print_statistics(void);

#endif /* INCLUDED_NIFTY_STATS_H */
