/* CTMemory.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "CTMemory.h"

/*
 * Class:     CTMemory
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_initNative
(JNIEnv* env, jobject memoryArea, jlong size, jboolean reuse) {
  struct MemBlock* mb = (struct MemBlock*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct MemBlock));
#ifdef RTJ_DEBUG
  printf("CTMemory.initNative(%d)\n", (size_t)size);
#endif
  getInflatedObject(env, memoryArea)->memBlock = mb;
  mb->block = Block_new(memoryArea, (size_t)size);
  mb->ref_info = RefInfo_new(reuse == JNI_TRUE);
#ifdef RTJ_DEBUG
  printf("  storing MemBlock in %08x\n", mb->block); 
#endif RTJ_DEBUG
}

/*
 * Class:     CTMemory
 * Method:    newMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_newMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread) {
  struct MemBlock* rtmb = getInflatedObject(env, realtimeThread)->temp;
  struct MemBlock* mamb = getInflatedObject(env, memoryArea)->memBlock;
  struct BlockInfo* bi = rtmb->block_info;
#ifdef RTJ_DEBUG
  printf("CTMemory.newMemBlock(%08x, %08x, %08x)\n", env, memoryArea, 
	 realtimeThread);
#endif
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = CTScope_NoHeapRThread_MemBlock_alloc;
    bi->free      = CTScope_NoHeapRThread_MemBlock_free;
    bi->allocator = CTScope_NoHeapRThread_MemBlock_allocator(memoryArea);
  } else {
    bi->alloc     = CTScope_RThread_MemBlock_alloc;
    bi->free      = CTScope_RThread_MemBlock_free;
    bi->allocator = CTScope_RThread_MemBlock_allocator(memoryArea);
  }
  rtmb->block = mamb->block;
  rtmb->ref_info = mamb->ref_info;
#ifdef RTJ_DEBUG
  printf("  retrieving memBlock from %08x\n", mamb->block);
#endif
}

/*
 * Class:     CTMemory
 * Method:    doneNative
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_CTMemory_doneNative
(JNIEnv* env, jobject memoryArea) {
  struct MemBlock* mb = getInflatedObject(env, memoryArea)->memBlock;
#ifdef RTJ_DEBUG
  printf("CTMemory.doneNative()\n");
#endif
  if (!mb->ref_info->refCount) {
    Block_free(mb->block);
    RTJ_FREE(mb->block_info);
  } else {
    mb->ref_info->reuse = 0;
  }
}

