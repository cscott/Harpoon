/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class java_io_NativeIO */

#ifndef _Included_java_io_NativeIO
#define _Included_java_io_NativeIO
#ifdef __cplusplus
extern "C" {
#endif
#undef java_io_NativeIO_O_RDONLY
#define java_io_NativeIO_O_RDONLY 0L
#undef java_io_NativeIO_O_WRONLY
#define java_io_NativeIO_O_WRONLY 1L
#undef java_io_NativeIO_O_RDWR
#define java_io_NativeIO_O_RDWR 2L
#undef java_io_NativeIO_O_NDELAY
#define java_io_NativeIO_O_NDELAY 4L
#undef java_io_NativeIO_O_APPEND
#define java_io_NativeIO_O_APPEND 8L
#undef java_io_NativeIO_O_SYNC
#define java_io_NativeIO_O_SYNC 16L
#undef java_io_NativeIO_O_NONBLOCK
#define java_io_NativeIO_O_NONBLOCK 128L
#undef java_io_NativeIO_O_CREAT
#define java_io_NativeIO_O_CREAT 256L
#undef java_io_NativeIO_O_TRUNC
#define java_io_NativeIO_O_TRUNC 512L
#undef java_io_NativeIO_O_EXCL
#define java_io_NativeIO_O_EXCL 1024L
#undef java_io_NativeIO_O_NOCTTY
#define java_io_NativeIO_O_NOCTTY 2048L
#undef java_io_NativeIO_S_IRUSR
#define java_io_NativeIO_S_IRUSR 256L
#undef java_io_NativeIO_S_IWUSR
#define java_io_NativeIO_S_IWUSR 128L
#undef java_io_NativeIO_S_IXUSR
#define java_io_NativeIO_S_IXUSR 64L
#undef java_io_NativeIO_S_IRGRP
#define java_io_NativeIO_S_IRGRP 32L
#undef java_io_NativeIO_S_IWGRP
#define java_io_NativeIO_S_IWGRP 16L
#undef java_io_NativeIO_S_IXGRP
#define java_io_NativeIO_S_IXGRP 8L
#undef java_io_NativeIO_S_IROTH
#define java_io_NativeIO_S_IROTH 4L
#undef java_io_NativeIO_S_IWOTH
#define java_io_NativeIO_S_IWOTH 2L
#undef java_io_NativeIO_S_IXOTH
#define java_io_NativeIO_S_IXOTH 1L
#undef java_io_NativeIO_S_ISUID
#define java_io_NativeIO_S_ISUID 2048L
#undef java_io_NativeIO_S_ISGID
#define java_io_NativeIO_S_ISGID 1024L
#undef java_io_NativeIO_S_ISVTX
#define java_io_NativeIO_S_ISVTX 512L
#undef java_io_NativeIO_S_ENFMT
#define java_io_NativeIO_S_ENFMT 1024L
#undef java_io_NativeIO_EOF
#define java_io_NativeIO_EOF -1L
#undef java_io_NativeIO_ERROR
#define java_io_NativeIO_ERROR -2L
#undef java_io_NativeIO_TRYAGAIN
#define java_io_NativeIO_TRYAGAIN -3L
#undef java_io_NativeIO_BUFFERFULL
#define java_io_NativeIO_BUFFERFULL -4L
#undef java_io_NativeIO_MOD_SELECT
#define java_io_NativeIO_MOD_SELECT 0L
#undef java_io_NativeIO_MOD_SIGNAL
#define java_io_NativeIO_MOD_SIGNAL 1L
/*
 * Class:     java_io_NativeIO
 * Method:    canAcceptJNI
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_NativeIO_canAcceptJNI
  (JNIEnv *, jclass, jint);

/*
 * Class:     java_io_NativeIO
 * Method:    getCharJNI
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_java_io_NativeIO_getCharJNI
  (JNIEnv *, jclass, jint);

/*
 * Class:     java_io_NativeIO
 * Method:    getFDs
 * Signature: ([I)I
 */
JNIEXPORT jint JNICALL Java_java_io_NativeIO_getFDs
  (JNIEnv *, jclass, jintArray);

/*
 * Class:     java_io_NativeIO
 * Method:    getFDsSmart
 * Signature: (Z[I)I
 */
JNIEXPORT jint JNICALL Java_java_io_NativeIO_getFDsSmart
  (JNIEnv *, jclass, jboolean, jintArray);

/*
 * Class:     java_io_NativeIO
 * Method:    initScheduler
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_NativeIO_initScheduler
  (JNIEnv *, jclass, jint);

/*
 * Class:     java_io_NativeIO
 * Method:    makeNonBlockJNI
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_NativeIO_makeNonBlockJNI
  (JNIEnv *, jclass, jint);

/*
 * Class:     java_io_NativeIO
 * Method:    putCharJNI
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_java_io_NativeIO_putCharJNI
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     java_io_NativeIO
 * Method:    readJNI
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_java_io_NativeIO_readJNI
  (JNIEnv *, jclass, jint, jbyteArray, jint, jint);

/*
 * Class:     java_io_NativeIO
 * Method:    registerRead
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_NativeIO_registerRead
  (JNIEnv *, jclass, jint);

/*
 * Class:     java_io_NativeIO
 * Method:    registerWrite
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_io_NativeIO_registerWrite
  (JNIEnv *, jclass, jint);

/*
 * Class:     java_io_NativeIO
 * Method:    selectJNI
 * Signature: ([I[I)[Z
 */
JNIEXPORT jbooleanArray JNICALL Java_java_io_NativeIO_selectJNI
  (JNIEnv *, jclass, jintArray, jintArray);

/*
 * Class:     java_io_NativeIO
 * Method:    writeJNI
 * Signature: (I[BII)I
 */
JNIEXPORT jint JNICALL Java_java_io_NativeIO_writeJNI
  (JNIEnv *, jclass, jint, jbyteArray, jint, jint);

#ifdef __cplusplus
}
#endif
#endif