/* FNI methods dealing w/ monitors. */
#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include "config.h"
#ifdef WITH_HEAVY_THREADS
#include <pthread.h>
#endif

#ifdef WITH_NO_THREADS
jint FNI_MonitorEnter(JNIEnv *env, jobject obj) {
  assert(FNI_NO_EXCEPTIONS(env));
  return 0;
}
jint FNI_MonitorExit(JNIEnv *env, jobject obj) {
  assert(FNI_NO_EXCEPTIONS(env));
  return 0;
}
void FNI_MonitorWait(JNIEnv *env, jobject obj, const struct timespec *abstime){
  assert(0/* can't wait in single-threaded mode! */);
}
void FNI_MonitorNotify(JNIEnv *env, jobject obj, jboolean wakeall) {
  return; /* we'll just ignore the notify in single-threaded mode */
}
#endif

#ifdef WITH_HEAVY_THREADS

/* the contents of an inflated lock */
struct lockinfo {
  pthread_t tid; /* can be zero, if no one has this lock */
  jint nesting_depth; /* recursive lock nesting depth */
  pthread_mutex_t mutex; /* simple (not recursive) lock */
  pthread_cond_t  cond; /* condition variable */
};

/* lock for inflating locks */
static pthread_mutex_t global_inflate_mutex = PTHREAD_MUTEX_INITIALIZER;

jint FNI_MonitorEnter(JNIEnv *env, jobject obj) {
  pthread_t self = pthread_self();
  struct lockinfo *li;
  assert(FNI_NO_EXCEPTIONS(env));
  /* check object field, inflate lock if necessary. */
  li = NULL; /* substitute proper initialization */
  if (li->tid == self) { /* i already have the lock */
    li->nesting_depth++;
  } else { /* someone else (or no one) has this lock */
    pthread_mutex_lock(&(li->mutex));
    li->tid = self;
    li->nesting_depth=1;
  }
  return 0;
}
jint FNI_MonitorExit(JNIEnv *env, jobject obj) {
  struct lockinfo *li;
  assert(FNI_NO_EXCEPTIONS(env));
  li = NULL; /* substitute proper initialization */
  assert(li->tid == pthread_self());
  if (--li->nesting_depth == 0) {
    /* okay, unlock this puppy. */
    li->tid = 0;
    pthread_mutex_unlock(&(li->mutex));
  }
  return 0;
}
void FNI_MonitorWait(JNIEnv *env, jobject obj, const struct timespec *abstime){
  struct lockinfo *li;
  assert(FNI_NO_EXCEPTIONS(env));
  li = NULL; /* substitute proper initialization */
  if (li->tid != pthread_self()) {
    /* throw IllegalMonitorStateException */
  } else {
    pthread_t tid = li->tid;
    jint nesting_depth = li->nesting_depth;
    li->tid = 0; // let other folk grab the lock, just as soon as we give it up
    if (abstime==NULL)
      pthread_cond_wait(&(li->cond), &(li->mutex));
    else
      pthread_cond_timedwait(&(li->cond), &(li->mutex), abstime);
    li->tid = tid;
    li->nesting_depth = nesting_depth;
  }
}
void FNI_MonitorNotify(JNIEnv *env, jobject obj, jboolean wakeall) {
  struct lockinfo *li;
  assert(FNI_NO_EXCEPTIONS(env));
  li = NULL; /* substitute proper initialization */
  if (li->tid != pthread_self()) {
    /* throw IllegalMonitorStateException */
  } else if (wakeall)
    pthread_cond_broadcast(&(li->cond));
  else
    pthread_cond_signal(&(li->cond));
}
#endif
