#include "config.h"

/* Output debugging information for RTJ */
/*  #define RTJ_DEBUG 1 */

/* Look at the timing of calls to GC_malloc and GC_free */
/*  #define WITH_GC_STATS 1 */

/* Same info, except for RTJ internal calls */
/*  #define RTJ_TIMER 1 */

#ifdef WITH_GC_STATS
#include "GCstats.h"
#endif

#ifdef BDW_CONSERVATIVE_GC 
#ifdef WITH_GC_STATS 
#define RTJ_MALLOC GC_malloc_stats
#define RTJ_MALLOC_UNCOLLECTABLE GC_malloc_uncollectable_stats
#define RTJ_FREE GC_free_stats
#else 
#define RTJ_MALLOC GC_malloc
#define RTJ_MALLOC_UNCOLLECTABLE GC_malloc_uncollectable
#define RTJ_FREE GC_free 
#endif 
#else 
#define RTJ_MALLOC_UNCOLLECTABLE malloc
#define RTJ_FREE free 
#endif

