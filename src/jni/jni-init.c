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

#ifndef LOCALREF_STACK_SIZE
#define LOCALREF_STACK_SIZE (64*1024) /* 64k word stack */
#endif

/* no global refs, initially. */
struct _jobject_globalref FNI_globalrefs = { {NULL}, NULL, NULL };

/** constructor/destructor for thread state information structure */

static JNIEnv * FNI_CreateThreadState(void) {
  /* safe to use malloc -- no pointers to garbage collected memory in here */
  struct FNI_Thread_State * env = malloc(sizeof(*env));
  env->vtable = &FLEX_JNI_vtable;
  env->exception = NULL;
  env->localrefs_stack =
  env->localrefs_next =
  env->localrefs_end =
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
#ifdef WITH_PRECISE_GC
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
  free(env);
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
  gtl->jnienv = FNI_CreateThreadState();
  return FNI_GetJNIEnv();
}
JNIEnv *FNI_GetJNIEnv(void) {
  return gtl->jnienv;
}
#endif /* WITH_USER_THREADS */
