/* RTJmalloc.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "RTJmalloc.h"

inline void* RTJ_malloc(size_t size) { 
  void* newPtr;
#ifdef DEBUG
  printf("RTJ_malloc(%d)\n", size);
#endif
  newPtr = RTJ_malloc_block(size, MemBlock_currentMemBlock()); 
#ifdef DEBUG
  printf("= %08x\n", (int)newPtr);
#endif
  return newPtr;
}

inline void* RTJ_malloc_block(size_t size, struct MemBlock* memBlock) {
#ifdef DEBUG
  printf("RTJ_malloc_block(%d)\n", size);
#endif
  return MemBlock_alloc(memBlock, size);
}

inline static void memBlock_free(void* memBlock) {
  /* Called on thread exit. */
#ifdef DEBUG  
  printf("memBlock_free()\n");
#endif
  MemBlock_free(memBlock);
}

inline static void memBlock_key_alloc() {
#ifdef DEBUG
  printf("memBlock_key_alloc()\n");
#endif
  pthread_key_create(&memBlock_key, memBlock_free);
}

inline struct MemBlock* MemBlock_currentMemBlock() {
  jobject thread;
  JNIEnv* env;
  struct MemBlock* memBlock;
#ifdef DEBUG
  printf("MemBlock_currentMemBlock()\n");
#endif
/*    return pthread_getspecific(memBlock_key); */
  thread = ((struct FNI_Thread_State *)(env = FNI_GetJNIEnv()))->thread;
  memBlock = getInflatedObject(env, thread)->memBlock;
  if (memBlock) {
    pthread_setspecific(memBlock_key, memBlock);
  }
  return memBlock;
}

inline void MemBlock_setCurrentMemBlock(JNIEnv* env,
					jobject realtimeThread,
					struct MemBlock* memBlock) {
#ifdef DEBUG
  printf("MemBlock_setCurrentMemBlock()\n");
#endif
  getInflatedObject(env, realtimeThread)->memBlock = memBlock;
}

inline void RTJ_preinit() {
#ifdef DEBUG
  printf("RTJ_preinit()\n");
#endif
  HeapMemory_init();
}

inline void RTJ_init() {
#ifdef DEBUG
  printf("RTJ_init()\n");
#endif
  pthread_once(&memBlock_key_once, memBlock_key_alloc);
  BlockAllocator_init();
  MemBlock_setCurrentMemBlock(FNI_GetJNIEnv(),
			      ((struct FNI_Thread_State *)
			       FNI_GetJNIEnv())->thread,
			      HeapMemory_RThread_MemBlock_new());
}

