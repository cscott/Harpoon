/* listAllocator.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __LIST_ALLOCATOR_H__
#define __LIST_ALLOCATOR_H__

#include "objectList.h"
#include "heapMemory.h"

#ifdef WITH_PRECISE_GC
#include "jni-private.h"
#include "../gc/precise_gc.h"
#endif

#define INIT_LIST_ALLOCATOR_SIZE 1024

struct ListAllocator {
  struct ObjectList* objects;
  int noHeap;
};

inline struct ListAllocator* ListAllocator_new(int noHeap);
inline void ListAllocator_init(struct ListAllocator* ls, int noHeap);
inline void* ListAllocator_alloc(struct ListAllocator* ls, 
				 size_t size);
inline void ListAllocator_free(struct ListAllocator* ls);
inline void ListAllocator_gc(struct ListAllocator* ls);

#endif /* __LIST_ALLOCATOR_H__ */
