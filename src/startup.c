#include <signal.h> /* for signal(), to ignore SIGPIPE */
#include <jni.h>
#include <jni-private.h>
#include "java.lang/java_lang_Thread.h"
#include "flexthread.h"

/* these functions are defined in src/java.lang/java_lang_Thread.c but only
 * used here. */
void FNI_java_lang_Thread_setupMain(JNIEnv *env);
void FNI_java_lang_Thread_finishMain(JNIEnv *env);

#define CHECK_EXCEPTIONS(env) \
if ((*env)->ExceptionOccurred(env)){ (*env)->ExceptionDescribe(env); exit(1); }

extern char *name_of_binary;

int main(int argc, char *argv[]) {
  int top_of_stack; /* special variable holding the top-of-stack position */
  char *firstclasses[] = {
    "java/util/Properties", "java/io/FileDescriptor", "java/lang/System", 
    "java/io/BufferedWriter", NULL
  };
  JNIEnv *env;
  jclass cls;
  jmethodID mid;
  jobject args;
  jclass thrCls;
  jobject mainthread;
  jthrowable threadexc;
  char **namep;
  int st=0;
  int i;
  
  /* set up for bfd */
  name_of_binary = argv[0];

  /* random package initialization */
#ifdef WITH_PTH_THREADS
  pth_init();
#endif
#ifdef WITH_USER_THREADS
  inituser(&top_of_stack);
#endif
  /* ignore SIGPIPE; we look at errno to handle this condition */
  signal(SIGPIPE, SIG_IGN);

  /* set up JNIEnv structures. */
  FNI_InitJNIEnv();
  env = FNI_CreateJNIEnv();
  ((struct FNI_Thread_State *)(env))->stack_top = &top_of_stack;
  ((struct FNI_Thread_State *)(env))->is_alive = JNI_TRUE;
  /* setup main thread info. */
  FNI_java_lang_Thread_setupMain(env);
  thrCls  = (*env)->FindClass(env, "java/lang/Thread");
  CHECK_EXCEPTIONS(env);
  mainthread = Java_java_lang_Thread_currentThread(env, thrCls);
  CHECK_EXCEPTIONS(env);

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

#ifdef WITH_STATISTICS
  /* print out collected statistics */
  { void print_statistics(void); print_statistics(); }
#endif

  /* Execute main() method. */
  cls = (*env)->FindClass(env, FNI_javamain);
  CHECK_EXCEPTIONS(env);
  mid = (*env)->GetStaticMethodID(env, cls, "main", "([Ljava/lang/String;)V");
  CHECK_EXCEPTIONS(env);
  (*env)->CallStaticVoidMethod(env, cls, mid, args);
  // handle uncaught exception in main thread. (see also thread_startup)
  if ( (threadexc = (*env)->ExceptionOccurred(env)) != NULL) {
    // call thread.getThreadGroup().uncaughtException(thread, exception)
    jclass thrGrpCls;
    jobject threadgroup;
    jmethodID gettgID, uncaughtID;
    (*env)->ExceptionClear(env); /* clear the thread's exception */
    st=1; /* main() exit status will be non-zero. */
    // Thread.currentThread().getThreadGroup()...
    gettgID = (*env)->GetMethodID(env, thrCls, "getThreadGroup",
				  "()Ljava/lang/ThreadGroup;");
    CHECK_EXCEPTIONS(env);
    threadgroup = (*env)->CallObjectMethod(env, mainthread, gettgID);
    CHECK_EXCEPTIONS(env);
    // ...uncaughtException(Thread.currentThread(), exception)
    thrGrpCls  = (*env)->FindClass(env, "java/lang/ThreadGroup");
    CHECK_EXCEPTIONS(env);
    uncaughtID = (*env)->GetMethodID(env, thrGrpCls, "uncaughtException",
				 "(Ljava/lang/Thread;Ljava/lang/Throwable;)V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallVoidMethod(env, threadgroup, uncaughtID, mainthread, threadexc);
    CHECK_EXCEPTIONS(env); // catch exception thrown by uncaughtException() ?
    (*env)->DeleteLocalRef(env, threadgroup);
    (*env)->DeleteLocalRef(env, threadexc);
  }
  (*env)->DeleteLocalRef(env, args);
  (*env)->DeleteLocalRef(env, cls);
  // call Thread.currentThread().exit() at this point.
  {
    jmethodID exitID = (*env)->GetMethodID(env, thrCls, "exit", "()V");
    CHECK_EXCEPTIONS(env);
    (*env)->CallNonvirtualVoidMethod(env, mainthread, thrCls, exitID);
    CHECK_EXCEPTIONS(env);
  }
  (*env)->DeleteLocalRef(env, thrCls);
  /* main thread is dead now. */
  ((struct FNI_Thread_State *)(env))->is_alive = JNI_FALSE;
  FNI_SetJNIData(env, mainthread, NULL, NULL); /* clear the env from the obj */
  /* Notify others that it's dead (before we deallocate the thread object!). */
  FNI_MonitorEnter(env, mainthread);
  FNI_MonitorNotify(env, mainthread, JNI_TRUE);
  FNI_MonitorExit(env, mainthread);
  // wait for all threads to finish up.
  FNI_java_lang_Thread_finishMain(env);

#ifdef WITH_STATISTICS
  /* print out collected statistics */
  { void print_statistics(void); print_statistics(); }
#endif
  return st;
}
