#include <jni.h>
#include <jni-private.h>
#include "flexthread.h"

/* these functions are defined in src/java.lang/java_lang_Thread.c but only
 * used here. */
void FNI_java_lang_Thread_setupMain(JNIEnv *env);
void FNI_java_lang_Thread_finishMain(JNIEnv *env);

#define CHECK_EXCEPTIONS(env) \
if ((*env)->ExceptionOccurred(env)){ (*env)->ExceptionDescribe(env); exit(1); }

int main(int argc, char *argv[]) {
  char *firstclasses[] = {
    "java/util/Properties", "java/io/FileDescriptor", "java/lang/System", 
    "java/io/BufferedWriter", NULL
  };
  JNIEnv *env;
  jclass cls;
  jmethodID mid;
  jobject args;
  char **namep;
  int i;
  
  /* random package initialization */
#ifdef WITH_PTH_THREADS
  pth_init();
#endif

  /* set up JNIEnv structures. */
  FNI_InitJNIEnv();
  env = FNI_CreateJNIEnv();
  ((struct FNI_Thread_State *)(env))->stack_top = FNI_STACK_TOP();
  /* setup main thread info. */
  FNI_java_lang_Thread_setupMain(env);

  /* initialize pre-System.initializeSystemClass() initializers. */
  for (i=0; firstclasses[i]!=NULL; i++) {
    cls = (*env)->FindClass(env, firstclasses[i]);
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

  /* Execute rest of static initializers, in proper order. */
  for (namep=FNI_static_inits; *namep!=NULL; namep++) {
    for (i=0; firstclasses[i]!=NULL; i++)
      if (strcmp(*namep, firstclasses[i])==0)
	goto skip;
    cls = (*env)->FindClass(env, *namep);
    CHECK_EXCEPTIONS(env);
    mid = (*env)->GetStaticMethodID(env, cls, "<clinit>","()V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallStaticVoidMethod(env, cls, mid);
    CHECK_EXCEPTIONS(env);
    (*env)->DeleteLocalRef(env, cls);
  skip:
  }

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

#ifdef WITH_CLUSTERED_HEAPS
  /* print out allocation statistics */
  { void print_statistics(void); print_statistics(); }
#endif

  /* Execute main() method. */
  cls = (*env)->FindClass(env, FNI_javamain);
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod(env, cls, mid, args);
  // XXX on exception should call
  //   Thread.currentThread().getThreadGroup().uncaughtException(thread, ex)
  CHECK_EXCEPTIONS(env);
  (*env)->DeleteLocalRef(env, args);
  (*env)->DeleteLocalRef(env, cls);
  // XXX: should call Thread.currentThread().exit() at this point.
  FNI_java_lang_Thread_finishMain(env);

#ifdef WITH_CLUSTERED_HEAPS
  /* print out allocation statistics */
  { void print_statistics(void); print_statistics(); }
#endif
}
