#include <jni.h>
#include "constants.h"

JNIEnv * FNI_ThreadInit(void); /* from jni-private.h */
extern JNIEnv *FNI_JNIEnv; /* temporary hack. */

int main(int argc, char *argv[]) {
  JNIEnv *env;
  jclass cls;
  jmethodID mid;
  
  env = FNI_ThreadInit();
  FNI_JNIEnv = env;
  cls = (*env)->FindClass(env, "Hello3");
  mid = (*env)->GetStaticMethodID(env, cls, "main",
				  "([Ljava/lang/String;)V");
  (*env)->CallStaticVoidMethod(env, cls, mid, NULL);
  if ((*env)->ExceptionOccurred(env)!=NULL)
    (*env)->ExceptionDescribe(env);
}
