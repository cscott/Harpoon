/* linkedListAllocator.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "linkedListAllocator.h"

inline LListAllocator LListAllocator_new(size_t size) {
  LListAllocator ls = (LListAllocator)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct linkedListAllocator));
  ls->data = NULL;
  ls->used = 0;
  ls->size = (long int)size;
  return ls;
}

inline void* LListAllocator_alloc(LListAllocator ls, size_t size) {
  struct cons* newCons; 
  if (ls->size && 
      (exchange_and_add((uint32_t*)(&(ls->used)), size)>(ls->size))) {
    return NULL; /* Out of space. */
  }
  newCons = (struct cons*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct cons));
  newCons->car = (struct oobj*)RTJ_MALLOC_UNCOLLECTABLE(size);
#ifdef WITH_PRECISE_GC
  ((struct oobj*)(newCons->car))->claz = NULL;
#endif
  while (!compare_and_swap((long int*)(&(ls->data)), 
			   (long int)(newCons->cdr = ls->data), 
			   (long int)newCons)) {}
  return (void*)(newCons->car);
}

inline void LListAllocator_free(LListAllocator ls) {
  struct cons* current = ls->data;
  while (current) {
    struct cons* old = current;
    current = current->cdr;
    RTJ_finalize(old->car);
    RTJ_FREE(old->car);
    RTJ_FREE(old);
  }
  ls->data = NULL;
}

#ifdef WITH_PRECISE_GC
inline void LListAllocator_gc(LListAllocator ls) {
  struct cons* current = ls->data;
  while (current) {
    if (current->car->claz) trace(current->car);
    current = current->cdr;
  }
}
#endif
