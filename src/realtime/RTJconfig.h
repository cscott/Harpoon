#include "config.h"

/* Output debugging information for RTJ */
/*  #define RTJ_DEBUG 1 */

/* Look at the timing of calls to GC_malloc and GC_free */
/*  #define WITH_GC_STATS 1 */

/* Same info, except for RTJ internal calls */
/*  #define RTJ_TIMER 1 */

/* When re-running a scope, reuse the same memory - this requires 
 * explicit deallocation by explicit calling of the "finally" method. */

/*  #define ALLOW_SCOPE_REENTRY */

#ifdef WITH_GC_STATS
#include "GCstats.h"
#endif
