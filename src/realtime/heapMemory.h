/* heapMemory.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __HEAP_MEMORY_H__
#define __HEAP_MEMORY_H__

#include "objectList.h"
#include "gc.h"

#define HEAP_SIZE 100
#define HEAP_REFS 100

struct ObjectList* heapPtrs;
struct ObjectList* heapRefs;

inline void HeapMemory_init();
inline void* HeapMemory_alloc(size_t size);
inline void HeapMemory_register(void* heapPtr);
inline void HeapMemory_registerRef(void** heapRef);
inline void HeapMemory_free(void* ptr);
inline void HeapMemory_freeAll();
inline int HeapMemory_contains(void* ptr);


#endif /* __HEAP_MEMORY_H__ */
