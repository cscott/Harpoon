#include <jni.h>
#include "java_lang_Thread.h"

#include <assert.h>

/*
 * Class:     java_lang_Thread
 * Method:    currentThread
 * Signature: ()Ljava/lang/Thread;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Thread_currentThread
  (JNIEnv *env, jclass cls) {
    assert(0);
}

#if 0
/*
 * Class:     java_lang_Thread
 * Method:    yield
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_yield
  (JNIEnv *, jclass);

/*
 * Class:     java_lang_Thread
 * Method:    sleep
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_sleep
  (JNIEnv *, jclass, jlong);

/*
 * Class:     java_lang_Thread
 * Method:    start
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_start
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Thread
 * Method:    isInterrupted
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isInterrupted
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     java_lang_Thread
 * Method:    isAlive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isAlive
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Thread
 * Method:    countStackFrames
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Thread_countStackFrames
  (JNIEnv *, jobject);
#endif

/*
 * Class:     java_lang_Thread
 * Method:    setPriority0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_setPriority0
  (JNIEnv *env, jobject obj, jint pri) {
    assert(0);
}

#if 0
/*
 * Class:     java_lang_Thread
 * Method:    stop0
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_stop0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_Thread
 * Method:    suspend0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_suspend0
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Thread
 * Method:    resume0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_resume0
  (JNIEnv *, jobject);

#endif
/*
 * Class:     java_lang_Thread
 * Method:    interrupt0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_interrupt0
  (JNIEnv *env, jobject obj) {
    assert(0);
}
