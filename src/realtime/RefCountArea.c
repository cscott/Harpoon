/* RefCountArea.c, created by wbeebee
   Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
   Licensed under the terms of the GNU GPL; see COPYING for details. */

#include "RefCountArea.h"

/*
 * Class:     javax_realtime_RefCountArea
 * Method:    initNative
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_RefCountArea_initNative
(JNIEnv* env, jobject refCountArea, jlong ignored) {
  struct MemBlock* mb = MemBlock_new(env, refCountArea);
#ifdef RTJ_DEBUG
  checkException();
  printf("RefCountArea.initNative(%p, %p)\n", env, refCountArea);
#endif
  mb->alloc_union.rc  = RefCountAllocator_new();
  mb->alloc           = RefCount_MemBlock_alloc;
  mb->memoryArea      = (*env)->NewGlobalRef(env, refCountArea);
  MemBlock_INCREF(mb);
#ifdef WITH_PRECISE_GC
  mb->gc              = RefCount_MemBlock_gc; /* must be the last set */
#endif
}

JNIEXPORT void JNICALL Java_javax_realtime_RefCountArea_initNative_00024_00024initcheck
(JNIEnv* env, jobject refCountArea, jlong ignored) {
#ifdef RTJ_DEBUG
  printf("\nRefCountArea.initNative_initcheck");
#endif
  Java_javax_realtime_RefCountArea_initNative(env, refCountArea, ignored);
}

void* RefCount_MemBlock_alloc(struct MemBlock* mem, size_t size) {
  void* result;
#ifdef RTJ_DEBUG
  checkException();
  printf("RefCount_MemBlock_alloc(%p, %d)\n", mem, size);
#endif
  result = RefCountAllocator_alloc(mem->alloc_union.rc, size, 0);
  RefCountAllocator_register_finalizer(result, 
				       RefCountAllocator_oobj_finalizer);
  return result;
}

#ifdef WITH_PRECISE_GC
void  RefCount_MemBlock_gc(struct MemBlock* mem) {
#ifdef RTJ_DEBUG
  checkException();
  printf("RefCount_MemBlock_gc(%p)\n", mem);
#endif
  RefCountAllocator_gc(mem->alloc_union.rc);
}
#endif


/*
 * Class:     javax_realtime_RefCountArea
 * Method:    INCREF
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_RefCountArea_INCREF
(JNIEnv* env, jobject refCountArea, jobject obj) {
#ifdef RTJ_DEBUG
  checkException();
  printf("RefCountArea.INCREF(%p, %p, %p)\n", 
	 env, refCountArea, obj);
#endif  
  RefCountAllocator_INCREF(FNI_UNWRAP_MASKED(obj));
}

extern int RTJ_init_in_progress;

JNIEXPORT void JNICALL Java_javax_realtime_RefCountArea_INCREF_00024_00024initcheck
(JNIEnv* env, jobject refCountArea, jobject obj) {
  assert(!RTJ_init_in_progress);
  return Java_javax_realtime_RefCountArea_INCREF(env, refCountArea, obj);
}


/*
 * Class:     javax_realtime_RefCountArea
 * Method:    DECREF
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_javax_realtime_RefCountArea_DECREF
(JNIEnv* env, jobject refCountArea, jobject obj) {
  struct refCountAllocator* rc = 
    getInflatedObject(env, refCountArea)->memBlock->alloc_union.rc;
#ifdef RTJ_DEBUG
  checkException();
  printf("RefCountArea.DECREF(%p, %p, %p)\n", 
	 env, refCountArea, obj);
#endif
  RefCountAllocator_DECREF(rc, FNI_UNWRAP_MASKED(obj));
}

