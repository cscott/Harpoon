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
(JNIEnv* env, jobject memoryArea, jlong size) {
  struct MemBlock* mb = (struct MemBlock*)
#ifdef BDW_CONSERVATIVE_GC
#ifdef WITH_GC_STATS
    GC_malloc_uncollectable_stats
#else
    GC_malloc_uncollectable
#endif
#else
    malloc
#endif
    (sizeof(struct MemBlock));
#ifdef RTJ_DEBUG
  printf("CTMemory.initNative(%d)\n", (size_t)size);
#endif
  getInflatedObject(env, memoryArea)->memBlock = mb;
  mb->block = Block_new(memoryArea, (size_t)size);
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
  struct MemBlock* mb = getInflatedObject(env, realtimeThread)->temp;
  struct BlockInfo* bi = mb->block_info;
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
  mb->block = getInflatedObject(env, memoryArea)->memBlock->block;
#ifdef RTJ_DEBUG
  printf("  retrieving memBlock from %08x\n", mb->block);
#endif
}


