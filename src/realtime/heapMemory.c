/* heapMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "heapMemory.h"

inline void HeapMemory_init() {
/*    heapPtrs = ObjectList_new(HEAP_SIZE); */
/*    heapRefs = ObjectList_new(HEAP_REFS); */
}

inline void* HeapMemory_alloc(size_t size) {
  void* result =
#ifdef BDW_CONSERVATIVE_GC
#ifdef WITH_GC_STATS
    GC_malloc_stats
#else
    GC_malloc
#endif
#else
    malloc
#endif
    (size);
/*    HeapMemory_register(result); */
  return result;
}

inline void HeapMemory_register(void* heapPtr) {
  ObjectList_insert(heapPtrs, heapPtr);
}

inline void HeapMemory_registerRef(void** heapRef) {
  ObjectList_insert(heapRefs, heapRef);
}

inline void HeapMemory_free(void* ptr) {
/*    ObjectList_delete(heapPtrs, ptr);  */
/* Register as finalizer? */ 
/*    GC_free(ptr); */
}

inline int HeapMemory_contains(void* ptr) {
  return ObjectList_contains(heapPtrs, ptr);
}

inline void HeapMemory_freeAll() {
#ifdef BDW_CONSERVATIVE_GC
/*    GC_free(heapPtrs); */
/*    GC_free(heapRefs); */
#else
/*    free(heapPtrs); */
/*    free(heapRefs); */
#endif
  /* At end of program - register w/pthreads... */
}
