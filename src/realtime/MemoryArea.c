/* MemoryArea.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "MemoryArea.h"

static jclass ArrayClaz;
static jmethodID Array_newInstance;
static jmethodID Array_newInstance_dims;
static jmethodID Constructor_newInstance;
static jfieldID MemAreaStack_next;

#ifdef WITH_NOHEAP_SUPPORT
static jfieldID MemoryArea_shadow = NULL;
#endif

void MemoryArea_init(JNIEnv* env) {
  jclass memAreaStackClaz = (*env)->FindClass(env, "javax/realtime/MemAreaStack");
  jclass constructorClaz;
  jclass memoryAreaClaz;
  checkException();
  ArrayClaz = (*env)->FindClass(env, "java/lang/reflect/Array");
  checkException();
  ArrayClaz = (*env)->NewGlobalRef(env, ArrayClaz);
  checkException();
  MemAreaStack_next = (*env)->GetFieldID(env, memAreaStackClaz, "next",
					 "Ljavax/realtime/MemAreaStack;");
  checkException();
  Array_newInstance = (*env)->GetStaticMethodID(env, ArrayClaz, "newInstance",
						"(Ljava/lang/Class;I)Ljava/lang/Object;");
  checkException();
  Array_newInstance_dims = (*env)->GetStaticMethodID(env, ArrayClaz, "newInstance",
						     "(Ljava/lang/Class;[I)Ljava/lang/Object;");
  checkException();
  constructorClaz = (*env)->FindClass(env, "java/lang/reflect/Constructor");
  checkException();
  Constructor_newInstance = (*env)->GetMethodID(env, constructorClaz, "newInstance",
						"([Ljava/lang/Object;)Ljava/lang/Object;");
  checkException();
  memoryAreaClaz = (*env)->FindClass(env, "javax/realtime/MemoryArea");
  checkException();
#ifdef WITH_NOHEAP_SUPPORT
  MemoryArea_shadow = (*env)->GetFieldID(env, memoryAreaClaz, 
					 "shadow", "Ljavax/realtime/MemoryArea;");
  checkException();
#endif
}

/*
 * Class:     MemoryArea
 * Method:    enterMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;Ljavax/realtime/MemAreaStack;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_enterMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, jobject memAreaStack) {
  struct MemBlock* memBlock;
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.enterMemBlock(%p, %p, %p, %p)\n", 
	 env, memoryArea, realtimeThread, memAreaStack);
#endif
  getInflatedObject(env, memAreaStack)->memBlock = MemBlock_currentMemBlock();
  if (memAreaStack) {
    MemBlock_INCREF(getInflatedObject(env, memAreaStack)->memBlock);
  }
  if (!((memBlock = getInflatedObject(env, memoryArea)->memBlock)->refCount)) {
    pthread_mutex_lock(&(memBlock->finalize_lock));
    while ((!memBlock->refCount)&&(memBlock->memoryArea)) {
      pthread_cond_wait(&(memBlock->finalize_cond), &(memBlock->finalize_lock));
    }
    if (!memBlock->memoryArea) {
      memBlock->memoryArea = (*env)->NewGlobalRef(env, memoryArea);
    }
    MemBlock_INCREF(memBlock);
    pthread_mutex_unlock(&(memBlock->finalize_lock));
    pthread_cond_signal(&(memBlock->finalize_cond));
  } else {
    MemBlock_INCREF(memBlock);
  }
  MemBlock_setCurrentMemBlock(env, realtimeThread, memBlock);
}

JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_enterMemBlock_00024_00024initcheck
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, jobject memAreaStack) {
#ifdef RTJ_DEBUG
  printf("\nMemoryArea.enterMemBlock_initcheck");
#endif
  Java_javax_realtime_MemoryArea_enterMemBlock(env, memoryArea, realtimeThread, memAreaStack);
}

/*
 * Class:     MemoryArea
 * Method:    exitMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;Ljavax/realtime/MemAreaStack;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_exitMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, jobject memAreaStack) {
  struct MemBlock *memBlock, *newMemBlock;
  /* This should be whittled down to the minimal atomic section. */
#ifdef WITH_REALTIME_THREADS 
  int state = StopSwitching();
#endif  
#ifdef RTJ_DEBUG
  printf("MemoryArea.exitMemBlock(%p, %p, %p)\n",
	 env, memoryArea, realtimeThread);
#endif

  memBlock = MemBlock_currentMemBlock();
  MemBlock_setCurrentMemBlock(env, realtimeThread, newMemBlock = 
			      getInflatedObject(env, memAreaStack)->memBlock);
  if (MemBlock_free(memBlock)) {
    MemBlock_free(newMemBlock);
  }
#ifdef WITH_REALTIME_THREADS
  RestoreSwitching(state);
#endif
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2I
(JNIEnv *env, jobject memoryArea, jobject realtimeThread, 
 jclass componentClass, jint length) {
  struct MemBlock *oldMemBlock;
  jobject result;
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.newArray(%p, %p, %p, %p, %d)\n",
	 env, memoryArea, realtimeThread, componentClass, length);
#endif  
  oldMemBlock = MemBlock_currentMemBlock();
  MemBlock_setCurrentMemBlock(env, realtimeThread, getInflatedObject(env, memoryArea)->memBlock);
  result = (*env)->CallStaticObjectMethod(env, ArrayClaz, Array_newInstance, 
					  componentClass, length);
  MemBlock_setCurrentMemBlock(env, realtimeThread, oldMemBlock);
  result = (*env)->NewGlobalRef(env, result);
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
  return result;
}

JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2I_00024_00024initcheck
(JNIEnv *env, jobject memoryArea, jobject realtimeThread,
 jclass componentClass, jint length) {
#ifdef RTJ_DEBUG
  printf("\nMemoryArea.newArray_initcheck");
#endif
  return Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2I(env, memoryArea, realtimeThread, componentClass, length);
}

/* BAD HACK!!  Please fix the initializer transform! */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray_00024_00024initcheck
(JNIEnv *env, jobject memoryArea, jobject realtimeThread,
 jclass componentClass, jint length) {
  printf("WARNING: Initializer transform is broken - please fix!\n");
  return Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2I(env, memoryArea, realtimeThread, componentClass, length);
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;[I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2_3I
(JNIEnv *env, jobject memoryArea, jobject realtimeThread, 
 jclass componentClass, jintArray dims) {
  struct MemBlock *oldMemBlock;
  jobject result;
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.newArray(%p, %p, %p, %p, %p)\n",
	 env, memoryArea, realtimeThread, componentClass, dims);
#endif
  oldMemBlock = MemBlock_currentMemBlock();
  MemBlock_setCurrentMemBlock(env, realtimeThread, getInflatedObject(env, memoryArea)->memBlock);
  result = (*env)->CallStaticObjectMethod(env, ArrayClaz, Array_newInstance_dims,
					  componentClass, dims);
  MemBlock_setCurrentMemBlock(env, realtimeThread, oldMemBlock);
  result = (*env)->NewGlobalRef(env, result);
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
  return result;
}

JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2_3I_00024_00024initcheck
(JNIEnv *env, jobject memoryArea, jobject realtimeThread,
 jclass componentClass, jintArray dims) {
#ifdef RTJ_DEBUG
  printf("\nMemoryArea.newArray_initcheck");
#endif
  return Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2_3I(env, memoryArea, realtimeThread, componentClass, dims);
}


/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newInstance
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/reflect/Constructor;[Ljava/lang/Object)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newInstance
(JNIEnv *env, jobject memoryArea, jobject realtimeThread, 
 jobject constructor, jobjectArray parameters) {
  struct MemBlock* oldMemBlock; 
  jobject result;
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.newInstance(%p, %p, %p, %p, %p)\n",
	 env, memoryArea, realtimeThread, constructor, parameters);
#endif
  oldMemBlock = MemBlock_currentMemBlock();
  MemBlock_setCurrentMemBlock(env, realtimeThread, getInflatedObject(env, memoryArea)->memBlock);
  result = (*env)->CallObjectMethod(env, constructor, Constructor_newInstance, parameters);
  MemBlock_setCurrentMemBlock(env, realtimeThread, oldMemBlock);
  result = (*env)->NewGlobalRef(env, result);
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
  return result;
}

JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newInstance_00024_00024initcheck
(JNIEnv *env, jobject memoryArea, jobject realtimeThread, jobject constructor, jobjectArray parameters) {
#ifdef RTJ_DEBUG
  printf("\nMemoryArea.newInstance_initcheck");
#endif
  return Java_javax_realtime_MemoryArea_newInstance(env, memoryArea, realtimeThread, constructor, parameters);
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    throwIllegalAssignmentError
 * Signature: (Ljava/lang/Object;Ljavax/realtime/MemoryArea;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_throwIllegalAssignmentError
(JNIEnv* env, jobject fromMA, jobject toObj, jobject toMA) {
  jclass excls;
#ifdef RTJ_DEBUG
  printf("An Illegal assignment was detected.  Throwing an IllegalAssignmentError.\n");
  printf("From a %s to a %s\n", className(fromMA), className(toMA));
  printf("illegal access to ");
#ifdef RTJ_DEBUG_REF
  printPointerInfo(FNI_UNWRAP_MASKED(toObj), 1);
#else
  printf("location %p of type %s\n", toObj, className(toObj));
#endif
#endif
  excls = (*env)->FindClass(env, "javax/realtime/IllegalAssignmentError");
  (*env)->ThrowNew(env, excls, 
		   "illegal assignment detected: use RTJ_DEBUG and RTJ_DEBUG_REF to debug");
#ifdef RTJ_DEBUG
  checkException();
#endif
}

void* allocfunc(jsize length) {
  return RTJ_MALLOC_UNCOLLECTABLE(length);
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    shadow
 * Signature: ()Ljavax/realtime/MemoryArea;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_shadow
(JNIEnv* env, jobject memoryArea) {
  jobject clone = memoryArea;
#ifdef WITH_NOHEAP_SUPPORT
  u_int32_t size;
  if (((ptroff_t)FNI_UNWRAP(memoryArea))&1) {
      size = FNI_CLAZ(FNI_UNWRAP_MASKED(memoryArea))->size;
      clone = FNI_Alloc(env, NULL, FNI_CLAZ(FNI_UNWRAP_MASKED(memoryArea)), allocfunc, size);
      memcpy(FNI_UNWRAP_MASKED(clone)->field_start,
	     FNI_UNWRAP_MASKED(memoryArea)->field_start,
	     size - sizeof(struct oobj));
      getInflatedObject(env, clone)->memBlock = 
	  getInflatedObject(env, memoryArea)->memBlock;
  }
#endif
  return clone;
}

JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_shadow_00024_00024initcheck
(JNIEnv* env, jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("\nMemoryArea.shadow_initcheck");
#endif
  return Java_javax_realtime_MemoryArea_shadow(env, memoryArea);
}

static void (*deflate_object)(void* obj, void* client_data) = NULL;

void MemoryArea_finalize(void* obj, void* client_data) {
  JNIEnv* env = FNI_GetJNIEnv();
  struct _jobject memoryArea;
#ifdef WITH_NOHEAP_SUPPORT
  jobject shadow;
#endif
#ifndef WITH_PRECISE_GC
  struct MemBlock* memBlock;
#endif
  memoryArea.obj = obj+((ptroff_t)client_data);
#ifdef RTJ_DEBUG
  checkException();
  printf("%s.finalNative(%p, %p)\n", classNameUnwrap(PTRMASK(obj)), 
	 obj, client_data);
  assert(!getInflatedObject(env, &memoryArea)->memBlock->refCount);
#endif
#ifdef WITH_NOHEAP_SUPPORT
  shadow = (*env)->GetObjectField(env, &memoryArea, MemoryArea_shadow);
  if (FNI_UNWRAP(shadow) != memoryArea.obj) {
    struct oobj* shadow_unwrapped = FNI_UNWRAP(shadow);
#ifdef RTJ_DEBUG
    printf("  freeing shadow = %p\n", shadow_unwrapped);
#endif
    (*env)->DeleteLocalRef(env, shadow);
    RTJ_FREE(shadow_unwrapped);
  }
#endif
#ifdef WITH_PRECISE_GC
  RefCountAllocator_DECREF(gc_info, getInflatedObject(env, &memoryArea)->memBlock);
#else
  MemBlock_finalize(NULL, memBlock = getInflatedObject(env, &memoryArea)->memBlock);
  RTJ_FREE(memBlock);
#endif
#ifdef RTJ_DEBUG
  printf("  calling deflate_object...");
#endif
  deflate_object(obj, client_data);
#ifdef RTJ_DEBUG
  printf("done!\n");
#endif
}
/*
 * Class:     javax_realtime_MemoryArea
 * Method:    registerFinal
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_registerFinal
(JNIEnv* env, jobject memoryArea) {
  struct inflated_oobj* infl = getInflatedObject(env, memoryArea);
#ifdef BDW_CONSERVATIVE_GC
  struct oobj* obj = FNI_UNWRAP_MASKED(memoryArea);
#endif
  /* If it's inflated, that means that inflate object registered deflate object
   * as it's finalizer, which must be called after RTJ_finalizer to deallocate
   * the extra memory associated with the inflated object.
   */
  if (!deflate_object) { 
    deflate_object = infl->RTJ_finalizer;
  }
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.registerFinal(%p, %p)\n", env, memoryArea);
#endif
#ifdef WITH_PRECISE_GC 
  infl->precise_deflate_obj = ((void*)MemoryArea_finalize);
#elif defined(BDW_CONSERVATIVE_GC)
    /* register finalizer to deallocate inflated_oobj on gc */
  if (GC_base(obj)!=NULL) {// skip if this is not a heap-allocated object
    GC_register_finalizer(GC_base(obj), MemoryArea_finalize,
			  (GC_PTR) ((void*)obj-(void*)GC_base(obj)),
			  &(infl->old_finalizer),
			  &(infl->old_client_data));
  } 
#endif
  RTJ_register_finalizer(memoryArea, MemoryArea_finalize); 
}

JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_registerFinal_00024_00024initcheck
(JNIEnv* env, jobject memoryArea) {
#ifdef RTJ_DEBUG
  printf("\nMemoryArea.registerFinal_initcheck");
#endif
  Java_javax_realtime_MemoryArea_registerFinal(env, memoryArea);
}
