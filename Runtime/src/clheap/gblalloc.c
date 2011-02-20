/* Global heap allocation function. */

#include "alloc.h"	/* prototypes for NGBL_malloc* */
#include "config.h"	/* for BDW_CONSERVATIVE_GC */
#include "stats.h"	/* for UPDATE_STATS */
#include <stdlib.h>	/* for malloc, size_t */
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif
#ifdef BDW_CONSERVATIVE_GC
# include "gc.h"	/* for GC_malloc */
#endif

/* allocate but don't update statistics: this is used in various places
 * when REALLY_DO_ALLOC is not defined. */
void *NGBL_malloc_noupdate(size_t size) {
#ifdef WITH_PRECISE_GC
  return precise_malloc(size);
#elif defined(BDW_CONSERVATIVE_GC)
  return GC_malloc(size);
#else
  return malloc(size);
#endif
}
/* allocate on the global heap. */
void *NGBL_malloc(size_t size) {
  UPDATE_NIFTY_STATS(gbl, size);
#ifdef WITH_PRECISE_GC
  return precise_malloc(size);
#elif defined(BDW_CONSERVATIVE_GC)
  return GC_malloc(size);
#else
  return malloc(size);
#endif
}
/* allocate an object with no internal pointers on the global heap. */
void *NGBL_malloc_atomic(size_t size) {
  UPDATE_NIFTY_STATS(gbl, size);
#ifdef WITH_PRECISE_GC
  return precise_malloc(size);
#elif defined(BDW_CONSERVATIVE_GC)
  return GC_malloc_atomic(size);
#else
  return malloc(size);
#endif
}
