/* Implementation for class java_net_SocketInputStream */
#include <jni.h>
#include "java_net_SocketInputStream.h"

#include <assert.h>

/*
 * Class:     java_net_SocketInputStream
 * Method:    socketRead
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_java_net_SocketInputStream_socketRead
  (JNIEnv *env, jobject _this, jbyteArray b, jint off, jint len) {
  assert(0);
}
