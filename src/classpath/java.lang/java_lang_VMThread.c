#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_Thread.h"

#include <assert.h>
#include <stdlib.h> /* for fprintf, stderr */
#include "../../java.lang/thread.h" /* useful library-indep implementations */

/*
 * Class:     java_lang_VMThread
 * Method:    currentThread
 * Signature: ()Ljava/lang/Thread;
 */
JNIEXPORT jobject JNICALL Java_java_lang_VMThread_currentThread
  (JNIEnv *env, jclass thrcls) {
  return fni_thread_currentThread(env);
}

/*
 * Class:     java_lang_VMThread
 * Method:    yield
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_VMThread_yield
  (JNIEnv *env, jclass thrcls) {
  fni_thread_yield(env);
}

/*
 * Class:     java_lang_VMThread
 * Method:    sleep
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMThread_sleep
  (JNIEnv *env, jclass thrcls, jlong ms, jint ns) {
  fni_thread_sleep(env, ms, ns);
}

#if !defined(WITH_NO_THREADS)
/*
 * Class:     java_lang_VMThread
 * Method:    start
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_VMThread_start
  (JNIEnv *env, jobject thisvmthr, jlong stksize) {
  fni_thread_start(env, fni_vmthread_thread(env,thisvmthr), thisvmthr);
}
#ifdef WITH_INIT_CHECK
JNIEXPORT void JNICALL Java_java_lang_VMThread_start_00024_00024initcheck
  (JNIEnv *env, jobject thisvmthr) {
  fni_thread_start_initcheck(env, fni_vmthread_thread(env,thisvmthr), thisvmthr);
}
#endif /* WITH_INIT_CHECK */

/*
 * Class:     java_lang_VMThread
 * Method:    interrupted
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_VMThread_interrupted
  (JNIEnv *env, jclass thrcls) {
  jobject thisthr = fni_thread_currentThread(env);
  return fni_thread_isInterrupted(env, thisthr, JNI_TRUE);
}

/*
 * Class:     java_lang_VMThread
 * Method:    isInterrupted
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_VMThread_isInterrupted
  (JNIEnv *env, jobject thisthr) {
  return fni_thread_isInterrupted(env,fni_vmthread_thread(env,thisthr), JNI_FALSE);
}

/*
 * Class:     java_lang_Thread
 * Method:    isAlive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isAlive
  (JNIEnv *env, jobject thisthr) {
  return fni_thread_isAlive(env, thisthr);
}
#endif /* !WITH_NO_THREADS */

/*
 * Class:     java_lang_Thread
 * Method:    countStackFrames
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Thread_countStackFrames
  (JNIEnv *env, jobject thisthr) {
  return fni_thread_countStackFrames(env, thisthr);
}

  /**
   * Checks whether the current thread holds the monitor on a given object.
   * This allows you to do <code>assert Thread.holdsLock(obj)</code>.
   *
   * @param obj the object to check
   * @return true if the current thread is currently synchronized on obj
   * @throws NullPointerException if obj is null
   * @since 1.4
   */
/*
 * Class:     java_lang_VMThread
 * Method:    holdsLock
 * Signature: (Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_VMThread_holdsLock
  (JNIEnv *env, jclass thrcls, jobject obj) {
  assert(0); /* unimplemented */
  return JNI_TRUE; /* most likely to satisfy the assertion */
}

  /**
   * Whatever native initialization must be done in the constructor.
   *
   * @param size the requested stack size; may be ignored, and 0 signifies the
   *        default amount
   * @see #Thread(ThreadGroup, Runnable, String, long)
   */
/*
 * Class:     java_lang_Thread
 * Method:    nativeInit
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_nativeInit
  (JNIEnv *env, jobject thisthr, jlong size) {
  /* no native init required? */
}

  /**
   * Stop a thread by throwing the given exception.
   *
   * @param t the exception to throw, non-null
   * @see #stop(Throwable)
   */
/*
 * Class:     java_lang_VMThread
 * Method:    nativeStop
 * Signature: (Ljava/lang/Throwable;)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMThread_nativeStop
  (JNIEnv *env, jobject thisthr, jthrowable t) {
  fni_thread_stop(env, fni_vmthread_thread(env,thisthr), t);
}

/*
 * Class:     java_lang_VMThread
 * Method:    interrupt
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_VMThread_interrupt
  (JNIEnv *env, jobject thisthr) {
  fni_thread_interrupt(env, fni_vmthread_thread(env,thisthr));
}

/*
 * Class:     java_lang_VMThread
 * Method:    suspend
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_VMThread_suspend
  (JNIEnv *env, jobject thisthr) {
  fni_thread_suspend(env, fni_vmthread_thread(env,thisthr));
}

/*
 * Class:     java_lang_VMThread
 * Method:    resume
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_VMThread_resume
  (JNIEnv *env, jobject thisthr) {
  fni_thread_resume(env, fni_vmthread_thread(env,thisthr));
}

/*
 * Class:     java_lang_VMThread
 * Method:    nativeSetPriority
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMThread_nativeSetPriority
  (JNIEnv *env, jobject thisthr, jint newPriority) {
  fni_thread_setPriority(env, fni_vmthread_thread(env,thisthr), newPriority);
}
