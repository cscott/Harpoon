/* Implementation for class java_net_SocketOutputStream */
#include <jni.h>
#include "java_net_SocketOutputStream.h"
#include "../java.io/java_io_FileOutputStream.h"

/*
 * Class:     java_net_SocketOutputStream
 * Method:    socketWrite
 * Signature: ([BII)V
 */
// stolen from src/java.io/java_io_FileOutputStream.c
JNIEXPORT void JNICALL Java_java_net_SocketOutputStream_socketWrite
  (JNIEnv *env, jobject _this, jbyteArray ba, jint start, jint len) {
  // steal implementation from java_io_FileOutputStream, verbatim.
  // (note, on windows platforms sockets are not the same as file
  // descriptors, so this would have to be different)
  return Java_java_io_FileOutputStream_writeBytes(env, _this, ba, start, len);
}
