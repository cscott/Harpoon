/* listAllocator.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "objectList.h"
#include "heapMemory.h"

#ifndef __LIST_ALLOCATOR_H__
#define __LIST_ALLOCATOR_H__

#define INIT_LIST_HEAP_REF_SIZE 1
#define INIT_LIST_ALLOCATOR_SIZE 1024

struct ListAllocator {
  struct ObjectList* heapRefs;
  struct ObjectList* objects;
  int noHeap;
};

inline struct ListAllocator* ListAllocator_new(int noHeap);
inline void ListAllocator_init(struct ListAllocator* ls, int noHeap);
inline int ListAllocator_heapCheck(struct ListAllocator* ls, 
				   void** heapRef);
inline void* ListAllocator_alloc(struct ListAllocator* ls, 
				 size_t size);
inline void ListAllocator_free(struct ListAllocator* ls);

#endif /* __LIST_ALLOCATOR_H__ */
