#include <jni.h>
#include <jni-private.h>
#include "config.h"

#include <assert.h>
#include <errno.h>
#include "flexthread.h" /* also includes thread-impl-specific headers */
#ifdef WITH_THREADS
#include <sys/time.h>
#endif
#ifdef WITH_HEAVY_THREADS
#include <sched.h> /* for sched_get_priority_min/max */
#endif
#include <stdio.h>
#include <stdlib.h>
#include <time.h> /* for nanosleep */
#include <unistd.h> /* for usleep */
#ifdef WITH_CLUSTERED_HEAPS
#include "../clheap/alloc.h" /* for NTHR_malloc_first/NTHR_free */
#endif
#include "memstats.h"
#ifdef WITH_PRECISE_GC
#include "jni-gc.h"
#ifdef WITH_THREADS
#include "jni-gcthreads.h"
#endif
#endif
#ifdef WITH_REALTIME_THREADS
#include "../realtime/RTJconfig.h" /* for RTJ_MALLOC_UNCOLLECTABLE */
#include "../realtime/threads.h"
#include "../realtime/qcheck.h"
#endif /* WITH_REALTIME_THREADS */

#include "thread.h"
#include "threadlist.h" /* maintain list of running threads */

jclass thrCls; /* clazz for java/lang/Thread. */
jfieldID priorityID; /* "priority" field in Thread object. */
jfieldID daemonID; /* "daemon" field in Thread object. */
jmethodID runID; /* Thread.run() method. */
jmethodID gettgID; /* Thread.getThreadGroup() method. */
#ifdef CLASSPATH_VERSION
jmethodID removeThreadID; /* ThreadGroup.removeThread() method. */
#else /* if !CLASSPATH_VERSION */
jmethodID exitID; /* Thread.exit() method. */
#endif /* !CLASSPATH_VERSION */
jmethodID uncaughtID; /* ThreadGroup.uncaughtException() method. */
#ifdef WITH_REALTIME_JAVA
jmethodID cleanupID; /* RealtimeThread.cleanup() method. */
#endif

/* information about priority values -- both w/in java and runtime system */
jint MIN_PRIORITY, NORM_PRIORITY, MAX_PRIORITY;
#ifdef WITH_HEAVY_THREADS
int sched_min_priority, sched_norm_priority, sched_max_priority;
#endif

/* this is used to implement pthread_cond_timedwait for pth in flexthread.h */
#ifdef WITH_PTH_THREADS
pth_key_t flex_timedwait_key = PTH_KEY_INIT;
#endif

void FNI_java_lang_Thread_setupMain(JNIEnv *env) {
  jclass thrGrpCls;
  jmethodID thrConsID;
  jstring mainStr;
  jobject mainThr, mainThrGrp;
  /* first make main thread object. */
  thrCls  = (*env)->FindClass(env,
#if defined(WITH_REALTIME_JAVA) || defined(WITH_FAKE_SCOPES)
                              "javax/realtime/RealtimeThread"
#else
                              "java/lang/Thread"
#endif
			      );
  assert(!((*env)->ExceptionOccurred(env)));
  thrConsID = (*env)->GetMethodID(env, thrCls, "<init>", "("
				  "Ljava/lang/ThreadGroup;"
				  "Ljava/lang/Runnable;"
				  "Ljava/lang/String;)V");
  assert(!((*env)->ExceptionOccurred(env)));
  mainThr = 
#ifdef WITH_CLUSTERED_HEAPS
    /* associate with thread-clustered heap */
    FNI_AllocObject_using(env, thrCls, NGBL_malloc_with_heap);
#else
    /* use default allocation strategy. */
    (*env)->AllocObject(env, thrCls);
#endif
#ifdef WITH_ROLE_INFER
    NativeassignUID(env,mainThr,thrCls);
#endif
  /* put thread in env structure as 'current thread' */
  assert(((struct FNI_Thread_State *)env)->thread == NULL);
  ((struct FNI_Thread_State *)env)->thread = mainThr;
#if defined(WITH_EVENT_DRIVEN) && !defined(WITH_USER_THREADS)
  {
    extern struct oobj *
      _Flex_harpoon_Analysis_ContBuilder_Scheduler_currentThread;
    _Flex_harpoon_Analysis_ContBuilder_Scheduler_currentThread =
      FNI_UNWRAP(mainThr);
  }
#endif

  /* okay, now that we've got an object in env, setup this thread properly: */

  /* make the name of the group and the thread. */
  mainStr = (*env)->NewStringUTF(env, "main");
  assert(!((*env)->ExceptionOccurred(env)));
  /* make main thread group object. */
  thrGrpCls  = (*env)->FindClass(env, "java/lang/ThreadGroup");
  assert(!((*env)->ExceptionOccurred(env)));
#ifdef CLASSPATH_VERSION
  { /* main thread group comes from ThreadGroup.root */
    jfieldID thrGrpRootID;
    jmethodID thrGrpClInitID = /* hopefully clinit is idempotent! */
      (*env)->GetStaticMethodID(env, thrGrpCls, "<clinit>","()V");
    assert(!((*env)->ExceptionOccurred(env)));
    (*env)->CallStaticVoidMethod(env, thrGrpCls, thrGrpClInitID);
    assert(!((*env)->ExceptionOccurred(env)));
    thrGrpRootID = (*env)->GetStaticFieldID(env, thrGrpCls, "root",
					    "Ljava/lang/ThreadGroup;");
    assert(!((*env)->ExceptionOccurred(env)));
    mainThrGrp = (*env)->GetStaticObjectField(env, thrGrpCls, thrGrpRootID);
    assert(!((*env)->ExceptionOccurred(env)));
#if !defined(WITH_INIT_CHECK)
# error This code assumes that the static initializer is idempotent.
#endif
  }
#else
  { /* have to make main thread group object */
  jmethodID thrGrpConsID =
    (*env)->GetMethodID(env, thrGrpCls, "<init>", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  mainThrGrp = (*env)->NewObject(env, thrGrpCls, thrGrpConsID);
  assert(!((*env)->ExceptionOccurred(env)));
  }
#endif /* !CLASSPATH_VERSION */
  /* get info about MIN_PRIORITY/NORM_PRIORITY/MAX_PRIORITY values */
  MIN_PRIORITY = (*env)->GetStaticIntField
    (env, thrCls, (*env)->GetStaticFieldID(env, thrCls, "MIN_PRIORITY", "I"));
  NORM_PRIORITY = (*env)->GetStaticIntField
    (env, thrCls, (*env)->GetStaticFieldID(env, thrCls, "NORM_PRIORITY", "I"));
  MAX_PRIORITY = (*env)->GetStaticIntField
    (env, thrCls, (*env)->GetStaticFieldID(env, thrCls, "MAX_PRIORITY", "I"));
  assert(!((*env)->ExceptionOccurred(env)));
#ifdef WITH_HEAVY_THREADS
  {
    struct sched_param param;
    int policy;
    pthread_getschedparam(pthread_self(), &policy, &param);
    sched_min_priority = sched_get_priority_min(policy);
    sched_norm_priority = param.sched_priority;
    sched_max_priority = sched_get_priority_max(policy);
  }
#endif
  /* make Thread object-to-JNIEnv mapping. */
  FNI_SetJNIData(env, mainThr, env, NULL);
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  ((struct FNI_Thread_State *)env)->pthread = pthread_self();
#endif
  /* as the thread constructor method uses 'current thread' as a template
   * for setting the fields of the new thread, we need to hand-kludge some
   * field settings *before* calling the constructor.  ugh. */
  priorityID = (*env)->GetFieldID(env, thrCls, "priority", "I");
  assert(!((*env)->ExceptionOccurred(env)));
  (*env)->SetIntField(env, mainThr, priorityID, NORM_PRIORITY);
  // XXX: also set up the 'daemon' field?  but it defaults to the right thing.
  /* finish constructing the thread object */
  (*env)->CallNonvirtualVoidMethod(env, mainThr, thrCls, thrConsID,
				   mainThrGrp, NULL, mainStr);
  assert(!((*env)->ExceptionOccurred(env)));
  // FIXME: set other fields, etc?
  /* lookup run() method */
  runID = (*env)->GetMethodID(env, thrCls, "run", "()V");
  /* (some classhierarchies don't include the run() method. don't sweat it. */
  if (runID==NULL) (*env)->ExceptionClear(env);
  assert(!((*env)->ExceptionOccurred(env)));
  /* lookup Thread.daemon field */
  daemonID = (*env)->GetFieldID(env, thrCls, "daemon", "Z");
  assert(!((*env)->ExceptionOccurred(env)));
  /* lookup Thread.getThreadGroup() and Thread.exit() methods */
  gettgID = (*env)->GetMethodID(env, thrCls, "getThreadGroup",
				"()Ljava/lang/ThreadGroup;");
  assert(!((*env)->ExceptionOccurred(env)));
#ifdef CLASSPATH_VERSION
  removeThreadID = (*env)->GetMethodID(env, thrGrpCls, "removeThread",
				       "(Ljava/lang/Thread;)V");
  assert(!((*env)->ExceptionOccurred(env)));
#else /* !CLASSPATH_VERSION */
  exitID = (*env)->GetMethodID(env, thrCls, "exit", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
#endif /* !CLASSPATH_VERSION */
  /* lookup ThreadGroup.uncaughtException() method */
  uncaughtID = (*env)->GetMethodID(env, thrGrpCls, "uncaughtException",
				"(Ljava/lang/Thread;Ljava/lang/Throwable;)V");
  assert(!((*env)->ExceptionOccurred(env)));
#ifdef WITH_REALTIME_JAVA
  cleanupID = (*env)->GetMethodID(env, thrCls, "cleanup", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
#endif
  /* delete all local refs except for mainThr. */
  (*env)->DeleteLocalRef(env, mainStr);
  (*env)->DeleteLocalRef(env, thrGrpCls);
  (*env)->DeleteLocalRef(env, mainThrGrp);
  (*env)->DeleteLocalRef(env, thrCls);
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  /* make pthread_key_t before the fat lady sings */
  pthread_key_create(&running_threads_key, remove_running_thread);
#endif /* WITH_HEAVY_THREADS */

  /* done! */
}

/* wait for all non-main non-daemon threads to terminate */
void FNI_java_lang_Thread_finishMain(JNIEnv *env) {
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
  wait_on_running_thread();
#endif
}
