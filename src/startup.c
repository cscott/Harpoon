#include <jni.h>
#include <jni-private.h>

extern JNIEnv *FNI_JNIEnv; /* temporary hack. */

#define CHECK_EXCEPTIONS(env) \
if ((*env)->ExceptionOccurred(env)){ (*env)->ExceptionDescribe(env); exit(1); }

int main(int argc, char *argv[]) {
  JNIEnv *env;
  jclass cls;
  jmethodID mid;
  char **namep;
  
  env = FNI_ThreadInit();
  FNI_JNIEnv = env;

  /* Execute static initializers, in proper order. */
  for (namep=FNI_static_inits; *namep!=NULL; namep++) {
    cls = (*env)->FindClass(env, *namep);
    CHECK_EXCEPTIONS(env);
    mid = (*env)->GetStaticMethodID(env, cls, "<clinit>","()V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallStaticVoidMethod(env, cls, mid);
    CHECK_EXCEPTIONS(env);
  }

  /* Execute java.lang.System.initializeSystemClass */
  cls = (*env)->FindClass(env, "java/lang/System");
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetStaticMethodID(env, cls, "initializeSystemClass","()V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod(env, cls, mid);
  CHECK_EXCEPTIONS(env);

  /* Wrap argv strings */

  /* Execute main() method. */
  cls = (*env)->FindClass(env, FNI_javamain);
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod(env, cls, mid, NULL);
  CHECK_EXCEPTIONS(env);
}
