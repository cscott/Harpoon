/* linkedListAllocator.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "linkedListAllocator.h"

/** NOT thread safe */

inline void* LListAllocator_alloc(LListAllocator ls, size_t size) {
  struct cons* newCons = 
    (struct cons*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct cons));
  newCons->car = (struct oobj*)RTJ_MALLOC_UNCOLLECTABLE(size);
  newCons->cdr = *ls;
  *ls = newCons;
  return (void*)(newCons->car);
}

inline void LListAllocator_free(LListAllocator ls) {
  struct cons* current = *ls;
  while (current!=NULL) {
    struct cons* old = current;
    current = current->cdr;
    RTJ_finalize(old->car);
    RTJ_FREE(old->car);
    if (old!=(*ls)) RTJ_FREE(old);
  }
}

#ifdef WITH_PRECISE_GC
inline void LListAllocator_gc(LListAllocator ls) {
  struct cons* current = *ls;
  while (current!=NULL) {
    trace(current->car);
    current = current->cdr;
  }
}
#endif
