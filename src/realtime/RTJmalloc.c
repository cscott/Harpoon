/* RTJmalloc.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "RTJmalloc.h"

inline void* RTJ_malloc(size_t size) { 
  void* newPtr;
#ifdef DEBUG
  printf("RTJ_malloc(%d)\n", size);
#endif
  newPtr = MemBlock_alloc(MemBlock_currentMemBlock(), size); 
#ifdef DEBUG
  printf("= %08x\n", (int)newPtr);
#endif
  return newPtr;
}

inline void* RTJ_malloc_block(size_t size, 
			      struct MemBlock* memBlock) {
#ifdef DEBUG
  printf("RTJ_malloc_block(%d)\n", size);
#endif
  return MemBlock_alloc(memBlock, size);
}

inline struct MemBlock* MemBlock_currentMemBlock() {
  jobject thread;
  JNIEnv* env;
#ifdef DEBUG
  printf("MemBlock_currentMemBlock()\n");
#endif
  thread = ((struct FNI_Thread_State *)(env = FNI_GetJNIEnv()))->thread;
  return getInflatedObject(env, thread)->memBlock;
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
  BlockAllocator_init();
  MemBlock_setCurrentMemBlock(FNI_GetJNIEnv(),
			      ((struct FNI_Thread_State *)
			       FNI_GetJNIEnv())->thread,
			      HeapMemory_RThread_MemBlock_new());
}

