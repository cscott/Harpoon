/* Runtime statistics package. */
#ifndef INCLUDED_FNI_STATS_H
#define INCLUDED_FNI_STATS_H

#include <stdlib.h> /* for size_t */
#include "config.h" /* for WITH_THREADS, WITH_STATISTICS */
#include "flexthread.h" /* for flex_mutex_lock, etc */

/* statistics counter locking */
#ifdef WITH_THREADS
# define LOCK_STATS(which) \
	flex_mutex_lock(& stat_mutex_##which)
# define UNLOCK_STATS(which) \
	flex_mutex_unlock(& stat_mutex_##which)
# define DECLARE_LOCK_EXTERN(which) \
	extern flex_mutex_t stat_mutex_##which;
# define DECLARE_LOCK_LOCAL(which) \
	flex_mutex_t stat_mutex_##which = FLEX_MUTEX_INITIALIZER;
#else
# define LOCK_STATS(which)
# define UNLOCK_STATS(which)
# define DECLARE_LOCK_EXTERN(which)
# define DECLARE_LOCK_LOCAL(which)
#endif

/* all counters are size_t, and are initialized to 0 */
#ifdef WITH_STATISTICS
# define DECLARE_STATS_EXTERN(which) \
	extern size_t stat_counter_##which;\
	DECLARE_LOCK_EXTERN(which)
# define DECLARE_STATS_LOCAL(which) \
	size_t stat_counter_##which;\
	DECLARE_LOCK_LOCAL(which)
# define INCREMENT_STATS(which, amount) \
	do {\
	  LOCK_STATS(which);\
          stat_counter_##which += amount;\
	  UNLOCK_STATS(which);\
	} while(0)
#define FETCH_STATS(which) \
	({ size_t val;\
	   LOCK_STATS(which);\
	   val=stat_counter_##which;\
	   UNLOCK_STATS(which);\
	   val; })
#else /* !WITH_STATISTICS */
# define DECLARE_STATS_EXTERN(which)
# define DECLARE_STATS_LOCAL(which)
# define INCREMENT_STATS(which, amount) /* no op */
# define FETCH_STATS(which) ((size_t)0)
#endif /* WITH_STATISTICS */

#endif /* INCLUDED_FNI_STATS_H */
