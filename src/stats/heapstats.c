#include "config.h"

#ifdef WITH_LIVE_HEAP_STATISTICS

#ifndef BDW_CONSERVATIVE_GC
# error heapstats should only be compiled with BDW_CONSERVATIVE_GC
#endif
#ifndef WITH_STATISTICS
# error heapstats should only be compiled WITH_STATISTICS
#endif

#include <jni.h>
#include <jni-private.h> /* for ptroff_t */
#include <stdlib.h>
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#include "fni-stats.h" /* for stats-related macros */

/* XXX allow GC_FREQUENCY to be modified by debugger or an env. variable */
static int GC_FREQUENCY=100;

DECLARE_STATS_EXTERN(heap_current_live_bytes)
DECLARE_STATS_EXTERN(heap_max_live_bytes)
DECLARE_STATS_EXTERN(heap_total_alloc_bytes)
DECLARE_STATS_EXTERN(heap_total_alloc_count)

static void heapstats_finalizer(GC_PTR obj, GC_PTR _size_) {
  ptroff_t size = (ptroff_t) _size_;
  /* decrease the size of the live set */
  INCREMENT_STATS(heap_current_live_bytes, -size);
}

void *heapstats_alloc(jsize length) {
  void *result;
  stat_t ttl;
  /* do the allocation & register finalizer */
  result = GC_malloc(length);
  GC_register_finalizer_no_order(result, heapstats_finalizer,
				 (GC_PTR) ((ptroff_t) length), NULL, NULL);
  /* (sometimes) collect all dead objects */
  if (0 == (FETCH_STATS(heap_total_alloc_count) % GC_FREQUENCY))
    GC_gcollect();
  /* update total and current live */
  INCREMENT_STATS(heap_total_alloc_count, 1);
  INCREMENT_STATS(heap_total_alloc_bytes, length);
  INCREMENT_STATS(heap_current_live_bytes, length);
  /* update max_live */
  ttl = FETCH_STATS(heap_current_live_bytes);
  UPDATE_STATS(heap_max_live_bytes, ttl > _old_value_ ? ttl : _old_value_);

  return result;
}

#endif /* WITH_LIVE_HEAP_STATISTICS */
