#include <jni.h>
#include "constants.h"

JNIEnv * FNI_ThreadInit(void); /* from jni-private.h */

int main(int argc, char *argv[]) {
  /*
  int (*f) (void *) = javamain;
  printf("%p\n", f);
  f(0);
  printf("returned.\n");
  */
  
  JNIEnv *env = FNI_ThreadInit();
  jclass cls = (*env)->FindClass(env, "Hello2");
  jmethodID mid = (*env)->GetStaticMethodID(env, cls, "main",
					    "([Ljava/lang/String;)V");
  (*env)->CallStaticVoidMethod(env, cls, mid, NULL);
}
