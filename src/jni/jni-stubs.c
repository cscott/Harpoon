#include <jni.h>

jint GetVersion(JNIEnv *env) {
  return 0x00010001; /* JNI version 1.1 */
}

/* do-nothing stubs */
jint MonitorEnter(JNIEnv *env, jobject obj) {
  return 0;
}
jint MonitorExit(JNIEnv *env, jobject obj) {
  return 0;
}
jint UnregisterNatives(JNIEnv *env, jclass clazz) {
  return 0;
}
