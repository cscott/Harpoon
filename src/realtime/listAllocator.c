/* listAllocator.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "listAllocator.h"

inline struct ListAllocator* ListAllocator_new(int noHeap) {
  struct ListAllocator* ls = (struct ListAllocator*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct ListAllocator));
  ListAllocator_init(ls, noHeap);
  return ls;
}

inline void ListAllocator_init(struct ListAllocator* ls, int noHeap) {
  ls->noHeap = noHeap;
  ls->objects = ObjectList_new(INIT_LIST_ALLOCATOR_SIZE);
}

inline void* ListAllocator_alloc(struct ListAllocator* ls, 
				 size_t size) {
  struct oobj* obj = (struct oobj*)RTJ_MALLOC_UNCOLLECTABLE(size);
  obj->claz = NULL;
  ObjectList_insert(ls->objects, obj);
  return (void*)obj;
}

inline void ListAllocator_free(struct ListAllocator* ls) {
#ifdef RTJ_DEBUG
  printf("ObjectList_freeRefs(ls->objects)\n");
#endif
  ObjectList_freeRefs(ls->objects);
#ifdef RTJ_DEBUG
  printf("ObjectList_free(ls->objects)\n");
#endif
  ObjectList_free(ls->objects);
#ifdef RTJ_DEBUG
  printf("free(ls)\n");
#endif
  RTJ_FREE(ls);
}

#ifdef WITH_PRECISE_GC
inline void ListAllocator_gc(struct ListAllocator* ls) {
  ObjectList_scan(ls->objects);
}
#endif
