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

/* no global refs, initially. */
struct _jobject FNI_globalrefs = { NULL, NULL };

/** constructor/destructor for thread state information structure */

static JNIEnv * FNI_CreateThreadState(void) {
  struct FNI_Thread_State * env = malloc(sizeof(*env));
  env->vtable = &FLEX_JNI_vtable;
  env->exception = NULL;
  env->localrefs.obj = NULL;
  env->localrefs.next= NULL;
  env->thread = NULL;
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_init(&(env->sleep_mutex), NULL);
  pthread_mutex_lock(&(env->sleep_mutex));
  pthread_cond_init(&(env->sleep_cond), NULL);
#endif
  return (JNIEnv *) env;
}
static void FNI_DestroyThreadState(void *cl) {
  struct FNI_Thread_State * env = (struct FNI_Thread_State *) cl;
  if (cl==NULL) return; // death of uninitialized thread.
  // ignore wrapped exception; free localrefs.
  FNI_DeleteLocalRefsUpTo((JNIEnv *)env, NULL);
#ifdef WITH_HEAVY_THREADS
  // destroy condition variable & mutex
  pthread_mutex_unlock(&(env->sleep_mutex));
  pthread_mutex_destroy(&(env->sleep_mutex));
  pthread_cond_destroy(&(env->sleep_cond));
#endif
  // now free thread state structure.
  free(env);
}

/** implementations of JNIEnv management functions.  */

#ifdef WITH_NO_THREADS
/** single-threaded implementation. Single, global, JNIEnv. */
static JNIEnv *FNI_JNIEnv = NULL;
void FNI_InitJNIEnv(void) { /* do nothing */ }
JNIEnv *FNI_CreateJNIEnv(void) { return FNI_JNIEnv = FNI_CreateThreadState(); }
JNIEnv *FNI_GetJNIEnv(void) { return FNI_JNIEnv; }
#endif /* WITH_NO_THREADS */

#ifdef WITH_HEAVY_THREADS
/** threaded implementation: JNIEnv is stored in per-thread memory. */
#include <pthread.h>
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
#endif /* WITH_HEAVY_THREADS */
