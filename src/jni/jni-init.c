/* initialization code for the JNI. */
#include <jni.h>
#include <jni-private.h>
extern struct JNINativeInterface FLEX_JNI_vtable;

#include <assert.h>
#include <stdlib.h>
#include "config.h"
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif
#include "flexthread.h"

#ifdef WITH_REALTIME_JAVA
#include "../realtime/RTJconfig.h"
#endif

#ifndef LOCALREF_STACK_SIZE
#define LOCALREF_STACK_SIZE (64*1024) /* 64k word stack */
#endif

/* no global refs, initially. */
struct _jobject_globalref FNI_globalrefs = { {NULL}, NULL, NULL };

/** constructor/destructor for thread state information structure */

static JNIEnv * FNI_CreateThreadState(void) {
  /* safe to use malloc -- no pointers to garbage collected memory in here */
  struct FNI_Thread_State * env = 
#ifdef WITH_REALTIME_JAVA
    (struct FNI_Thread_State*)RTJ_CALLOC_UNCOLLECTABLE(1,sizeof(*env));
#elif defined(WITH_TRANSACTIONS) && defined(BDW_CONSERVATIVE_GC)
    /* we store transaction object pointers in here */
    GC_malloc(sizeof(*env));
#else
    malloc(sizeof(*env));
#endif
  env->vtable = &FLEX_JNI_vtable;
  env->exception = NULL;
  env->localrefs_stack =
  env->localrefs_next =
  env->localrefs_end =
#ifdef WITH_REALTIME_JAVA
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(*(env->localrefs_stack))*LOCALREF_STACK_SIZE);
#else
#ifdef WITH_PRECISE_GC
    malloc
#elif defined(BDW_CONSERVATIVE_GC)
#ifdef WITH_GC_STATS
    GC_malloc_uncollectable_stats
#else
    GC_malloc_uncollectable /* local ref stack has heap pointers */
#endif
#else /* okay, use system-default malloc */
    malloc
#endif
    (sizeof(*(env->localrefs_stack))*LOCALREF_STACK_SIZE);
#endif
  env->localrefs_end += LOCALREF_STACK_SIZE;
  env->thread = NULL;
  env->stack_top = NULL;
  env->is_alive = JNI_FALSE;
#if defined(WITH_REALTIME_JAVA) && defined(WITH_NOHEAP_SUPPORT)
  env->noheap = 0;
#endif
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  pthread_mutex_init(&(env->sleep_mutex), NULL);
  pthread_mutex_lock(&(env->sleep_mutex));
  pthread_cond_init(&(env->sleep_cond), NULL);
#endif
  return (JNIEnv *) env;
}
void FNI_DestroyThreadState(void *cl) {
  struct FNI_Thread_State * env = (struct FNI_Thread_State *) cl;
  if (cl==NULL) return; // death of uninitialized thread.
  // ignore wrapped exception; free localrefs.
#ifdef WITH_REALTIME_JAVA
  RTJ_FREE(env->localrefs_stack);
#elif defined(WITH_PRECISE_GC)
  free(env->localrefs_stack);
#elif defined(BDW_CONSERVATIVE_GC)
  GC_free(env->localrefs_stack);
#else
  free(env->localrefs_stack);
#endif
  env->exception = /* this may point into localrefs_stack, so null it, too */
  env->localrefs_stack = env->localrefs_next = env->localrefs_end = NULL;
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  // destroy condition variable & mutex
  pthread_mutex_unlock(&(env->sleep_mutex));
  pthread_mutex_destroy(&(env->sleep_mutex));
  pthread_cond_destroy(&(env->sleep_cond));
#endif
  // now free thread state structure.
#ifdef WITH_REALTIME_JAVA
  RTJ_FREE(env);
#elif WITH_TRANSACTIONS
  /* allocated with GC_malloc; no 'free' necessary. */
#else
  free(env);
#endif
}

/** implementations of JNIEnv management functions.  */

#ifndef WITH_THREADS
/** single-threaded implementation. Single, global, JNIEnv. */
JNIEnv *FNI_JNIEnv = NULL;
void FNI_InitJNIEnv(void) { /* do nothing */ }
JNIEnv *FNI_CreateJNIEnv(void) { return FNI_JNIEnv = FNI_CreateThreadState(); }
JNIEnv *FNI_GetJNIEnv(void) { return FNI_JNIEnv; }
#endif /* !WITH_THREADS */

#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
/** threaded implementation: JNIEnv is stored in per-thread memory. */
static pthread_key_t FNI_JNIEnv_key;
void FNI_InitJNIEnv(void) {
  int status = pthread_key_create(&FNI_JNIEnv_key, FNI_DestroyThreadState);
  assert(status==0);
}
JNIEnv *FNI_CreateJNIEnv(void) {
  int status = pthread_setspecific(FNI_JNIEnv_key, FNI_CreateThreadState());
  assert(status==0);
  assert(FNI_GetJNIEnv()!=NULL);
  return FNI_GetJNIEnv();
}
JNIEnv *FNI_GetJNIEnv(void) {
  return (JNIEnv *) pthread_getspecific(FNI_JNIEnv_key);
}
#endif /* WITH_HEAVY_THREADS || WITH_PTH_THREADS */

#if WITH_USER_THREADS
/** threaded implementation: JNIEnv is stored in per-thread memory. */
void FNI_InitJNIEnv(void) {
}

JNIEnv *FNI_CreateJNIEnv(void) {
#ifndef WITH_REALTIME_THREADS
  gtl->jnienv = FNI_CreateThreadState();
#else
  currentThread->jnienv = FNI_CreateThreadState();
#endif
  return FNI_GetJNIEnv();
}
JNIEnv *FNI_GetJNIEnv(void) {
#ifndef WITH_REALTIME_THREADS
  return gtl->jnienv;
#else
  return currentThread->jnienv;
#endif
}
#endif /* WITH_USER_THREADS */
