#include <jni.h>
#include "Hello3.h"
void Java_Hello3_hello(JNIEnv *env, jclass cls, jint i) {
  printf("Hello, world! (%d)\n", i);
}
