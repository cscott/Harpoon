/* heapMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "heapMemory.h"

inline void HeapMemory_init() {
}

inline void* HeapMemory_alloc(size_t size) {
  return (void*)RTJ_MALLOC(size);
}

inline void HeapMemory_free(void* ptr) {
}

inline void HeapMemory_freeAll() {
}

