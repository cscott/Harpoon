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
  if (RTJ_init_in_progress) {
#ifdef RTJ_DEBUG
    RTJ_init_in_progress++;
    printf("  RTJ init in progress... \n");
    if (RTJ_init_in_progress > 50) {
      printf("    Avoid cycles in static initializers by allocating all\n");
      printf("    statically-initialized objects on the heap!\n");
    }
#endif   
    return (void*)RTJ_MALLOC(size);
  }
  newPtr = MemBlock_alloc(
#ifdef RTJ_DEBUG_REF
			  memBlock=
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
    
    while (!compare_and_swap((long int*)(&(memBlock->ptr_info)),
			     (long int)(newInfo->next = memBlock->ptr_info), 
			     (long int)newInfo)) {}
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
  printf("MemBlock_setCurrentMemBlock(0x%08x, 0x%08x, 0x%08x)\n",
	 env, realtimeThread, memBlock);
  checkException();
#endif
  getInflatedObject(env, realtimeThread)->memBlock = memBlock;
}

int RTJ_init_in_progress;

#ifdef WITH_MEMORYAREA_TAGS
static jfieldID memoryAreaID = NULL; 
static jobject heapMem = NULL;
#endif

inline void RTJ_preinit() {
  JNIEnv *env = FNI_GetJNIEnv();
  jclass clazz = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
  jclass memAreaStackClaz = (*env)->FindClass(env, "javax/realtime/MemAreaStack");
#ifdef WITH_MEMORYAREA_TAGS
  jclass heapClaz;
  jmethodID methodID;
#endif
  (*env)->SetStaticBooleanField(env, clazz,
				(*env)->GetStaticFieldID(env, clazz, 
							 "RTJ_init_in_progress", "Z"),
				JNI_TRUE);
  RTJ_init_in_progress = 1;
#ifdef WITH_MEMORYAREA_TAGS
  memoryAreaID = (*env)->GetFieldID(env, clazz, "memoryArea", 
				    "Ljavax/realtime/MemoryArea;");
  heapClaz = (*env)->FindClass(env, "javax/realtime/HeapMemory");    
  methodID = 
    (*env)->GetStaticMethodID(env, heapClaz,
			      "instance", "()Ljavax/realtime/HeapMemory;");
  heapMem = (*env)->CallStaticObjectMethod(env, heapClaz, methodID, NULL);
  heapMem = (*env)->NewGlobalRef(env, heapMem);
#endif
#ifdef RTJ_DEBUG
  printf("RTJ_preinit()\n");
  checkException();
#endif
}

inline void RTJ_init() {
  JNIEnv* env = FNI_GetJNIEnv();
  jclass clazz = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
#ifdef RTJ_DEBUG
  printf("RTJ_init()\n");
  checkException();
#endif
  MemBlock_setCurrentMemBlock(env,
			      ((struct FNI_Thread_State *)env)->thread,
			      Heap_MemBlock_new(env, heapMem));
#ifdef RTJ_DEBUG
  checkException();
#endif
  RTJ_init_in_progress = 0;
  (*env)->SetStaticBooleanField(env, clazz,
				(*env)->GetStaticFieldID(env, clazz,
							 "RTJ_init_in_progress", "Z"),
				JNI_FALSE);
}

#ifdef WITH_MEMORYAREA_TAGS
void RTJ_tagObject(JNIEnv* env, jobject obj) {
  jobject memoryArea;
#ifdef RTJ_DEBUG
  printf("RTJ_tagObject(0x%08x, 0x%08x)\n", env, obj);
#endif
  memoryArea = 
    RTJ_init_in_progress?NULL:(MemBlock_currentMemBlock()->memoryArea);
  FNI_SetObjectField(env, obj, memoryAreaID, 
		     (memoryArea==NULL)?heapMem:memoryArea);
}
#endif
