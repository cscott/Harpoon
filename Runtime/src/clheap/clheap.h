/* clustered heap data structure */

#ifndef INCLUDED_CLHEAP_H
#define INCLUDED_CLHEAP_H

#include <stdlib.h> /* for size_t */
#include "config.h" /* for WITH_THREADS */
#include "flexthread.h" /* for flex_mutex_t */
#include "misc.h" /* for RECYCLE_HEAPS */

typedef struct clustered_heap {
  char *heap_start, *heap_top, *heap_end;

  /* reference counting */
  int use_count;

#ifdef WITH_THREADS
  flex_mutex_t heap_lock;
#endif

#ifdef RECYCLE_HEAPS
  /* for keeping a free list. */
  struct clustered_heap *next;
#endif
} * clheap_t;

void *clheap_alloc(clheap_t clh, size_t size);
clheap_t clheap_create(void);
void clheap_attach(clheap_t clh);
void clheap_detach(clheap_t clh);

#endif /* INCLUDED_CLHEAP_H */
