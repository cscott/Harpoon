/* MemBlock.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "MemBlock.h"

#ifdef RTJ_DEBUG_REF
struct PTRinfo* ptr_info = NULL;
#endif

#ifdef WITH_PRECISE_GC
struct refCountAllocator* gc_info = NULL;
#endif

inline void MemBlock_finalize(struct refCountAllocator* rc, void* obj) {
  struct MemBlock* memBlock = (struct MemBlock*)obj;
  JNIEnv* env = FNI_GetJNIEnv();
#ifdef RTJ_DEBUG
  printf("MemBlock_finalize(0x%08x)\n", memBlock);
#endif
  if (memBlock->finalize) memBlock->finalize(memBlock);
  pthread_cond_destroy(&(memBlock->finalize_cond));
  pthread_mutex_destroy(&(memBlock->finalize_lock));
}

struct MemBlock* MemBlock_new(JNIEnv* env, 
			      jobject memoryArea) {
  struct MemBlock* mb;
#ifdef RTJ_DEBUG_REF
  struct PTRinfo* new_ptr_info = 
    (struct PTRinfo*)RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct PTRinfo));
#endif
#ifdef WITH_PRECISE_GC
  if (!gc_info) {
    gc_info = RefCountAllocator_new();
  }
  mb = (struct MemBlock*)RefCountAllocator_alloc(gc_info, sizeof(struct MemBlock), 1);
  RefCountAllocator_register_finalizer(mb, MemBlock_finalize);
#else
  mb = (struct MemBlock*)RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct MemBlock), 1);
#endif
#ifdef RTJ_DEBUG
  checkException();
  printf("MemBlock_new(%s, 0x%08x)\n", 
	 className((*env)->GetObjectClass(env, memoryArea)));
  checkException();
#endif
  pthread_mutex_init(&(mb->finalize_lock), NULL);
  pthread_cond_init(&(mb->finalize_cond), NULL);
  getInflatedObject(env, memoryArea)->memBlock = mb;
#ifdef RTJ_DEBUG_REF
  new_ptr_info->memBlock = mb;
  while (!compare_and_swap((long int*)(&ptr_info),
			   (long int)(new_ptr_info->next = ptr_info),
			   (long int)new_ptr_info)) {}
#endif
  return mb;
}

inline struct inflated_oobj* getInflatedObject(JNIEnv* env, jobject obj) {
#ifdef RTJ_DEBUG
  printf("getInflatedObject(0x%08x, 0x%08x)\n", env, obj);
  checkException();
#endif
  if (!FNI_IS_INFLATED(obj)) {
#ifdef RTJ_DEBUG
    printf("  inflating...\n");
#endif
    FNI_InflateObject(env, obj);
#ifdef RTJ_DEBUG
    checkException();
#endif
  }
  return FNI_UNWRAP_MASKED(obj)->hashunion.inflated;
}

void checkException() {
#ifdef RTJ_DEBUG
  JNIEnv* env = FNI_GetJNIEnv();
  if ((*env)->ExceptionOccurred(env)) {
    (*env)->ExceptionDescribe(env);
    printf("\nException occurred, quitting!\n");
    exit(0);
  }
#endif
}

const char* className(jobject obj) {
  return FNI_GetClassInfo((jclass)(FNI_UNWRAP_MASKED(obj)->claz))->name;
}

const char* classNameUnwrap(struct oobj* obj) {
  return FNI_GetClassInfo((jclass)(obj->claz))->name;
}

inline long MemBlock_INCREF(struct MemBlock* memBlock) {
  long* refcount = &(memBlock->refCount);
#ifdef RTJ_DEBUG
  printf("MemBlock_INCREF(0x%08x): %d -> ", memBlock, *refcount); 
  checkException();
#endif
  atomic_add((uint32_t*)refcount, 1);
#ifdef RTJ_DEBUG
  printf("%d \n", *refcount);
#endif
  return *refcount;
}

inline long MemBlock_DECREF(struct MemBlock* memBlock) {
  long* refcount = &(memBlock->refCount);
#ifdef RTJ_DEBUG
  printf("MemBlock_DECREF(0x%08x): %d -> ", memBlock, *refcount);
  checkException();
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
  printf("MemBlock_alloc(0x%08x, %d)\n", (int)memBlock, size);
  checkException();
#endif
  if (!memBlock) { 
#ifdef RTJ_DEBUG
    printf("No memBlock yet...\n");
#endif
    return (void*)RTJ_MALLOC(size);
  }
  return memBlock->alloc(memBlock, size);
}


#ifdef RTJ_DEBUG_REF
inline void freePtrInfo(struct MemBlock* memBlock) {
  struct PTRinfo* local_ptr_info;
  if (ptr_info) {
    if ((local_ptr_info = ptr_info)->memBlock == memBlock) {
      if (!compare_and_swap((long int*)(&ptr_info),
			    (long int)local_ptr_info,
			    (long int)ptr_info->next)) {
#ifdef RTJ_DEBUG
	printf("  Moved!  Retrying...\n");
#endif
	freePtrInfo(memBlock);
      }
    } else {
      struct PTRinfo* freeMe;
      while ((freeMe = local_ptr_info->next) && 
	     (freeMe->memBlock != memBlock)) local_ptr_info = freeMe;
      if (freeMe && (!compare_and_swap((long int*)(&(local_ptr_info->next)),
				       (long int)freeMe,
				       (long int)(freeMe->next)))) {
#ifdef RTJ_DEBUG
	printf("  Moved!  Retrying...\n");
#endif
	freePtrInfo(memBlock);
      }
    }
  }
}
#endif

inline int MemBlock_free(struct MemBlock* memBlock) {
#ifdef RTJ_DEBUG_REF
  struct PTRinfo* local_ptr_info;
#endif
  JNIEnv* env = FNI_GetJNIEnv();
#ifdef RTJ_DEBUG
  printf("MemBlock_free(0x%08x)\n", memBlock);
  checkException();
#endif
  if (MemBlock_DECREF(memBlock)) {
#ifdef RTJ_DEBUG
    printf("  decrementing reference count: %d\n", 
	   memBlock->refCount);
#endif
    return 0;
  } else {
#ifdef RTJ_DEBUG
    printf("  freeing memory!\n");
#endif
#ifdef RTJ_DEBUG_REF
    /* Free each of the items in the MemBlock-local ptr_info list */
    local_ptr_info = memBlock->ptr_info;
    while (local_ptr_info) {
      struct PTRinfo* next = local_ptr_info->next;
      printf("    %s (0x%08x) allocated at %s:%d, %d bytes freed!\n", 
	     classNameUnwrap(local_ptr_info->ptr),
	     local_ptr_info->ptr, local_ptr_info->file, local_ptr_info->line, 
	     local_ptr_info->size);
      RTJ_FREE(local_ptr_info);
      local_ptr_info = next;
    }
    memBlock->ptr_info = NULL;

    freePtrInfo(memBlock);
#endif
    if (memBlock->free) memBlock->free(memBlock);
    (*env)->DeleteGlobalRef(env, memBlock->memoryArea);
#ifdef RTJ_DEBUG
    checkException();
#endif
    memBlock->memoryArea = NULL;
    pthread_cond_signal(&(memBlock->finalize_cond));
    return 1;
  }
}

#ifdef WITH_PRECISE_GC
inline void RTJ_gc_scan(void* obj, struct accum* acc) {
  struct MemBlock* mb = (struct MemBlock*)obj;
  void (*gc) (struct MemBlock* mem);
#ifdef RTJ_DEBUG
  printf("  MemBlock: 0x%08x\n", mb);
#endif
  if ((mb->refCount)&&(gc=mb->gc)) gc(mb);
#ifdef RTJ_DEBUG
  checkException();
#endif
}

inline void find_RTJ_roots() {
  RefCountAllocator_accumulate(gc_info, RTJ_gc_scan);
}
#endif

#ifdef RTJ_DEBUG_REF
void printPointerInfo(struct oobj* obj, int getClassInfo) {
  struct MemBlock* memBlock;
  struct PTRinfo* topPtrInfo;
  struct PTRinfo* ptrInfo = NULL;
  JNIEnv* env = FNI_GetJNIEnv();
  topPtrInfo = ptr_info;
  printf("pointer at 0x%08x ", obj);
  while ((topPtrInfo != NULL)&&(ptrInfo == NULL)) {
    for (ptrInfo = (memBlock = topPtrInfo->memBlock)->ptr_info;
	 (ptrInfo != NULL)&&(ptrInfo->ptr != obj); 
	 ptrInfo = ptrInfo->next) {}
    topPtrInfo = topPtrInfo->next;
  }
  if (ptrInfo == NULL) {
    printf("not found in MemBlocks\n");
    if (getClassInfo) {
      printf("pointing to a %s of size %d\n", classNameUnwrap(obj), 
	     FNI_ObjectSize(obj));
    } 
  } else {
    printf("found in MemBlock = 0x%08x, \n", memBlock);
    if (memBlock->memoryArea == NULL) {
      printf("allocated during the main thread in the initial HeapMemory\n");
    } else {
      if (getClassInfo) {
	printf("belonging to %s = 0x%08x\n",
	       className(memBlock->memoryArea), 
	       memBlock->memoryArea);
      } else {
	printf("belonging to MemoryArea = 0x%08x\n", 
	       memBlock->memoryArea);
      }
    }
    printf("at location %s:%d", ptrInfo->file, ptrInfo->line);
    if (getClassInfo) {
      printf(" pointing to an %s of size %d\n", classNameUnwrap(obj), ptrInfo->size);
    } else {
      printf(" pointing to a location of size %d\n", ptrInfo->size);
    }
  }
}

void dumpMemoryInfo(int classInfo) {
  struct PTRinfo *topPtrInfo, *ptrInfo;
  struct MemBlock* memBlock;
  topPtrInfo = ptr_info;
  while (topPtrInfo != NULL) {
    if (classInfo) {
      printf("%s (0x%08x):\n", 
	     className(topPtrInfo->memBlock->memoryArea), 
	     topPtrInfo->memBlock->memoryArea);
    } else {
      printf("MemoryArea (0x%08x):\n",
	     topPtrInfo->memBlock->memoryArea);
    }
    printf("  ");
    for (ptrInfo = (memBlock = topPtrInfo->memBlock)->ptr_info;
	 (ptrInfo != NULL); ptrInfo = ptrInfo->next) {
      printf("0x%08x (%s:%d of %d bytes), ", ptrInfo->ptr, ptrInfo->file, ptrInfo->line, ptrInfo->size);
    }
    printf("\n");
    topPtrInfo = topPtrInfo->next;
  }
}
#endif

#ifdef WITH_NOHEAP_SUPPORT
inline void _heapCheck_leap(struct oobj* ptr, const int line, const char* file) {
#ifdef WITH_PRECISE_GC
  JNIEnv* env;
  if ((((int)ptr)&1)&&(((struct FNI_Thread_State*)(env = FNI_GetJNIEnv()))->noheap)) {
    char desc[200];
    jclass excls = (*env)->FindClass(env, "java/lang/IllegalAccessException");
    snprintf(desc, 200, "attempted heap reference 0x%08x in native code at %s:%d\n", 
	     ptr, file, line); 
    (*env)->ThrowNew(env, excls, desc);
#ifdef RTJ_DEBUG
    checkException();
#endif
  }  
#endif
}

#ifdef RTJ_DEBUG_REF
inline void heapCheckRef(struct oobj* ptr, const int line, const char* file, 
			 const char* op) {
#else
inline void heapCheckJava(struct oobj* ptr) {
#endif 
  JNIEnv* env = FNI_GetJNIEnv();
  if (((struct FNI_Thread_State*)env)->noheap) {
    char desc[400];
    jclass excls = (*env)->FindClass(env, "java/lang/IllegalAccessException");
#ifdef RTJ_DEBUG
#ifdef RTJ_DEBUG_REF
    printf("attempted heap reference 0x%08x in java code at %s:%d during %s\n", 
	   ptr, file, line, op);
    printPointerInfo(ptr, 0);
    exit(1);
#else
    printf("attempted heap reference 0x%08x in java code\n", ptr);
    exit(1);
#endif
#endif
    (*env)->ThrowNew(env, excls, 
		     "Illegal heap access.  Use RTJ_DEBUG_REF for more information.");
#ifdef RTJ_DEBUG
    checkException();
#endif
  }
}

inline int IsNoHeapRealtimeThread(JNIEnv *env, 
				  jobject realtimeThread) {
#ifdef RTJ_DEBUG  
  printf("IsNoHeapRealtimeThread(0x%08x, 0x%08x)\n", env, realtimeThread);
  checkException();
#endif
  return (*env)->IsInstanceOf(env, realtimeThread, 
			      (*env)->
			      FindClass(env, 
					"javax/realtime/NoHeapRealtimeThread")) 
    == JNI_TRUE;
}
#endif
