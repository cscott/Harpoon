/* linkedListAllocator.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __LINKED_LIST_ALLOCATOR_H__
#define __LINKED_LIST_ALLOCATOR_H__
#include "MemBlock.h"
#include "RTJfinalize.h"

struct cons {
  struct oobj* car;
  struct cons* cdr;
};

typedef struct cons** LListAllocator; 

inline void* LListAllocator_alloc(LListAllocator ls, size_t size);
inline void  LListAllocator_free(LListAllocator ls);

#ifdef WITH_PRECISE_GC
inline void  LListAllocator_gc(LListAllocator ls);
#endif
#endif
