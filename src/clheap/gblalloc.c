/* Global heap allocation function. */

#include "alloc.h"	/* prototypes for NGBL_malloc* */
#include "config.h"	/* for BDW_CONSERVATIVE_GC */
#include "stats.h"	/* for UPDATE_STATS */
#include <stdlib.h>	/* for malloc, size_t */
#ifdef BDW_CONSERVATIVE_GC
# include "gc.h"	/* for GC_malloc */
#else
/* don't use B-D-W allocator */
# define GC_malloc malloc
# define GC_malloc_atomic malloc
#endif

void *NGBL_malloc(size_t size) {
  UPDATE_STATS(gbl, size);
  return GC_malloc(size);
}
void *NGBL_malloc_atomic(size_t size) {
  UPDATE_STATS(gbl, size);
  return GC_malloc_atomic(size);
}
