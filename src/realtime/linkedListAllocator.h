/* linkedListAllocator.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __LINKED_LIST_ALLOCATOR_H__
#define __LINKED_LIST_ALLOCATOR_H__
#include "RTJfinalize.h"
#include "RTJconfig.h"
#include "asm/atomicity.h"

struct cons {
  struct oobj* car;
  struct cons* cdr;
};

struct linkedListAllocator {
  struct cons* data;
  long int used;
  long int size;
};

typedef struct linkedListAllocator* LListAllocator; 

inline LListAllocator LListAllocator_new(size_t size);
inline void* LListAllocator_alloc(LListAllocator ls, size_t size);
inline void  LListAllocator_free(LListAllocator ls);

#ifdef WITH_PRECISE_GC
inline void  LListAllocator_gc(LListAllocator ls);
#endif
#endif
