/* VTMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "VTMemory.h"

/*
 * Class:     VTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_VTMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {
  struct MemBlock* mb       = MemBlock_new(env, memoryArea);
  mb->alloc_union.lls       = LListAllocator_new(size);
  mb->alloc                 = VTScope_MemBlock_alloc;
  mb->free                  = VTScope_MemBlock_free;
  mb->finalize              = VTScope_MemBlock_finalize;
#ifdef WITH_PRECISE_GC
  mb->gc                    = VTScope_MemBlock_gc; /* must be the last set */
#endif
}

void* VTScope_MemBlock_alloc(struct MemBlock* mem, size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_MemBlock_alloc(%p, %d)\n", mem, (int)size);
#endif
  return LListAllocator_alloc(mem->alloc_union.lls, size);
}

void  VTScope_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_MemBlock_free(%p)\n", mem);
#endif
  LListAllocator_free(mem->alloc_union.lls);
}

#ifdef WITH_PRECISE_GC
void  VTScope_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_MemBlock_gc(%p)\n", mem);
#endif
  LListAllocator_gc(mem->alloc_union.lls);
}
#endif

void  VTScope_MemBlock_finalize(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("VTScope_MemBlock_finalize(%p)\n", mem);
#endif
  RTJ_FREE(mem->alloc_union.lls);
  mem->alloc_union.lls = NULL;
}
