/* MemoryArea.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "MemoryArea.h"

static jfieldID MemAreaStack_next = NULL;

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
  if (!MemAreaStack_next) {
    jclass memAreaStackClaz = (*env)->GetObjectClass(env, memAreaStack);
    MemAreaStack_next = (*env)->GetFieldID(env, memAreaStackClaz, "next", 
					   "Ljavax/realtime/MemAreaStack;");
  }
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

/*
 * Class:     MemoryArea
 * Method:    exitMemBlock
 * Signature: (Ljavax/realtime/RealtimeThread;Ljavax/realtime/MemAreaStack;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_MemoryArea_exitMemBlock
(JNIEnv* env, jobject memoryArea, jobject realtimeThread, jobject memAreaStack) {
  struct MemBlock *memBlock, *newMemBlock;
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
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2I
(JNIEnv *env, jobject memoryArea, jobject realtimeThread, 
 jclass componentClass, jint length) {
  struct MemBlock *oldMemBlock, *newMemBlock;
  jobject result;
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.newArray(%p, %p, %p, %p, %d)\n",
	 env, memoryArea, realtimeThread, componentClass, length);
#endif  
  oldMemBlock = MemBlock_currentMemBlock();
  MemBlock_setCurrentMemBlock(env, realtimeThread, getInflatedObject(env, memoryArea)->memBlock);
  result = Java_java_lang_reflect_Array_newArray(env, NULL, componentClass, length);
  MemBlock_setCurrentMemBlock(env, realtimeThread, oldMemBlock);
  return result;
}

/*
 * Class:     javax_realtime_MemoryArea
 * Method:    newArray
 * Signature: (Ljavax/realtime/RealtimeThread;Ljava/lang/Class;[I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_realtime_MemoryArea_newArray__Ljavax_realtime_RealtimeThread_2Ljava_lang_Class_2_3I
(JNIEnv *env, jobject memoryArea, jobject realtimeThread, 
 jclass componentClass, jintArray dims) {
  struct MemBlock *oldMemBlock, *newMemBlock;
  jobject result;
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.newArray(%p, %p, %p, %p, %p)\n",
	 env, memoryArea, realtimeThread, componentClass, dims);
#endif
  oldMemBlock = MemBlock_currentMemBlock();
  MemBlock_setCurrentMemBlock(env, realtimeThread, 
			      getInflatedObject(env, memoryArea)->memBlock);
  result = Java_java_lang_reflect_Array_multiNewArray(env, NULL, componentClass, dims);
  MemBlock_setCurrentMemBlock(env, realtimeThread, oldMemBlock);
  return result;
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
  struct FNI_method2info *method; /* method information */
  jclass methodclazz; /* declaring class of method */
  jobject result;
#ifdef RTJ_DEBUG
  checkException();
  printf("MemoryArea.newInstance(%p, %p, %p, %p, %p)\n",
	 env, memoryArea, realtimeThread, constructor, parameters);
#endif
  oldMemBlock = MemBlock_currentMemBlock();
  
#ifdef RTJ_DEBUG  
  assert(constructor != NULL);
  checkException();
#endif
  method = FNI_GetMethodInfo(constructor);
#ifdef RTJ_DEBUG
  assert(method != NULL);
  checkException();
#endif
  methodclazz = FNI_WRAP(method->declaring_class_object);
  
  /* check that declaring class is not abstract. */
  if (FNI_GetClassInfo(methodclazz)->modifiers &
      java_lang_reflect_Modifier_ABSTRACT) {
    jclass excls=(*env)->FindClass(env, "java/lang/IllegalAccessException");
    (*env)->ThrowNew(env, excls,
		     "attempted instantiation of an abstract class");
    return NULL;
  }
  /* create zero-filled-object instance. */
  MemBlock_setCurrentMemBlock(env, realtimeThread, 
			      getInflatedObject(env, memoryArea)->memBlock);
  result = FNI_AllocObject_using(env, methodclazz, RTJ_jmalloc);
  MemBlock_setCurrentMemBlock(env, realtimeThread, oldMemBlock);
  if ((*env)->ExceptionOccurred(env)) return NULL; /* bail */

  /* okay, now invoke constructor */
  Java_java_lang_reflect_Method_invoke(env, constructor, result, parameters);
  if ((*env)->ExceptionOccurred(env)) return NULL; /* bail */
  return result;
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
  u_int32_t size;
  jobject clone = memoryArea;
#ifdef WITH_NOHEAP_SUPPORT
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

static jfieldID MemoryArea_shadow = NULL;
static void (*deflate_object)(void* obj, void* client_data) = NULL;

void MemoryArea_finalize(void* obj, void* client_data) {
  JNIEnv* env = FNI_GetJNIEnv();
  struct _jobject memoryArea;
  jobject shadow;
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
  if (!MemoryArea_shadow) {
    MemoryArea_shadow = (*env)->GetFieldID(env, (*env)->GetObjectClass(env, &memoryArea), 
					   "shadow", "Ljavax/realtime/MemoryArea;");
  }
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
  struct oobj* obj = FNI_UNWRAP_MASKED(memoryArea);
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

