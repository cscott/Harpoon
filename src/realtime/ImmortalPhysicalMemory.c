/* ImmortalPhysicalMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "ImmortalPhysicalMemory.h"

/*
 * Class:     ImmortalPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ImmortalPhysicalMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {

}

void* ImmortalPhysical_MemBlock_alloc(struct MemBlock* mem, 
					      size_t size) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return NULL;
}

void  ImmortalPhysical_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_RThread_MemBlock_free()\n");
#endif
}

inline Allocator ImmortalPhysical_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

#ifdef WITH_PRECISE_GC
void  ImmortalPhysical_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("ImmortalPhysical_RThread_MemBlock_gc(%p)\n", mem);
#endif
}
#endif
