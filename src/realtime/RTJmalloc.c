/* RTJmalloc.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "RTJmalloc.h"

void* RTJ_jmalloc(jsize size) {
  return RTJ_malloc((size_t)size);
}

#ifdef RTJ_DEBUG_REF
inline void* RTJ_malloc_ref(size_t size, const int line, const char *file) {
#else
inline void* RTJ_malloc(size_t size) { 
#endif
  void* newPtr;
#ifdef RTJ_DEBUG_REF
  struct MemBlock* memBlock; 
  struct PTRinfo* newInfo;
#endif
#ifdef RTJ_DEBUG
  checkException();
#ifndef RTJ_DEBUG_REF
  printf("RTJ_malloc(%d)\n", size);
#endif
#endif
#ifdef RTJ_DEBUG_REF
  printf("RTJ_malloc(%d) at %s:%d\n", size, file, line);
#endif
  newPtr = MemBlock_alloc(
#ifdef RTJ_DEBUG_REF
    memBlock = 
#endif
    MemBlock_currentMemBlock(), size); 
#ifdef RTJ_DEBUG
  printf("= 0x%08x\n", (int)newPtr);
#endif
#ifdef RTJ_DEBUG_REF
  if (memBlock) {
    newInfo = (struct PTRinfo*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct PTRinfo));
    newInfo->file = (char*)file;
    newInfo->line = (int)line;
    newInfo->size = size;
    newInfo->ptr = newPtr;
    
    flex_mutex_lock(&memBlock->ptr_info_lock);
    newInfo->next = memBlock->ptr_info;
    memBlock->ptr_info = newInfo;
    flex_mutex_unlock(&memBlock->ptr_info_lock);
  } else {
    printf("No memBlock yet, PTR info is lost\n");
  }
#endif
  return newPtr;
}

inline struct MemBlock* MemBlock_currentMemBlock() {
  jobject thread;
  JNIEnv* env;
#ifdef RTJ_DEBUG
  printf("MemBlock_currentMemBlock()\n");
  checkException();
#endif
  thread = ((struct FNI_Thread_State *)(env = FNI_GetJNIEnv()))->thread;
  return getInflatedObject(env, thread)->memBlock;
}

inline void MemBlock_setCurrentMemBlock(JNIEnv* env,
					jobject realtimeThread,
					struct MemBlock* memBlock) {
#ifdef RTJ_DEBUG
  printf("MemBlock_setCurrentMemBlock()\n");
  checkException();
#endif
  getInflatedObject(env, realtimeThread)->memBlock = memBlock;
}

inline void RTJ_preinit() {
#ifdef RTJ_DEBUG
  printf("RTJ_preinit()\n");
  checkException();
#endif
#ifdef BDW_CONSERVATIVE_GC
  GC_finalize_on_demand = 1;
#endif
#ifdef RTJ_DEBUG_REF
  flex_mutex_init(&ptr_info_lock);
#endif
  HeapMemory_init();
}

inline void RTJ_init() {
  JNIEnv* env = FNI_GetJNIEnv();
#ifdef RTJ_DEBUG
  printf("RTJ_init()\n");
  checkException();
#endif
#ifdef RTJ_DEBUG_REF
  flex_mutex_init(&ptr_info_lock);
#endif
  BlockAllocator_init();
#ifdef RTJ_DEBUG
  checkException();
#endif
  MemBlock_setCurrentMemBlock(env,
			      ((struct FNI_Thread_State *)env)->thread,
			      HeapMemory_RThread_MemBlock_new());
#ifdef RTJ_DEBUG
  checkException();
#endif
}

