#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_Thread.h"

#include <assert.h>
#include "../../java.lang/thread.h" /* useful library-indep implementations */

/*
 * Class:     java_lang_Thread
 * Method:    currentThread
 * Signature: ()Ljava/lang/Thread;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Thread_currentThread
  (JNIEnv *env, jclass thrcls) {
  return fni_thread_currentThread(env);
}

/*
 * Class:     java_lang_Thread
 * Method:    yield
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_yield
  (JNIEnv *env, jclass thrcls) {
  fni_thread_yield(env, thrcls);
}

/*
 * Class:     java_lang_Thread
 * Method:    sleep
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_sleep
  (JNIEnv *env, jclass thrcls, jlong ms, jint ns) {
  fni_thread_sleep(env, thrcls, ms, ns);
}

#if !defined(WITH_NO_THREADS)
/*
 * Class:     java_lang_Thread
 * Method:    start
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_start
  (JNIEnv *env, jobject thisthr) {
  fni_thread_start(env, thisthr);
}

/*
 * Class:     java_lang_Thread
 * Method:    interrupted
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_interrupted
  (JNIEnv *env, jclass thrcls) {
  jobject thisthr = fni_thread_currentThread(env);
  return fni_thread_isInterrupted(env, thisthr, JNI_TRUE);
}

/*
 * Class:     java_lang_Thread
 * Method:    isInterrupted
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isInterrupted
  (JNIEnv *env, jobject thisthr) {
  return fni_thread_isInterrupted(env, thisthr, JNI_FALSE);
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
 * Class:     java_lang_Thread
 * Method:    holdsLock
 * Signature: (Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_holdsLock
  (JNIEnv *env, jclass thrcls, jobject obj) {
  assert(0); /* unimplemented */
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
 * Class:     java_lang_Thread
 * Method:    nativeStop
 * Signature: (Ljava/lang/Throwable;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_nativeStop
  (JNIEnv *env, jobject thisthr, jthrowable t) {
  fni_thread_stop(env, thisthr, t);
}

/*
 * Class:     java_lang_Thread
 * Method:    nativeInterrupt
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_nativeInterrupt
  (JNIEnv *env, jobject thisthr) {
  fni_thread_interrupt(env, thisthr);
}

/*
 * Class:     java_lang_Thread
 * Method:    nativeSuspend
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_nativeSuspend
  (JNIEnv *env, jobject thisthr) {
  fni_thread_suspend(env, thisthr);
}

/*
 * Class:     java_lang_Thread
 * Method:    nativeResume
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_nativeResume
  (JNIEnv *env, jobject thisthr) {
  fni_thread_resume(env, thisthr);
}

/*
 * Class:     java_lang_Thread
 * Method:    nativeSetPriority
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_nativeSetPriority
  (JNIEnv *env, jobject thisthr, jint newPriority) {
  fni_thread_setPriority(env, thisthr, newPriority);
}
