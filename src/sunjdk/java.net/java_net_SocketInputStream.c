/* Implementation for class java_net_SocketInputStream */
#include <jni.h>
#include "java_net_SocketInputStream.h"
#include "../java.io/java_io_FileInputStream.h"

/*
 * Class:     java_net_SocketInputStream
 * Method:    socketRead
 * Signature: ([BII)I
 */
JNIEXPORT jint JNICALL Java_java_net_SocketInputStream_socketRead
  (JNIEnv *env, jobject _this, jbyteArray ba, jint start, jint len) {
  // steal implementation from java_io_FileInputStream, verbatim.
  // (note, on windows platforms sockets are not the same as file
  // descriptors, so this would have to be different)
  return Java_java_io_FileInputStream_readBytes(env, _this, ba, start, len);
}
