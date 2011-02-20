/* ScopedPhysicalMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "ScopedPhysicalMemory.h"

/*
 * Class:     ScopedPhysicalMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {
  
}

JNIEXPORT void JNICALL Java_javax_realtime_ScopedPhysicalMemory_initNative_00024_00024initcheck
(JNIEnv* env, jobject memoryArea, jlong size) {
#ifdef RTJ_DEBUG
  printf("\nScopedPhysicalMemory.initNative_initcheck");
#endif
  Java_javax_realtime_ScopedPhysicalMemory_initNative(env, memoryArea, size);
}

void* ScopedPhysical_MemBlock_alloc(struct MemBlock* mem, size_t size) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return NULL;
}

void  ScopedPhysical_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_free()\n");
#endif
}

#ifdef WITH_PRECISE_GC
void  ScopedPhysical_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_gc(%p)\n", mem);
#endif
}
#endif

