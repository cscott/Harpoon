#include "java_io_ObjectInputStream.h"
#include "config.h" /* for WITH_INIT_CHECK */
#include <assert.h>

/*
 * Class:     java_io_ObjectInputStream
 * Method:    loadClass0
 * Signature: (Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_io_ObjectInputStream_loadClass0
  (JNIEnv *env, jobject obj, jclass cls, jstring str) {
    assert(0);
}

/*
 * Class:     java_io_ObjectInputStream
 * Method:    inputClassFields
 * Signature: (Ljava/lang/Object;Ljava/lang/Class;[I)V
 */
JNIEXPORT void JNICALL Java_java_io_ObjectInputStream_inputClassFields
  (JNIEnv *env, jobject obj1, jobject obj2, jclass cls, jintArray iarr) {
    assert(0);
}

/*
 * Class:     java_io_ObjectInputStream
 * Method:    allocateNewObject
 * Signature: (Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_io_ObjectInputStream_allocateNewObject
  (JNIEnv *env, jclass cls1, jclass cls2, jclass cls3) {
    assert(0);
}

/*
 * Class:     java_io_ObjectInputStream
 * Method:    allocateNewArray
 * Signature: (Ljava/lang/Class;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_io_ObjectInputStream_allocateNewArray
  (JNIEnv *env, jclass cls1, jclass cls2, jint i) {
    assert(0);
}

/*
 * Class:     java_io_ObjectInputStream
 * Method:    invokeObjectReader
 * Signature: (Ljava/lang/Object;Ljava/lang/Class;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_ObjectInputStream_invokeObjectReader
  (JNIEnv *env, jobject obj1, jobject obj2, jclass cls) {
    assert(0);
}

/*------------------------------------------------------------------*/
#ifdef WITH_INIT_CHECK
/*
 * Class:     java_io_ObjectInputStream
 * Method:    loadClass0
 * Signature: (Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_io_ObjectInputStream_loadClass0_00024_00024initcheck
  (JNIEnv *env, jobject obj, jclass cls, jstring str) {
    assert(0);
}

/*
 * Class:     java_io_ObjectInputStream
 * Method:    inputClassFields
 * Signature: (Ljava/lang/Object;Ljava/lang/Class;[I)V
 */
JNIEXPORT void JNICALL Java_java_io_ObjectInputStream_inputClassFields_00024_00024initcheck
  (JNIEnv *env, jobject obj1, jobject obj2, jclass cls, jintArray iarr) {
    assert(0);
}

/*
 * Class:     java_io_ObjectInputStream
 * Method:    allocateNewObject
 * Signature: (Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_io_ObjectInputStream_allocateNewObject_00024_00024initcheck
  (JNIEnv *env, jclass cls1, jclass cls2, jclass cls3) {
    assert(0);
}

/*
 * Class:     java_io_ObjectInputStream
 * Method:    allocateNewArray
 * Signature: (Ljava/lang/Class;I)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_io_ObjectInputStream_allocateNewArray_00024_00024initcheck
  (JNIEnv *env, jclass cls1, jclass cls2, jint i) {
    assert(0);
}

/*
 * Class:     java_io_ObjectInputStream
 * Method:    invokeObjectReader
 * Signature: (Ljava/lang/Object;Ljava/lang/Class;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_ObjectInputStream_invokeObjectReader_00024_00024initcheck
  (JNIEnv *env, jobject obj1, jobject obj2, jclass cls) {
    assert(0);
}
#endif /* WITH_INIT_CHECK */
