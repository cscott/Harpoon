/* heapMemory.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __HEAP_MEMORY_H__
#define __HEAP_MEMORY_H__

#include "RTJconfig.h"
#include "objectList.h"
#include "gc.h"

inline void HeapMemory_init();
inline void* HeapMemory_alloc(size_t size);
inline void HeapMemory_free(void* ptr);


#endif /* __HEAP_MEMORY_H__ */
