/* FNI methods dealing w/ monitors. */
#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include <errno.h> /* for EBUSY */
#include "config.h"
#include "flexthread.h"
#include "fni-stats.h"

DECLARE_STATS_EXTERN(monitor_enter)
DECLARE_STATS_EXTERN(monitor_contention)

#ifndef WITH_THREADS
jint FNI_MonitorEnter(JNIEnv *env, jobject obj) {
  assert(FNI_NO_EXCEPTIONS(env));
  INCREMENT_STATS(monitor_enter, 1);
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
#endif /* !WITH_THREADS */

#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
#ifdef WITH_PRECISE_GC
#include "jni-gcthreads.h"
#endif

jint FNI_MonitorEnter(JNIEnv *env, jobject obj) {
  pthread_t self = pthread_self();
  struct inflated_oobj *li; int st;
  assert(FNI_NO_EXCEPTIONS(env));
  INCREMENT_STATS(monitor_enter, 1);
  /* check object field, inflate lock if necessary. */
  if (!FNI_IS_INFLATED(obj)) FNI_InflateObject(env, obj);
  li = FNI_INFLATED(obj);
  if (li->tid == self) { /* i already have the lock */
    li->nesting_depth++;
  } else { /* someone else (or no one) has this lock */
#ifdef WITH_STATISTICS
    if ((st = pthread_mutex_trylock(&(li->mutex)))!=EBUSY) goto gotlock;
    INCREMENT_STATS(monitor_contention, 1);
#endif /* WITH_STATISTICS */
#ifdef WITH_PRECISE_GC
    decrement_running_thread_count();
#endif /* WITH_PRECISE_GC */
    st = pthread_mutex_lock(&(li->mutex));
#ifdef WITH_PRECISE_GC
    increment_running_thread_count();
#endif /* WITH_PRECISE_GC */
#ifdef WITH_STATISTICS
  gotlock:
#endif /* WITH_STATISTICS */
    assert(st==0 /* no mutex errors */);
    li->tid = self;
    li->nesting_depth=1;
  }
  return 0;
}
jint FNI_MonitorExit(JNIEnv *env, jobject obj) {
  struct inflated_oobj *li; int st;
  assert(FNI_NO_EXCEPTIONS(env));
  assert(FNI_IS_INFLATED(obj));
  li = FNI_INFLATED(obj);
  assert(li->tid == pthread_self());
  if (--li->nesting_depth == 0) {
    /* okay, unlock this puppy. */
    li->tid = 0;
    st = pthread_mutex_unlock(&(li->mutex));
    assert(st==0 /* no mutex errors */);
  }
  return 0;
}
void FNI_MonitorWait(JNIEnv *env, jobject obj, const struct timespec *abstime){
  struct inflated_oobj *li; jclass ex; int st;
  assert(FNI_NO_EXCEPTIONS(env));
  if (!FNI_IS_INFLATED(obj)) goto error; // we don't have the lock.
  li = FNI_INFLATED(obj);
  if (li->tid != pthread_self()) goto error; // we don't have the lock.
  else { /* open brace so we can push new stuff on the stack */
    pthread_t tid = li->tid;
    jint nesting_depth = li->nesting_depth;
    li->tid = 0;/*let other folk grab the lock, just as soon as we give it up*/
#ifdef WITH_PRECISE_GC
      decrement_running_thread_count();
#endif /* WITH_PRECISE_GC */
    if (abstime==NULL)
      st = pthread_cond_wait(&(li->cond), &(li->mutex));
    else
      st = pthread_cond_timedwait(&(li->cond), &(li->mutex), abstime);
#ifdef WITH_PRECISE_GC
      increment_running_thread_count();
#endif /* WITH_PRECISE_GC */
    assert(st==0 || st==ETIMEDOUT || st==EINTR /*no cond variable errors*/);
    li->tid = tid;
    li->nesting_depth = nesting_depth;
    return;
  }

 error:
  ex = (*env)->FindClass(env, "java/lang/IllegalMonitorStateException");
  if ((*env)->ExceptionOccurred(env)) return;
  (*env)->ThrowNew(env, ex, "wait() called but we don't have the lock");
  return;
}
void FNI_MonitorNotify(JNIEnv *env, jobject obj, jboolean wakeall) {
  struct inflated_oobj *li; jclass ex; int st;
  assert(FNI_NO_EXCEPTIONS(env));
  if (!FNI_IS_INFLATED(obj)) goto error; // we don't have the lock.
  li = FNI_INFLATED(obj);
  if (li->tid != pthread_self()) goto error; // we don't have the lock.

  if (wakeall)
    st = pthread_cond_broadcast(&(li->cond));
  else
    st = pthread_cond_signal(&(li->cond));
  assert(st == 0/* no condition variable errors */);
  return;

 error:
  ex = (*env)->FindClass(env, "java/lang/IllegalMonitorStateException");
  if ((*env)->ExceptionOccurred(env)) return;
  (*env)->ThrowNew(env, ex, "notify() called but we don't have the lock");
  return;
}
#endif /* WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS */
