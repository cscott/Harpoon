#include "config.h"
#include "clheap.h"
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#ifdef WITH_HEAVY_THREADS
#include <pthread.h>
#endif

#define HEAPSIZE 65536
#define ALIGNMENT 4
#define ALIGN(x) (((x)+(ALIGNMENT-1)) & ~(ALIGNMENT-1))

void *clheap_alloc(clheap_t clh, int size) {
  char *result;
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_lock(&(clh->heap_lock));
#endif
  result = clh->heap_top;
  clh->heap_top = result + ALIGN(size);
  if (clh->heap_top > clh->heap_end) result=NULL;
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_unlock(&(clh->heap_lock));
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
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_init(&(clh->heap_lock), NULL);
#endif
}

/* race condition here if someone detaches before we attach */
clheap_t clheap_attach(clheap_t clh) {
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_lock(&(clh->heap_lock));
#endif
  clh->use_count++;
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_unlock(&(clh->heap_lock));
#endif
}

void clheap_detach(clheap_t clh) {
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_lock(&(clh->heap_lock));
#endif
  if (--clh->use_count > 0) {
    /* unlock and leave. */
#ifdef WITH_HEAVY_THREADS
    pthread_mutex_unlock(&(clh->heap_lock));
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
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_destroy(&(clh->heap_lock));
#endif
  free(clh);
}
