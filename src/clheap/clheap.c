#include "config.h"
#include "clheap.h"	/* prototypes */
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"		/* GC_malloc_uncollectable, GC_free */
#endif
#include "flexthread.h"	/* flex_mutex_lock */
#include "misc.h"	/* for ALIGN */
#include <stdlib.h>	/* for malloc, size_t */

void *clheap_alloc(clheap_t clh, size_t size) {
  char *result;
#ifdef WITH_THREADS
  flex_mutex_lock(&(clh->heap_lock));
#endif
  result = clh->heap_top;
  clh->heap_top = result + ALIGN(size);
  if (clh->heap_top > clh->heap_end) result=NULL;
#ifdef WITH_THREADS
  flex_mutex_unlock(&(clh->heap_lock));
#endif
  return (void *) result;
}

clheap_t clheap_create() {
  clheap_t clh = (clheap_t) malloc(sizeof(*clh));
  clh->heap_start = clh->heap_top =
#ifdef BDW_CONSERVATIVE_GC
    GC_malloc_uncollectable
#else
    malloc
#endif
    (HEAPSIZE);
  clh->heap_end = clh->heap_start + HEAPSIZE;
  clh->use_count = 1;
#ifdef WITH_THREADS
  flex_mutex_init(&(clh->heap_lock));
#endif
  return clh;
}

/* race condition here if someone detaches before we attach */
clheap_t clheap_attach(clheap_t clh) {
  // XXX we should really be able to turn around and retry if someone
  // frees the heap before we get the lock.
#ifdef WITH_THREADS
  flex_mutex_lock(&(clh->heap_lock));
#endif
  clh->use_count++;
#ifdef WITH_THREADS
  flex_mutex_unlock(&(clh->heap_lock));
#endif
  return clh;
}

void clheap_detach(clheap_t clh) {
#ifdef WITH_THREADS
  flex_mutex_lock(&(clh->heap_lock));
#endif
  if (--clh->use_count > 0) {
    /* unlock and leave. */
#ifdef WITH_THREADS
    flex_mutex_unlock(&(clh->heap_lock));
#endif
    return;
  }
  /* deallocate this heap */
#ifdef BDW_CONSERVATIVE_GC
  GC_free
#else
  free
#endif
    (clh->heap_start);
#ifdef WITH_THREADS
  flex_mutex_destroy(&(clh->heap_lock));
#endif
  free(clh);
}
