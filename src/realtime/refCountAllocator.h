/* refCountAllocator.h, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#ifndef __REF_COUNT_ALLOCATOR_H__
#define __REF_COUNT_ALLOCATOR_H__
#include "RTJfinalize.h"
#include "RTJconfig.h"
#include "asm/atomicity.h"
#include "MemBlock.h"
#include "../gc/trace.h"

struct refCountAllocator;

struct refCons {
  uint32_t refCount;
  struct refCons *next, *nextFree;
  void (*finalize)(struct refCountAllocator* rc, void* obj);
  char obj[0];
};

struct refCountAllocator {
  uint32_t refCount; 
  uint32_t cleaning;
  struct refCons *in_use, **scan, *freeList, *collectable;
};

struct accum {
  void* acc;
  int stop;
};

#define REF_HEADER(obj) ((struct refCons*)(((void*)obj)-(sizeof(struct refCons))))

typedef struct refCountAllocator* RefCountAllocator;

inline RefCountAllocator RefCountAllocator_new();
inline void* RefCountAllocator_alloc(RefCountAllocator rc, size_t size, int clear);
inline void  RefCountAllocator_free(RefCountAllocator rc);

inline void  RefCountAllocator_INC(RefCountAllocator rc);
inline void  RefCountAllocator_DEC(RefCountAllocator rc);
inline long int RefCountAllocator_INCREF(void* obj);
inline void RefCountAllocator_DECREF(RefCountAllocator rc, void* obj);

inline void* RefCountAllocator_accumulate(RefCountAllocator rc, 
					  void (*visitor)(void* obj, 
							  struct accum* accum));
#ifdef WITH_PRECISE_GC
inline void  RefCountAllocator_gc(RefCountAllocator rc);
inline void  RefCountAllocator_gc_visit(void* obj, struct accum* accum);
#endif

inline void  RefCountAllocator_register_finalizer(void* obj, 
						  void (*finalize)(RefCountAllocator rc, void* obj));
inline void RefCountAllocator_oobj_finalizer(RefCountAllocator rc, void* obj);
inline void RefCountAllocator_oobj_handle_reference(RefCountAllocator rc, 
						    struct oobj** obj);
inline void RefCountAllocator_oobj_trace(RefCountAllocator rc,
					 jobject_unwrapped unaligned_ptr);
#endif
