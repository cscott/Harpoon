#include <jni.h>
#include "constants.h"

extern struct JNINativeInterface FLEX_JNI_table;
JNIEnv Fenv = &FLEX_JNI_table;

int main(int argc, char *argv[]) {
  /*
  int (*f) (void *) = javamain;
  printf("%p\n", f);
  f(0);
  printf("returned.\n");
  */
  
  JNIEnv *env = &Fenv;
  jclass cls = (*env)->FindClass(env, "Hello2");
  jmethodID mid = (*env)->GetStaticMethodID(env, cls, "main",
					    "([Ljava/lang/String;)V");
  (*env)->CallStaticVoidMethod(env, cls, mid, NULL);
}
