/* MemBlock.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "MemBlock.h"

struct RefInfo* RefInfo_new(int reuse) {
  struct RefInfo* ri = (struct RefInfo*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct RefInfo));
#ifdef RTJ_DEBUG
  printf("%08x = RefInfo_new(%d)\n", ri, reuse);
#endif
  ri->refCount = 0;
  ri->reuse = reuse;
  return ri;
}

struct MemBlock* MemBlock_new(JNIEnv* env, 
			      jobject memoryArea, 
			      jobject realtimeThread,
			      struct MemBlock* superBlock) {
  struct MemBlock* mb = RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct MemBlock));
  struct BlockInfo* bi = RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct BlockInfo));
  jclass memoryAreaClass = 
    (*env)->GetObjectClass(env, (mb->block_info = bi)->memoryArea = memoryArea);
  jclass realtimeThreadClass = 
    (*env)->GetObjectClass(env, bi->realtimeThread = realtimeThread);
  jmethodID methodID = (*env)->GetMethodID(env, memoryAreaClass, 
					   "newMemBlock",
					   "(Ljavax/realtime/RealtimeThread;)V");
#ifdef RTJ_DEBUG
  printf("MemBlock_new(%s, %s, %08x)\n", 
	 FNI_GetClassInfo(memoryAreaClass)->name, 
	 FNI_GetClassInfo(realtimeThreadClass)->name,
	 superBlock);
  printf("  methodID: %08x\n", methodID);
  checkException(env);
#endif
  bi->superBlock = superBlock;
  while (superBlock != NULL) {
    MemBlock_INCREF(superBlock);
    assert(superBlock != superBlock->block_info->superBlock);
    superBlock = superBlock->block_info->superBlock;
  }
  getInflatedObject(env, realtimeThread)->temp = mb;
  (*env)->CallVoidMethod(env, memoryArea, methodID, realtimeThread);
/*    (*env)->DeleteLocalRef(env, memoryAreaClass); */
/*    (*env)->DeleteLocalRef(env, realtimeThreadClass); */
  MemBlock_INCREF(mb);
  return mb;
}

inline struct inflated_oobj* getInflatedObject(JNIEnv* env, 
					       jobject obj) {
#ifdef RTJ_DEBUG
  printf("getInflatedObject(%08x, %08x)\n", env, obj);
#endif
  if (!FNI_IS_INFLATED(obj)) {
#ifdef RTJ_DEBUG
    printf("  inflating...\n");
#endif
    FNI_InflateObject(env, obj);
  }
  return FNI_UNWRAP(obj)->hashunion.inflated;
}

inline int IsNoHeapRealtimeThread(JNIEnv *env, 
				  jobject realtimeThread) {
#ifdef RTJ_DEBUG  
  printf("IsNoHeapRealtimeThread(%08x, %08x)\n", env, realtimeThread);
  checkException(env);
#endif
  return (*env)->IsInstanceOf(env, realtimeThread, 
			      (*env)->
			      FindClass(env, 
					"javax/realtime/NoHeapRealtimeThread")) 
    == JNI_TRUE;
}

inline void checkException(JNIEnv *env) {
  if ((*env)->ExceptionOccurred(env)) {
    (*env)->ExceptionDescribe(env);
  }
}

inline struct MemBlock* HeapMemory_RThread_MemBlock_new() {
  /* This function is called to create the initial MemBlock. */
  struct MemBlock* memBlock = 
    (struct MemBlock*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct MemBlock));
  struct BlockInfo* bi = (struct BlockInfo*)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct BlockInfo));
#ifdef RTJ_DEBUG
  printf("HeapMemory_RThread_MemBlock_new()\n");
#endif
  memBlock->block_info = bi;
  bi->memoryArea = NULL;
  bi->realtimeThread = NULL;
  bi->alloc = Heap_RThread_MemBlock_alloc;
  bi->free = Heap_RThread_MemBlock_free;
  bi->superBlock = NULL;
  bi->allocator = NULL;
  memBlock->ref_info = RefInfo_new(0);
  MemBlock_INCREF(memBlock);
  return memBlock;
}

inline long MemBlock_INCREF(struct MemBlock* memBlock) {
  long* refcount = &(memBlock->ref_info->refCount);
#ifdef RTJ_DEBUG
  printf("MemBlock_INCREF(%08x): %d -> ", memBlock, *refcount); 
#endif
  atomic_add((uint32_t*)refcount, 1);
#ifdef RTJ_DEBUG
  printf("%d \n", *refcount);
#endif
  return *refcount;
}

inline long MemBlock_DECREF(struct MemBlock* memBlock) {
  long* refcount = &(memBlock->ref_info->refCount);
#ifdef RTJ_DEBUG
  printf("MemBlock_DECREF(%08x): %d -> ", memBlock, *refcount);
#endif
  atomic_add((uint32_t*)refcount, -1);
#ifdef RTJ_DEBUG
  printf("%d \n", *refcount);
#endif
  return *refcount;
}

inline void* MemBlock_alloc(struct MemBlock* memBlock, 
			    size_t size) {
#ifdef RTJ_DEBUG
  printf("MemBlock_alloc(%08x, %d)\n", (int)memBlock, size);
#endif
  if (!memBlock) { 
#ifdef RTJ_DEBUG
    printf("No memBlock yet...\n");
#endif
    return HeapMemory_alloc(size);
  }
  return memBlock->block_info->alloc(memBlock, size);
}

inline void MemBlock_free(struct MemBlock* memBlock) {
  struct MemBlock* superBlock;
#ifdef RTJ_DEBUG
  printf("MemBlock_free(%08x)\n", (int)memBlock);
#endif
  if (superBlock = memBlock->block_info->superBlock) {
      if (MemBlock_DECREF(memBlock)) {
#ifdef RTJ_DEBUG
	  printf("  decrementing reference count: %d\n", 
		 memBlock->ref_info->refCount);
#endif
      } else {
#ifdef RTJ_DEBUG
	  printf("  freeing memory!\n");
#endif
	  memBlock->block_info->free(memBlock);
	  MemBlock_free(superBlock);
      }
  }
}

inline struct MemBlock* MemBlock_prevMemBlock(struct MemBlock* memBlock) {
#ifdef RTJ_DEBUG
  printf("MemBlock_prevMemBlock()\n");
#endif
  return memBlock->block_info->superBlock;
}

void* ScopedPhysical_RThread_MemBlock_alloc(struct MemBlock* mem, 
					    size_t size) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_RThread_MemBlock_alloc(mem, size);
}

void  ScopedPhysical_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_free()\n");
#endif
  Scope_RThread_MemBlock_free(mem);
}

inline Allocator ScopedPhysical_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* ScopedPhysical_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
						  size_t size) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_NoHeapRThread_MemBlock_alloc(mem, size);
}

void  ScopedPhysical_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_NoHeapRThread_MemBlock_free()\n");
#endif
  Scope_NoHeapRThread_MemBlock_free(mem);
}

inline Allocator ScopedPhysical_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("ScopedPhysical_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* ImmortalPhysical_RThread_MemBlock_alloc(struct MemBlock* mem, 
					      size_t size) {
#ifdef RTJ_DEBUG
  printf("ImmortalPhysical_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_RThread_MemBlock_alloc(mem, size);
}

void  ImmortalPhysical_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("ImmortalPhysical_RThread_MemBlock_free()\n");
#endif
  Scope_RThread_MemBlock_free(mem);
}

inline Allocator ImmortalPhysical_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("ImmortalPhysical_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* ImmortalPhysical_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem,
						    size_t size) {
#ifdef RTJ_DEBUG
  printf("ImmortalPhysical_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_NoHeapRThread_MemBlock_alloc(mem, size);
}

void  ImmortalPhysical_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("ImmortalPhysical_NoHeapRThread_MemBlock_free()\n");
#endif
  Scope_NoHeapRThread_MemBlock_free(mem);
}

inline Allocator ImmortalPhysical_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("ImmortalPhysical_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return NULL;
}


void* CTScope_RThread_MemBlock_alloc(struct MemBlock* mem, 
				     size_t size) {
#ifdef RTJ_DEBUG
  void* ptr;
  printf("CTScope_RThread_MemBlock_alloc(%08x, %d)\n", mem, size);
  printf("  current usage: %d of %d\n", 
	 (size_t)((mem->block->free)-(mem->block->begin)),
	 (size_t)((mem->block->end)-(mem->block->begin)));
  printf("  begin: %08x, free: %08x, end: %08x\n", 
	 mem->block->begin,
	 mem->block->free,
	 mem->block->end);
  printf("  retrieving memBlock from %08x\n", mem->block);
  ptr = Block_alloc(mem->block, size);
  printf("  begin: %08x, free: %08x, end: %08x\n", 
	 mem->block->begin,
	 mem->block->free,
	 mem->block->end);
  if (!ptr) {
    assert("Out of memory!!!");
  }
  return ptr;
#else
  return Block_alloc(mem->block, size);
#endif
}

void  CTScope_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("CTScope_RThread_MemBlock_free(%08x)\n", mem);
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
    Block_free(mem->block);
    RTJ_FREE(mem->block_info);
  }
}

inline Allocator CTScope_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("CTScope_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* VTScope_RThread_MemBlock_alloc(struct MemBlock* mem, 
				     size_t size) {
#ifdef RTJ_DEBUG
  printf("VTScope_RThread_MemBlock_alloc(%d)\n", (int)size);
#endif
  return ListAllocator_alloc((struct ListAllocator*)(mem->block_info->allocator), 
			     size);
}

void  VTScope_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("VTScope_RThread_MemBlock_free()\n");
#endif
  ListAllocator_free((struct ListAllocator*)(mem->block_info->allocator));
#ifdef RTJ_DEBUG
  printf("  free(%08x)\n", (int)mem);
#endif
  RTJ_FREE(mem); 
}

inline Allocator VTScope_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("VTScope_RThread_MemBlock_allocator()\n");
#endif
  return ListAllocator_new(0);
}

void* LTScope_RThread_MemBlock_alloc(struct MemBlock* mem, 
				     size_t size) {
#ifdef RTJ_DEBUG
  printf("LTScope_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_RThread_MemBlock_alloc(mem, size);
}

void  LTScope_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("LTScope_RThread_MemBlock_free()\n");
#endif
  Scope_RThread_MemBlock_free(mem);
}

inline Allocator LTScope_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("LTScope_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* CTScope_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
					   size_t size) {
#ifdef RTJ_DEBUG
  printf("CTScope_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_NoHeapRThread_MemBlock_alloc(mem, size);
}

void  CTScope_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("CTScope_NoHeapRThread_MemBlock_free()\n");
#endif
  Scope_RThread_MemBlock_free(mem);
}

inline Allocator CTScope_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("CTScope_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* VTScope_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
					   size_t size) {
#ifdef RTJ_DEBUG
  printf("VTScope_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_NoHeapRThread_MemBlock_alloc(mem, size);
}

void  VTScope_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("VTScope_NoHeapRThread_MemBlock_free()\n");
#endif
  Scope_NoHeapRThread_MemBlock_free(mem);
}

inline Allocator VTScope_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("VTScope_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* LTScope_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
					   size_t size) {
#ifdef RTJ_DEBUG
  printf("LTScope_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return Scope_NoHeapRThread_MemBlock_alloc(mem, size);
}

void  LTScope_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("LTScope_NoHeapRThread_MemBlock_free()\n");
#endif
  Scope_NoHeapRThread_MemBlock_free(mem);
}

inline Allocator LTScope_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("LTScope_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return NULL;
}


inline void* Scope_RThread_MemBlock_alloc(struct MemBlock* mem, 
						size_t size) {
#ifdef RTJ_DEBUG
  printf("Scope_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return Heap_RThread_MemBlock_alloc(mem, size);
}

inline void  Scope_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("Scope_RThread_MemBlock_free()\n");
#endif
}

inline Allocator Scope_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("Scope_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

inline void* Scope_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
						size_t size) {
#ifdef RTJ_DEBUG
  printf("Scope_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return Heap_RThread_MemBlock_alloc(mem, size);
}

inline void  Scope_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("Scope_NoHeapRThread_MemBlock_free()\n");
#endif
}

inline Allocator Scope_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("Scope_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* Heap_RThread_MemBlock_alloc(struct MemBlock* mem, 
				  size_t size) {
#ifdef RTJ_DEBUG
  printf("Heap_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return HeapMemory_alloc(size);
}

void  Heap_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("Heap_RThread_MemBlock_free()\n");
#endif
  HeapMemory_freeAll();
}

inline Allocator Heap_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("Heap_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* Immortal_RThread_MemBlock_alloc(struct MemBlock* mem, 
				      size_t size) {
#ifdef RTJ_DEBUG
  printf("Immortal_RThread_MemBlock_alloc(%d)\n", size);
#endif
  return Heap_RThread_MemBlock_alloc(mem, size);
}

void  Immortal_RThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("Immortal_RThread_MemBlock_free()\n");
#endif
}

inline Allocator Immortal_RThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("Immortal_RThread_MemBlock_allocator()\n");
#endif
  return NULL;
}

void* Immortal_NoHeapRThread_MemBlock_alloc(struct MemBlock* mem, 
					    size_t size) {
#ifdef RTJ_DEBUG
  printf("Immortal_NoHeapRThread_MemBlock_alloc(%d)\n", size);
#endif
  return Heap_RThread_MemBlock_alloc(mem, size);
}

void  Immortal_NoHeapRThread_MemBlock_free(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  printf("Immortal_NoHeapRThread_MemBlock_free()\n");
#endif
}

inline Allocator Immortal_NoHeapRThread_MemBlock_allocator(jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("Immortal_NoHeapRThread_MemBlock_allocator()\n");
#endif
  return NULL;
}
