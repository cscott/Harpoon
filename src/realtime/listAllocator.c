/* listAllocator.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "listAllocator.h"

inline struct ListAllocator* ListAllocator_new(int noHeap) {
  struct ListAllocator* ls = 
    (struct ListAllocator*)
#ifdef BDW_CONSERVATIVE_GC
    GC_malloc
#else
    malloc
#endif
    (sizeof(struct ListAllocator));
  ListAllocator_init(ls, noHeap);
  return ls;
}

inline void ListAllocator_init(struct ListAllocator* ls, int noHeap) {
  ls->heapRefs = ObjectList_new(INIT_LIST_HEAP_REF_SIZE);
  ls->noHeap = noHeap;
  ls->objects = ObjectList_new(INIT_LIST_ALLOCATOR_SIZE);
}

inline int ListAllocator_heapCheck(struct ListAllocator* ls, 
				   void** heapRef) {
  if (HeapMemory_contains(*heapRef)) {
    if (!ls->noHeap) {
      ObjectList_insert(ls->heapRefs, heapRef);
    }
    return 1;
  }
  return 0;
}

inline void* ListAllocator_alloc(struct ListAllocator* ls, 
				 size_t size) {
  void* obj = 
#ifdef BDW_CONSERVATIVE_GC
    GC_malloc_uncollectable
#else
    malloc
#endif
    (size);
  ObjectList_insert(ls->objects, obj);
  return obj;
}

inline void ListAllocator_free(struct ListAllocator* ls) {
#ifdef DEBUG
  printf("ObjectList_free(ls->heapRefs)\n");
#endif
  ObjectList_free(ls->heapRefs);
#ifdef DEBUG
  printf("ObjectList_freeRefs(ls->objects)\n");
#endif
  ObjectList_freeRefs(ls->objects);
#ifdef DEBUG
  printf("ObjectList_free(ls->objects)\n");
#endif
  ObjectList_free(ls->objects);
#ifdef DEBUG
  printf("free(ls)\n");
#endif
#ifdef BDW_CONSERVATIVE_GC
  GC_free
#else
    free
#endif
    (ls);
}
