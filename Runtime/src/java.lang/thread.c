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
#ifdef CLASSPATH_VERSION /* >= 0.08 */
jclass vmThrCls; /* clazz for java/lang/VMThread. */
jfieldID vmThrID; /* Thread.vmThread field. */
jfieldID vmThrThrID; /* VMThread.thread field. */
#else /* if !CLASSPATH_VERSION */
jmethodID gettgID; /* Thread.getThreadGroup() method. */
jmethodID exitID; /* Thread.exit() method. */
jmethodID uncaughtID; /* ThreadGroup.uncaughtException() method. */
#endif /* !CLASSPATH_VERSION */
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

#ifdef WITH_INIT_CHECK
int initDone = 0;
#endif

void FNI_java_lang_Thread_setupMain(JNIEnv *env) {
#ifndef WITH_REALTIME_JAVA
  jclass thrGrpCls;
#endif
  jmethodID thrConsID;
  jstring mainStr;
  jobject mainThr;
#ifndef WITH_REALTIME_JAVA
  jobject mainThrGrp;
  jobject vmThr; /* only used by classpath */
#endif
  /* first make main thread object. */
  thrCls  = (*env)->FindClass(env,
#if defined(WITH_REALTIME_JAVA) || defined(WITH_FAKE_SCOPES)
                              "javax/realtime/RealtimeThread"
#else
                              "java/lang/Thread"
#endif
			      );
  assert(!((*env)->ExceptionOccurred(env)));
  thrCls = (*env)->NewGlobalRef(env, thrCls);
  thrConsID = (*env)->GetMethodID(env, thrCls, "<init>", "("
#if defined(CLASSPATH_VERSION) /* xxx: should check version >= 0.08 */
				  "Ljava/lang/VMThread;"
				  "Ljava/lang/String;"
				  "IZ"
#else
				  "Ljava/lang/ThreadGroup;"
				  "Ljava/lang/Runnable;"
				  "Ljava/lang/String;"
#endif
				  ")V");
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
#if !defined(WITH_REALTIME_JAVA)
  /* make main thread group object. */
  thrGrpCls  = (*env)->FindClass(env, "java/lang/ThreadGroup");
  assert(!((*env)->ExceptionOccurred(env)));
#endif
#ifdef CLASSPATH_VERSION
  { /* main thread group comes from ThreadGroup.root */
#if !defined(WITH_REALTIME_JAVA)
    jfieldID thrGrpRootID;
    jmethodID thrGrpClInitID = /* hopefully clinit is idempotent! */
      (*env)->GetStaticMethodID(env, thrGrpCls, "<clinit>","()V");
    assert(!((*env)->ExceptionOccurred(env)));
    (*env)->CallStaticVoidMethod(env, thrGrpCls, thrGrpClInitID);
    assert(!((*env)->ExceptionOccurred(env)));

    //printf("java.lang.ThreadGroup.<clinit> successfully executed\n");

    thrGrpRootID = (*env)->GetStaticFieldID(env, thrGrpCls, "root",
					    "Ljava/lang/ThreadGroup;");
    assert(!((*env)->ExceptionOccurred(env)));
    mainThrGrp = (*env)->GetStaticObjectField(env, thrGrpCls, thrGrpRootID);
    assert(!((*env)->ExceptionOccurred(env)));
#endif
#if !defined(WITH_INIT_CHECK)
# error This code assumes that the static initializer is idempotent.
#endif
  }
#else
  { /* have to make main thread group object */
#ifndef WITH_REALTIME_JAVA
  jmethodID thrGrpConsID =
    (*env)->GetMethodID(env, thrGrpCls, "<init>", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  mainThrGrp = (*env)->NewObject(env, thrGrpCls, thrGrpConsID);
  assert(!((*env)->ExceptionOccurred(env)));
#endif
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
  priorityID = (*env)->GetFieldID(env, thrCls, "priority", "I");
  assert(!((*env)->ExceptionOccurred(env)));
#if !defined(CLASSPATH_VERSION)
  /* as the thread constructor method uses 'current thread' as a template
   * for setting the fields of the new thread, we need to hand-kludge some
   * field settings *before* calling the constructor.  ugh. */
  (*env)->SetIntField(env, mainThr, priorityID, NORM_PRIORITY);
  // XXX: also set up the 'daemon' field?  but it defaults to the right thing.
#endif
#ifdef CLASSPATH_VERSION
  // for classpath: create VMThread; add this to thread group and
  // inheritablethreadlocal
  {
      // Create VMThread
      vmThrCls = (*env)->FindClass(env, "java/lang/VMThread");
      jmethodID vmThrConsID = (*env)->GetMethodID(env, vmThrCls, "<init>",
						  "(Ljava/lang/Thread;)V");
      vmThr = (*env)->NewObject(env, vmThrCls, vmThrConsID, mainThr);
      assert(!((*env)->ExceptionOccurred(env)));
      vmThrCls = (*env)->NewGlobalRef(env, vmThrCls);
  }
  {
      // Add thread to thread group
      jfieldID thrGroupID;
      jmethodID thrGrpAddID;
      thrGrpAddID = (*env)->GetMethodID(env, thrGrpCls, "addThread",
					"(Ljava/lang/Thread;)V");
      (*env)->CallVoidMethod(env, mainThrGrp, thrGrpAddID, mainThr);
      assert(!((*env)->ExceptionOccurred(env)));
      thrGroupID = (*env)->GetFieldID(env, thrCls, "group",
				      "Ljava/lang/ThreadGroup;");
      (*env)->SetObjectField(env, mainThr, thrGroupID, mainThrGrp);
      assert(!((*env)->ExceptionOccurred(env)));
  }
 {
 }
  {
    // Initialize InheritableThreadLocal, then add mainThr.
    jclass itlCls; jmethodID itlInitID;
    itlCls  = (*env)->FindClass(env, "java/lang/InheritableThreadLocal");
    if ((*env)->ExceptionOccurred(env)) {
      /* "minilib" version of classpath library omits this class.
       * don't worry about initializing it, then. */
      (*env)->ExceptionClear(env);
    } else {
      itlInitID = /* hopefully clinit is idempotent! */
	(*env)->GetStaticMethodID(env, itlCls, "<clinit>","()V");
      assert(!((*env)->ExceptionOccurred(env)));
      (*env)->CallStaticVoidMethod(env, itlCls, itlInitID);
      assert(!((*env)->ExceptionOccurred(env)));
      // XXX: should we call ITL.newChildThread(mainThr) here?
      (*env)->DeleteLocalRef(env, itlCls);
    }
  }
#endif /* CLASSPATH_VERSION */
  /* finish constructing the thread object */
  (*env)->CallNonvirtualVoidMethod(env, mainThr, thrCls, thrConsID,
#ifdef CLASSPATH_VERSION
				   vmThr, mainStr, NORM_PRIORITY, JNI_FALSE
#elif !defined(WITH_REALTIME_JAVA)
				   mainThrGrp, NULL, mainStr
#else
				   NULL, NULL, mainStr
#endif
				   );
  assert(!((*env)->ExceptionOccurred(env)));
  // FIXME: set other fields, etc?
  /* lookup run() method */
  runID = (*env)->GetMethodID(env,
#ifdef CLASSPATH_VERSION /* >= 0.08 */
			      vmThrCls,
#else
			      thrCls,
#endif
			      "run", "()V");
  /* (some classhierarchies don't include the run() method. don't sweat it. */
  if (runID==NULL) (*env)->ExceptionClear(env);
  assert(!((*env)->ExceptionOccurred(env)));
  /* lookup Thread.daemon field */
  daemonID = (*env)->GetFieldID(env, thrCls, "daemon", "Z");
  assert(!((*env)->ExceptionOccurred(env)));
#ifdef CLASSPATH_VERSION
#ifndef WITH_REALTIME_JAVA
  vmThrID = (*env)->GetFieldID(env, thrCls, "vmThread", "Ljava/lang/VMThread;");
  assert(!((*env)->ExceptionOccurred(env)));
  vmThrThrID = (*env)->GetFieldID(env, vmThrCls, "thread", "Ljava/lang/Thread;");
  assert(!((*env)->ExceptionOccurred(env)));
#endif
#else /* !CLASSPATH_VERSION */
  /* lookup Thread.getThreadGroup() and Thread.exit() methods */
#ifndef WITH_REALTIME_JAVA
  gettgID = (*env)->GetMethodID(env, thrCls, "getThreadGroup",
				"()Ljava/lang/ThreadGroup;");
  assert(!((*env)->ExceptionOccurred(env)));
#endif
  exitID = (*env)->GetMethodID(env, thrCls, "exit", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  /* lookup ThreadGroup.uncaughtException() method */
#ifndef WITH_REALTIME_JAVA
  uncaughtID = (*env)->GetMethodID(env, thrGrpCls, "uncaughtException",
				"(Ljava/lang/Thread;Ljava/lang/Throwable;)V");
  assert(!((*env)->ExceptionOccurred(env)));
#else
  cleanupID = (*env)->GetMethodID(env, thrCls, "cleanup", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
#endif
#endif /* !CLASSPATH_VERSION */
  /* delete all local refs except for mainThr. */
#ifdef CLASSPATH_VERSION
  (*env)->DeleteLocalRef(env, vmThr);
#endif
  (*env)->DeleteLocalRef(env, mainStr);
#if !defined(WITH_REALTIME_JAVA)
  (*env)->DeleteLocalRef(env, thrGrpCls);
  (*env)->DeleteLocalRef(env, mainThrGrp);
#endif
  (*env)->DeleteLocalRef(env, thrCls);
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  /* make pthread_key_t before the fat lady sings */
  pthread_key_create(&running_threads_key, remove_running_thread);
#endif /* WITH_HEAVY_THREADS */

  /* done! */
}

/* CSA: don't know why this block is necessary; it looks plain wrong to
   me, as threads are forced to share a single errno and will race over it.
   Further, modern glibc emits a complaint on standard err when this
   stuff is included.   CSA: 14-nov-2003 */
#if defined(WITH_HEAVY_THREADS) && defined(GLIBC_COMPAT2) && defined(WBEEBEE)
#undef errno
extern int errno;
int* __errno_location() {
  return &errno;
}
#endif

/* wait for all non-main non-daemon threads to terminate */
void FNI_java_lang_Thread_finishMain(JNIEnv *env) {
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
  wait_on_running_thread();
#endif
}

#if defined(WITH_INIT_CHECK) && defined(WITH_THREADS)
/* functions for dealing with threads deferred during static initialization */
static struct deferred_thread_list {
  jobject thread;
  jobject vmthread;
  struct deferred_thread_list *next;
} *deferred_threads = NULL;

void fni_thread_addDeferredThread(JNIEnv *env, jobject thread, jobject vmthread) {
  struct deferred_thread_list *dtl = malloc(sizeof(*dtl));
  dtl->thread = (*env)->NewGlobalRef(env, thread);
  dtl->vmthread = (*env)->NewGlobalRef(env, vmthread);
  /* this interchange is safe from races because only one thread is running
   * during static initialization. */
  dtl->next = deferred_threads;
  deferred_threads = dtl;
}
void fni_thread_startDeferredThreads(JNIEnv *env) {
  struct deferred_thread_list *dtl;
  while (deferred_threads!=NULL) {
    /* advance deferred threads list */
    dtl = deferred_threads;
    deferred_threads = dtl->next;
    /* okay, now start the thread */
    fni_thread_start(env, dtl->thread, dtl->vmthread);
    /* now free the global ref */
    (*env)->DeleteGlobalRef(env, dtl->thread);
    (*env)->DeleteGlobalRef(env, dtl->vmthread);
    /* finally, free the thread list object */
    free(dtl);
  }
  /* done! */
}
#endif /* WITH_INIT_CHECK && WITH_THREADS */
