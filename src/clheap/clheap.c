#include "config.h"
#include "clheap.h"	/* prototypes */
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"		/* GC_malloc_uncollectable, GC_free */
#endif
#include "flexthread.h"	/* flex_mutex_lock */
#include "misc.h"	/* for ALIGN, RECYCLE_HEAPS */
#include <stdlib.h>	/* for malloc, size_t */
#include "stats.h"	/* for thread_heaps_created */
#include "memstats.h"

#ifdef RECYCLE_HEAPS
static clheap_t heap_free_list = NULL;
FLEX_MUTEX_DECLARE_STATIC(heap_free_list_mutex);
#endif

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
  clheap_t clh = NULL;
#ifdef RECYCLE_HEAPS
  FLEX_MUTEX_LOCK(&heap_free_list_mutex);
  if (heap_free_list!=NULL) {
    /* take top off of free list */
    clh = heap_free_list;
    heap_free_list = clh->next;
  }
  FLEX_MUTEX_UNLOCK(&heap_free_list_mutex);
  if (clh!=NULL) goto finish_init;
#endif
  INCREMENT_STATS(thread_heaps_created, 1);

  INCREMENT_MEM_STATS(sizeof(*clh));

#if !defined(BDW_CONSERVATIVE_GC)
  INCREMENT_MEM_STATS(HEAPSIZE);
#endif

  clh = (clheap_t) malloc(sizeof(*clh));
  clh->heap_start =
#ifdef WITH_PRECISE_GC /* precise_malloc_uncollectable ? */
# error unimplemented
#elif defined(BDW_CONSERVATIVE_GC)
    GC_malloc_uncollectable
#else
    malloc
#endif
    (HEAPSIZE);
  clh->heap_end = clh->heap_start + HEAPSIZE;
#ifdef WITH_THREADS
  flex_mutex_init(&(clh->heap_lock));
#endif
 finish_init:
  clh->heap_top = clh->heap_start;
  clh->use_count = 1;
  return clh;
}

/* race condition here if someone detaches before we attach; be aware. */
void clheap_attach(clheap_t clh) {
  // application logic must guarantee that no one
  // frees the heap before we get the lock.
  FLEX_MUTEX_LOCK(&(clh->heap_lock));
  clh->use_count++;
  FLEX_MUTEX_UNLOCK(&(clh->heap_lock));
}

void clheap_detach(clheap_t clh) {
  int uc;
  FLEX_MUTEX_LOCK(&(clh->heap_lock));
  uc = --clh->use_count;
  FLEX_MUTEX_UNLOCK(&(clh->heap_lock));
  if (uc > 0) return; /* don't free; still in use. */

  /* deallocate this heap */
#ifdef BDW_CONSERVATIVE_GC
  /* first call all finalizers. */
  {
    GC_finalization_proc finfunc=NULL; void *findata;
    GC_register_finalizer(clh->heap_start,NULL,NULL,&finfunc,&findata);
    if (finfunc!=NULL) (*finfunc)(clh->heap_start, findata);
  }
#endif
#ifdef RECYCLE_HEAPS
  FLEX_MUTEX_LOCK(&heap_free_list_mutex);
  clh->next = heap_free_list;
  heap_free_list = clh;
  FLEX_MUTEX_UNLOCK(&heap_free_list_mutex);
#else /* !RECYCLE_HEAPS */
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

  DECREMENT_MEM_STATS(sizeof(*clh));
#if !defined(BDW_CONSERVATIVE_GC)
  DECREMENT_MEM_STATS(HEAPSIZE);
#endif

#endif
}
