#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include "config.h"
#include "flexthread.h"

void * FNI_GetJNIData(JNIEnv *env, jobject obj) {
  void *result = NULL;
  if (FNI_IS_INFLATED(obj)) {
    struct inflated_oobj *infl= FNI_UNWRAP_MASKED(obj)->hashunion.inflated;
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
    // acquire read lock.
    pthread_rwlock_rdlock(&(infl->jni_data_lock));
#endif
    result = infl->jni_data;
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
    // release read lock.
    pthread_rwlock_unlock(&(infl->jni_data_lock));
#endif
  }
  return result;
}

void FNI_SetJNIData(JNIEnv *env, jobject obj,
		    void *jni_data, void (*cleanup_func)(void *jni_data)) {
  struct inflated_oobj *infl;
  if (!FNI_IS_INFLATED(obj)) FNI_InflateObject(env, obj);
  infl = FNI_UNWRAP_MASKED(obj)->hashunion.inflated;
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  // acquire write lock.
  pthread_rwlock_wrlock(&(infl->jni_data_lock));
#endif
  if (infl->jni_cleanup_func) // free old data.
    (*infl->jni_cleanup_func)(infl->jni_data);
  // set new data.
  infl->jni_data = jni_data;
  infl->jni_cleanup_func = cleanup_func;
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  // release write lock.
  pthread_rwlock_unlock(&(infl->jni_data_lock));
#endif
  return;
}
