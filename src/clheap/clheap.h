/* clustered heap data structure */

#ifndef INCLUDED_CLHEAP_H
#define INCLUDED_CLHEAP_H

#include "config.h"
#ifdef WITH_HEAVY_THREADS
#include <pthread.h>
#endif

typedef struct clustered_heap {
  char *heap_start, *heap_top, *heap_end;

  /* reference counting */
  int use_count;

#ifdef WITH_HEAVY_THREADS
  pthread_mutex_t heap_lock;
#endif
} * clheap_t;

#endif /* INCLUDED_CLHEAP_H */
