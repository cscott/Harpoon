/* Implementation for class java_net_SocketOutputStream */
#include <jni.h>
#include "java_net_SocketOutputStream.h"

#include <assert.h>

/*
 * Class:     java_net_SocketOutputStream
 * Method:    socketWrite
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL Java_java_net_SocketOutputStream_socketWrite
  (JNIEnv *env, jobject _this, jbyteArray b, jint off, jint len) {
  assert(0);
}
