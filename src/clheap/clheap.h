/* clustered heap data structure */

#ifndef INCLUDED_CLHEAP_H
#define INCLUDED_CLHEAP_H

#include "config.h"
#include "flexthread.h"

typedef struct clustered_heap {
  char *heap_start, *heap_top, *heap_end;

  /* reference counting */
  int use_count;

#ifdef WITH_THREADS
  flex_mutex_t heap_lock;
#endif
} * clheap_t;

#endif /* INCLUDED_CLHEAP_H */
