#include <jni.h>
#include <jni-private.h>
#include "java_lang_Thread.h"

#include <assert.h>
#include "config.h"
#include <errno.h>
#include "flexthread.h" /* also includes thread-impl-specific headers */
#ifdef WITH_THREADS
# include <sys/time.h>
#endif
#ifdef WITH_HEAVY_THREADS
# include <sched.h> /* for sched_get_priority_min/max */
#endif
#include <stdio.h>
#include <stdlib.h>
#include <time.h> /* for nanosleep */
#include <unistd.h> /* for usleep */
#ifdef WITH_CLUSTERED_HEAPS
# include "../../clheap/alloc.h" /* for NTHR_malloc_first/NTHR_free */
#endif
#include "memstats.h"
#ifdef WITH_PRECISE_GC
# include "jni-gc.h"
# ifdef WITH_THREADS
#  include "jni-gcthreads.h"
# endif
#endif
#ifdef WITH_REALTIME_THREADS
# include "../../realtime/RTJconfig.h" /* for RTJ_MALLOC_UNCOLLECTABLE */
# include "../../realtime/threads.h"
# include "../../realtime/qcheck.h"
#endif /* WITH_REALTIME_THREADS */

#include "../../java.lang/thread.h" /* useful library-indep implementations */

/*
 * Class:     java_lang_Thread
 * Method:    currentThread
 * Signature: ()Ljava/lang/Thread;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Thread_currentThread
  (JNIEnv *env, jclass cls) {
  return fni_thread_currentThread(env);
}

/*
 * Class:     java_lang_Thread
 * Method:    yield
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_yield
  (JNIEnv *env, jclass cls) {
  fni_thread_yield(env);
}

/*
 * Class:     java_lang_Thread
 * Method:    sleep
 * Signature: (J)V
 */
/* causes the *currently-executing* thread to sleep */
JNIEXPORT void JNICALL Java_java_lang_Thread_sleep
  (JNIEnv *env, jclass cls, jlong millis) {
  fni_thread_sleep(env, millis, 0);
}

/*
 * Class:     java_lang_Thread
 * Method:    stop0
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_stop0
  (JNIEnv *env, jobject thr, jobject throwable) {
  fni_thread_stop(env, thr, throwable);
}

/*
 * Class:     java_lang_Thread
 * Method:    suspend0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_suspend0
  (JNIEnv *env, jobject thr) {
  fni_thread_suspend(env, thr);
}

/*
 * Class:     java_lang_Thread
 * Method:    resume0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_resume0
  (JNIEnv *env, jobject thr) {
  fni_thread_resume(env, thr);
}


#if !defined(WITH_NO_THREADS)
/*
 * Class:     java_lang_Thread
 * Method:    start
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_start
  (JNIEnv *env, jobject _this) {
  fni_thread_start(env, _this, NULL);
}
#ifdef WITH_INIT_CHECK
JNIEXPORT void JNICALL Java_java_lang_Thread_start_00024_00024initcheck
  (JNIEnv *env, jobject thisthr) {
  fni_thread_start_initcheck(env, thisthr, NULL);
}
#endif /* WITH_INIT_CHECK */

/*
 * Class:     java_lang_Thread
 * Method:    isInterrupted
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isInterrupted
  (JNIEnv *env, jobject _this, jboolean clearInterrupted) {
  return fni_thread_isInterrupted(env, _this, clearInterrupted);
}

/*
 * Class:     java_lang_Thread
 * Method:    isAlive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isAlive
  (JNIEnv *env, jobject _this) {
  return fni_thread_isAlive(env, _this);
}
#endif /* !WITH_NO_THREADS */

/*
 * Class:     java_lang_Thread
 * Method:    countStackFrames
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Thread_countStackFrames
  (JNIEnv *env, jobject _this) {
  return fni_thread_countStackFrames(env, _this);
}

/*
 * Class:     java_lang_Thread
 * Method:    setPriority0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_setPriority0
  (JNIEnv *env, jobject obj, jint pri) {
  fni_thread_setPriority(env, obj, pri);
}

/*
 * Class:     java_lang_Thread
 * Method:    interrupt0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_interrupt0
  (JNIEnv *env, jobject obj) {
  fni_thread_interrupt(env, obj);
}
#ifdef WITH_TRANSACTIONS
/* transactional version of this native method */
JNIEXPORT void JNICALL Java_java_lang_Thread_interrupt0_00024_00024withtrans
  (JNIEnv *env, jobject obj, jobject commitrec) {
  assert(0);
}
#endif /* WITH_TRANSACTIONS */

/* for compatibility with IBM JDK... */
JNIEXPORT void JNICALL Java_java_lang_Thread_newThreadEvent0
  (JNIEnv *env, jobject _this, jobject thread) {
  fprintf(stderr, "WARNING: IBM JDK may not be fully supported.\n");
}

#if defined(WITH_EVENT_DRIVEN) && !defined(WITH_USER_THREADS)
/* for use by event-driven code. */
JNIEXPORT void JNICALL Java_java_lang_Thread_EDexit
(JNIEnv *env, jobject _this) {
  /* (see also the end of thread_startup_routine() -- keep these in sync. */
  /* call Thread.exit() */
  (*env)->CallNonvirtualVoidMethod(env, _this, thrCls, exitID);
#ifdef WITH_CLUSTERED_HEAPS
  /* give us a chance to deallocate the thread-clustered heap */
  NTHR_free(_this);
#endif
}
#endif /* WITH_EVENT_DRIVEN */
