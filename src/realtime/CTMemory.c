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
  printf("CTMemory.initNative(0x%08x, 0x%08x, %d, %d)\n", 
	 env, memoryArea, (size_t)size, reuse);
  checkException();
#endif
  getInflatedObject(env, memoryArea)->memBlock = mb;
  mb->block = Block_new(memoryArea, (size_t)size);
  mb->ref_info = RefInfo_new(reuse == JNI_TRUE);
  mb->next = NULL;
  flex_mutex_init(&(mb->list_lock));
#ifdef WITH_PRECISE_GC
  add_MemBlock_to_roots(mb);
#endif
#ifdef RTJ_DEBUG
  checkException();
  printf("  storing MemBlock in 0x%08x\n", mb->block); 
#endif
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
  struct MemBlock* current;
  checkException();
  printf("CTMemory.newMemBlock(0x%08x, 0x%08x, 0x%08x)\n", env, memoryArea, 
	 realtimeThread);
#endif
#ifdef WITH_NOHEAP_SUPPORT
  if (IsNoHeapRealtimeThread(env, realtimeThread)) {
    bi->alloc     = CTScope_NoHeapRThread_MemBlock_alloc;
    bi->free      = CTScope_NoHeapRThread_MemBlock_free;
    bi->allocator = (void*)mamb;
#ifdef WITH_PRECISE_GC
    bi->gc        = NULL;
#endif
  } else {
#endif
    bi->alloc     = CTScope_RThread_MemBlock_alloc;
    bi->free      = CTScope_RThread_MemBlock_free;
    bi->allocator = (void*)mamb;
#ifdef WITH_PRECISE_GC
    bi->gc        = CTScope_RThread_MemBlock_gc;
#endif
#ifdef WITH_NOHEAP_SUPPORT
  }
#endif
  rtmb->block = mamb->block;
  rtmb->ref_info = mamb->ref_info;
#ifdef RTJ_DEBUG
  checkException();
  printf("  retrieving memBlock from 0x%08x\n", mamb->block);
#endif
  flex_mutex_lock(&(mamb->list_lock));
  rtmb->next = mamb->next;
  mamb->next = rtmb;
#ifdef RTJ_DEBUG
  current = mamb;
  printf("  MemBlock list for current CTMemory: ");
  while (current != NULL) {
    printf("0x%08x, ", current);
    current = current->next;
  }
  printf("\n");
#endif
  flex_mutex_unlock(&(mamb->list_lock));
}

void CTScope_freeAll(struct MemBlock* topmem) {
  struct MemBlock* current;
  JNIEnv* env = FNI_GetJNIEnv();
#ifdef WITH_PRECISE_GC
  remove_MemBlock_from_roots(topmem);
#endif      
  current = topmem->next;
  while (current != NULL) {
    struct MemBlock* next = current->next;
    (*env)->DeleteGlobalRef(env, current->block_info->memoryArea);
    (*env)->DeleteGlobalRef(env, current->block_info->realtimeThread);
    RTJ_FREE(current->block_info);
    RTJ_FREE(current);
    current = next;
  }
  flex_mutex_destroy(&(topmem->list_lock));
  Block_free(topmem->block);
  RTJ_FREE(topmem->ref_info);
  RTJ_FREE(topmem);
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
  checkException();
  printf("CTMemory.doneNative(0x%08x, 0x%08x)\n", env, memoryArea);
#endif
  if (mb->ref_info->reuse) {
    mb->ref_info->reuse = 0;
    if (!mb->ref_info->refCount) {
      CTScope_freeAll(mb);
    } 
#ifdef RTJ_DEBUG
    else {
      printf("  Not freeing memory now because refCount = %d\n", mb->ref_info->refCount);
    }
#endif
  }
}

void* CTScope_RThread_MemBlock_alloc(struct MemBlock* mem, 
				     size_t size) {
#ifdef RTJ_DEBUG
  void* ptr;
  checkException();
  printf("CTScope_RThread_MemBlock_alloc(0x%08x, %d)\n", mem, size);
  printf("  current usage: %d of %d\n", 
	 (size_t)((mem->block->free)-(mem->block->begin)),
	 (size_t)((mem->block->end)-(mem->block->begin)));
  printf("  begin: 0x%08x, free: 0x%08x, end: 0x%08x\n", 
	 mem->block->begin, mem->block->free, mem->block->end);
  printf("  retrieving memBlock from 0x%08x\n", mem->block);
#ifdef WITH_NOHEAP_SUPPORT
  ptr = Block_alloc(mem->block, size, 0);
#else
  ptr = Block_alloc(mem->block, size);
#endif
  printf("  begin: 0x%08x, free: 0x%08x, end: 0x%08x\n", 
	 mem->block->begin, mem->block->free, mem->block->end);
  if (!ptr) {
    assert("Out of memory!!!");
    checkException();
  }
  return ptr;
#else
#ifdef WITH_NOHEAP_SUPPORT
  return Block_alloc(mem->block, size, 0);
#else
  return Block_alloc(mem->block, size);
#endif
#endif
}

void  CTScope_RThread_MemBlock_free(struct MemBlock* mem) {
  struct MemBlock *topmem, *current;
#ifdef RTJ_DEBUG
  printf("CTScope_RThread_MemBlock_free(0x%08x)\n", mem);
#endif

  if (mem->ref_info->reuse) {
#ifdef RTJ_DEBUG
    printf("  resetting block... \n");
#endif
    Block_reset(mem->block);
  } else {
#ifdef RTJ_DEBUG
    printf("  freeing block... \n");
#endif
    CTScope_freeAll((struct MemBlock*)(mem->block_info->allocator));
  }
}

#ifdef WITH_PRECISE_GC
void  CTScope_RThread_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("CTScope_RThread_MemBlock_gc(0x%08x)\n", mem);
#endif
  Block_scan(mem->block);
}
#endif

#ifdef WITH_NOHEAP_SUPPORT
void* CTScope_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
					   size_t size) {
#ifdef RTJ_DEBUG
  void* ptr;
  checkException();
  printf("CTScope_NoHeapRThread_MemBlock_alloc(0x%08x, %d)\n", mem, size);
  printf("  current usage: %d of %d\n",
	 (size_t)((mem->block->free)-(mem->block->begin)),
	 (size_t)((mem->block->end)-(mem->block->begin)));
  printf("  begin: 0x%08x, free: 0x%08x, end: 0x%08x\n",
	 mem->block->begin, mem->block->free, mem->block->end);
  printf("  retreiving memBlock from 0x%08x\n", mem->block);
  ptr = Block_alloc(mem->block, size, 1);
  printf("  begin: 0x%08x, free: 0x%08x, end: 0x%08x\n",
	 mem->block->begin, mem->block->free, mem->block->end);
  if (!ptr) {
    assert("Out of memory!!!");
    checkException();
  }
  return ptr; 
#else
  return Block_alloc(mem->block, size, 1);
#endif
}

void  CTScope_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
  struct MemBlock *topmem, *current;
#ifdef RTJ_DEBUG
  printf("CTScope_NoHeapRThread_MemBlock_free()\n");
#endif

  if (mem->ref_info->reuse) {
#ifdef RTJ_DEBUG
    printf("  resetting block... \n");
#endif
    Block_reset(mem->block);
  } else {
#ifdef RTJ_DEBUG
    printf("  freeing block... \n");
#endif
    CTScope_freeAll((struct MemBlock*)(mem->block_info->allocator));
  }
}
#endif
