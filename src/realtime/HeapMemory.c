/* HeapMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "HeapMemory.h"

/*
 * Class:     HeapMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_HeapMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size) {
#ifdef RTJ_DEBUG
  printf("HeapMemory.initNative(%p, %p, %d)\n", env, memoryArea, size);
#endif  
}

inline struct MemBlock* Heap_MemBlock_new(JNIEnv* env, jobject heapMem) {
  /* This function is called to create the initial MemBlock. */
  struct MemBlock* mb = MemBlock_new(env, heapMem);
  jclass heapClaz = (*env)->GetObjectClass(env, heapMem);
  jfieldID shadowID = (*env)->GetFieldID(env, heapClaz, "shadow",
					 "Ljavax/realtime/MemoryArea;");
  jobject shadow = (*env)->GetObjectField(env, heapMem, shadowID);
#ifdef RTJ_DEBUG
  checkException();
  printf("HeapMemory_RThread_MemBlock_new()\n");
#endif
  mb->alloc = Heap_MemBlock_alloc;
  MemBlock_INCREF(mb);
  mb->memoryArea = (*env)->NewGlobalRef(env, heapMem);
  getInflatedObject(env, shadow)->memBlock = mb;
  return mb;
}

void* Heap_MemBlock_alloc(struct MemBlock* mem, size_t size) {
#ifdef RTJ_DEBUG
  printf("Heap_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return (void*)RTJ_MALLOC(size);
}
