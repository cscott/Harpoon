#include <jni.h>
#include <jni-private.h>

extern JNIEnv *FNI_JNIEnv; /* temporary hack. */

#define CHECK_EXCEPTIONS(env) \
if ((*env)->ExceptionOccurred(env)){ (*env)->ExceptionDescribe(env); exit(1); }

int main(int argc, char *argv[]) {
  JNIEnv *env;
  jclass cls;
  jmethodID mid;
  jobject args;
  char **namep;
  int i;
  
  env = FNI_ThreadInit();
  FNI_JNIEnv = env;

  /* Execute static initializers, in proper order. */
  for (namep=FNI_static_inits; *namep!=NULL; namep++) {
    printf("INITIALIZING: %s\n", *namep);
    if (strcmp(*namep, "java/io/File")==0)
      continue;
    cls = (*env)->FindClass(env, *namep);
    CHECK_EXCEPTIONS(env);
    mid = (*env)->GetStaticMethodID(env, cls, "<clinit>","()V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallStaticVoidMethod(env, cls, mid);
    CHECK_EXCEPTIONS(env);
    (*env)->DeleteLocalRef(env, cls);
  }

  /* Execute java.lang.System.initializeSystemClass */
  cls = (*env)->FindClass(env, "java/lang/System");
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetStaticMethodID(env, cls, "initializeSystemClass","()V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod(env, cls, mid);
  CHECK_EXCEPTIONS(env);
  (*env)->DeleteLocalRef(env, cls);

  /* Execute java.io.File initializer. */
  cls = (*env)->FindClass(env, "java/io/File");
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetStaticMethodID(env, cls, "<clinit>","()V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod(env, cls, mid);
  CHECK_EXCEPTIONS(env);
  (*env)->DeleteLocalRef(env, cls);

  /* Wrap argv strings */
  cls = (*env)->FindClass(env, "java/lang/String");
  CHECK_EXCEPTIONS(env);
  args = (*env)->NewObjectArray(env, argc-1, cls, NULL);
  CHECK_EXCEPTIONS(env);
  for (i=1; i<argc; i++) {
    jstring str = (*env)->NewStringUTF(env, argv[i]);
    CHECK_EXCEPTIONS(env);
    (*env)->SetObjectArrayElement(env, args, i-1, str);
    CHECK_EXCEPTIONS(env);
    (*env)->DeleteLocalRef(env, str);
  }
  (*env)->DeleteLocalRef(env, cls);

  /* Execute main() method. */
  cls = (*env)->FindClass(env, FNI_javamain);
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod(env, cls, mid, args);
  CHECK_EXCEPTIONS(env);
  (*env)->DeleteLocalRef(env, args);
  (*env)->DeleteLocalRef(env, cls);
}
