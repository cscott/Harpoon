#include <jni.h>
#include <jni-private.h>
#include "java_lang_Thread.h"

#include <assert.h>
#include "config.h"
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

#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
#define EXTRACT_OTHER_ENV(env, thread) \
  ( (struct FNI_Thread_State *) FNI_GetJNIData(env, thread) )
#define EXTRACT_PTHREAD_T(env, thread) \
  ( EXTRACT_OTHER_ENV(env, thread)->pthread )

struct thread_list {
  struct thread_list *prev;
  pthread_t pthread;
  struct thread_list *next;
};

static struct thread_list running_threads = { NULL, 0, NULL }; /*header node*/
static pthread_key_t running_threads_key;
static pthread_mutex_t running_threads_mutex = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t running_threads_cond = PTHREAD_COND_INITIALIZER;

static void add_running_thread(const pthread_t thr) {
  /* safe to use malloc -- no pointers to garbage collected memory in here */
  struct thread_list *nlist = malloc(sizeof(struct thread_list));
  INCREMENT_MALLOC(sizeof(struct thread_list));
  nlist->prev = &running_threads;
  nlist->pthread = thr;
  pthread_mutex_lock(&running_threads_mutex);
  nlist->next = running_threads.next;
  if (nlist->next) nlist->next->prev = nlist;
  running_threads.next = nlist;
  pthread_mutex_unlock(&running_threads_mutex);
  pthread_setspecific(running_threads_key, nlist);
}
static void remove_running_thread(void *cl) {
  struct thread_list *nlist = (struct thread_list *) cl;
  pthread_mutex_lock(&running_threads_mutex);
  if (nlist->prev) nlist->prev->next = nlist->next;
  if (nlist->next) nlist->next->prev = nlist->prev;
  pthread_cond_signal(&running_threads_cond);
  pthread_mutex_unlock(&running_threads_mutex);
  free(nlist);
  DECREMENT_MALLOC(sizeof(struct thread_list));
}  
static void wait_on_running_thread() {
  pthread_mutex_lock(&running_threads_mutex);
  while (running_threads.next != NULL)
    pthread_cond_wait(&running_threads_cond, &running_threads_mutex);
  pthread_mutex_unlock(&running_threads_mutex);
}

#endif /* WITH_HEAVY_THREADS || WITH_PTH_THREADS */

#if WITH_USER_THREADS
#define EXTRACT_OTHER_ENV(env, thread) \
  ( (struct FNI_Thread_State *) FNI_GetJNIData(env, thread) )
#define EXTRACT_PTHREAD_T(env, thread) \
  ( EXTRACT_OTHER_ENV(env, thread)->pthread )


static pthread_mutex_t running_threads_mutex = PTHREAD_MUTEX_INITIALIZER;
static pthread_cond_t running_threads_cond = PTHREAD_COND_INITIALIZER;

static void add_running_thread(const pthread_t thr) {
}

static void remove_running_thread() {
  pthread_mutex_lock(&running_threads_mutex);
  pthread_cond_signal(&running_threads_cond);
  pthread_mutex_unlock(&running_threads_mutex);
}

static void wait_on_running_thread() {
  pthread_mutex_lock(&running_threads_mutex);
  while((gtl!=gtl->next)||(ioptr!=NULL)) {
    pthread_cond_wait(&running_threads_cond, &running_threads_mutex);
  }
  pthread_mutex_unlock(&running_threads_mutex);
}

#endif /* WITH_USER_THREADS */





static jclass thrCls; /* clazz for java/lang/Thread. */
static jfieldID priorityID; /* "priority" field in Thread object. */
static jfieldID daemonID; /* "daemon" field in Thread object. */
static jmethodID runID; /* Thread.run() method. */
static jmethodID gettgID; /* Thread.getThreadGroup() method. */
static jmethodID exitID; /* Thread.exit() method. */
static jmethodID uncaughtID; /* ThreadGroup.uncaughtException() method. */

/* information about priority values -- both w/in java and runtime system */
static jint MIN_PRIORITY, NORM_PRIORITY, MAX_PRIORITY;
#ifdef WITH_HEAVY_THREADS
static int sched_min_priority, sched_norm_priority, sched_max_priority;
#endif

/* this is used to implement pthread_cond_timedwait for pth in flexthread.h */
#ifdef WITH_PTH_THREADS
pth_key_t flex_timedwait_key = PTH_KEY_INIT;
#endif

void FNI_java_lang_Thread_setupMain(JNIEnv *env) {
  jclass thrGrpCls;
  jmethodID thrConsID, thrGrpConsID;
  jfieldID thrNPID;
  jstring mainStr;
  jobject mainThr, mainThrGrp;
  /* first make main thread object. */
  thrCls  = (*env)->FindClass(env,
#ifdef WITH_REALTIME_JAVA
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
  thrGrpConsID = (*env)->GetMethodID(env, thrGrpCls, "<init>", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  mainThrGrp = (*env)->NewObject(env, thrGrpCls, thrGrpConsID);
  assert(!((*env)->ExceptionOccurred(env)));
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
  exitID = (*env)->GetMethodID(env, thrCls, "exit", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  /* lookup ThreadGroup.uncaughtException() method */
  uncaughtID = (*env)->GetMethodID(env, thrGrpCls, "uncaughtException",
				"(Ljava/lang/Thread;Ljava/lang/Throwable;)V");
  assert(!((*env)->ExceptionOccurred(env)));
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

#ifdef WITH_HEAVY_THREADS
/* scale priority levels from java values to sched.h values */
static int java_priority_to_sched_priority(jint pr) {
  if (pr >= NORM_PRIORITY)
    return sched_norm_priority + 
      ( (sched_max_priority - sched_norm_priority) *
	(pr - NORM_PRIORITY) / (MAX_PRIORITY - NORM_PRIORITY) );
  else
    return sched_min_priority +
      ( (sched_norm_priority - sched_min_priority) *
	(pr - MIN_PRIORITY) / (NORM_PRIORITY - MIN_PRIORITY) );
}
#endif

/*
 * Class:     java_lang_Thread
 * Method:    currentThread
 * Signature: ()Ljava/lang/Thread;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Thread_currentThread
  (JNIEnv *env, jclass cls) {
  assert(((struct FNI_Thread_State *)env)->thread != NULL);
  return ((struct FNI_Thread_State *)env)->thread;
}

/*
 * Class:     java_lang_Thread
 * Method:    yield
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_yield
  (JNIEnv *env, jclass cls) {
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  pthread_yield_np();
#endif
}

/*
 * Class:     java_lang_Thread
 * Method:    sleep
 * Signature: (J)V
 */
/* causes the *currently-executing* thread to sleep */
JNIEXPORT void JNICALL Java_java_lang_Thread_sleep
  (JNIEnv *env, jclass cls, jlong millis) {
#if WITH_PTH_THREADS
  // FIXME: not sure this is the best way to handle thread interruptions
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  struct timeval tp; struct timespec ts;
  int rc;

  rc =  gettimeofday(&tp, NULL); assert(rc==0);
  /* Convert from timeval to timespec */
  ts.tv_sec  = tp.tv_sec;
  ts.tv_nsec = tp.tv_usec * 1000;
  printf("Sleeping from  %d.%09d s\n", ts.tv_sec, ts.tv_nsec);
  ts.tv_sec += millis/1000;
  ts.tv_nsec+= 1000*(millis%1000);
  if (ts.tv_nsec > 1000000000) { ts.tv_nsec-=1000000000; ts.tv_sec++; }
  /* okay, now wait */
  printf("Sleeping until %d.%09d s\n", ts.tv_sec, ts.tv_nsec);
  rc = pthread_cond_timedwait( &(fts->sleep_cond), &(fts->sleep_mutex),
				   &ts);
  printf("woke up\n");
  if (rc != ETIMEDOUT) { /* interrupted? */
    printf("INTERRUPTED!\n");
  }
#elif defined(HAVE_NANOSLEEP)
  struct timespec amt;
  amt.tv_sec = millis/1000;
  amt.tv_nsec = 1000*(millis%1000);
  nanosleep(&amt, &amt);
  // XXX check for interruption and throw InterruptedException
#elif defined(HAVE_USLEEP)
  usleep(1000L*millis);
#elif defined(HAVE_SLEEP)
  sleep((unsigned int)(millis/1000));
#else
# warning "No sleep function defined."
  /* don't sleep at all */
#endif
}


#if 0
/*
 * Class:     java_lang_Thread
 * Method:    countStackFrames
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Thread_countStackFrames
  (JNIEnv *, jobject);
#endif


#if 0
/*
 * Class:     java_lang_Thread
 * Method:    stop0
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_stop0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_Thread
 * Method:    suspend0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_suspend0
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Thread
 * Method:    resume0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_resume0
  (JNIEnv *, jobject);

#endif


#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
struct closure_struct {
  jobject thread;
  pthread_cond_t parampass_cond;
  pthread_mutex_t parampass_mutex;
};
static void * thread_startup_routine(void *closure) {
  int top_of_stack; /* special variable holding top-of-stack position */
  struct closure_struct *cls = (struct closure_struct *)closure;
  JNIEnv *env = FNI_CreateJNIEnv();
  jobject thread, threadgroup; jthrowable threadexc;
  /* set up the top of the stack for this thread for exception stack trace */
  ((struct FNI_Thread_State *)(env))->stack_top = &top_of_stack;
  /* This thread is alive! */
  ((struct FNI_Thread_State *)(env))->is_alive = JNI_TRUE;
  /* make sure creating thread is in cond_wait before proceeding. */
  pthread_mutex_lock(&(cls->parampass_mutex));
  /* copy thread wrapper to local stack */
  thread = FNI_NewLocalRef(env, FNI_UNWRAP(cls->thread));
  /* fill in the blanks in env */
  ((struct FNI_Thread_State *)env)->thread = thread;
  ((struct FNI_Thread_State *)env)->pthread = pthread_self();
  FNI_SetJNIData(env, thread, env, NULL);
  /* add this to the running_threads list, unless its a daemon thread */
  if ((*env)->GetBooleanField(env, thread, daemonID) == JNI_FALSE)
    add_running_thread(pthread_self());
  /* okay, parameter passing is done. we can unblock the creating thread now.
   * (note that we're careful to make sure we're on the 'running threads'
   *  list before letting the parent --- who may decide to exit -- continue.)
   */
  pthread_mutex_unlock(&(cls->parampass_mutex));
  pthread_cond_signal(&(cls->parampass_cond));
  /* okay, now start run() method */
  (*env)->CallVoidMethod(env, thread, runID);
  if ( (threadexc = (*env)->ExceptionOccurred(env)) != NULL) {
    // call thread.getThreadGroup().uncaughtException(thread, exception)
    (*env)->ExceptionClear(env); /* clear the thread's exception */
    threadgroup = (*env)->CallObjectMethod(env, thread, gettgID);
    (*env)->CallVoidMethod(env, threadgroup, uncaughtID, thread, threadexc);
  }
  /* this thread is dead now.  give it a chance to clean up. */
  /* (this also removes the thread from the ThreadGroup) */
  /* (see also Thread.EDexit() -- keep these in sync) */
  (*env)->CallNonvirtualVoidMethod(env, thread, thrCls, exitID);
  assert(!((*env)->ExceptionOccurred(env)));
  /* This thread is dead now. */
  ((struct FNI_Thread_State *)(env))->is_alive = JNI_FALSE;
  /** Get rid of the JNIEnv in the JNIData for the thread, since it is going
   *  to be destroyed by the thread clean-up code [see isAlive() ] */
  FNI_SetJNIData(env, thread, NULL, NULL);
  /* Notify others that it's dead (before we deallocate the thread object!). */
  FNI_MonitorEnter(env, thread);
  FNI_MonitorNotify(env, thread, JNI_TRUE);
  FNI_MonitorExit(env, thread);
  assert(!((*env)->ExceptionOccurred(env)));
#ifdef WITH_USER_THREADS
  remove_running_thread(); /* not smart enough to do this on its own. */
#endif
#ifdef WITH_CLUSTERED_HEAPS
  /* give us a chance to deallocate the thread-clustered heap */
  NTHR_free(thread);
#endif
  /* ta-da, done! */
}

#ifndef WITH_USER_THREADS
/*
 * Class:     java_lang_Thread
 * Method:    start
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_start
  (JNIEnv *env, jobject _this) {
  jint pri;
  pthread_t nthread; pthread_attr_t nattr;
  struct sched_param param;
  struct closure_struct cls =
    { _this, PTHREAD_COND_INITIALIZER, PTHREAD_MUTEX_INITIALIZER };
  int status;
  assert(runID!=NULL/* run() is certainly callable! */);
  /* first of all, see if this thread has already been started. */
  if (FNI_GetJNIData(env, _this)!=NULL) {
    // throw IllegalThreadStateException.
    jclass ex = (*env)->FindClass(env,"java/lang/IllegalThreadStateException");
    if ((*env)->ExceptionOccurred(env)) return;
    (*env)->ThrowNew(env, ex, "Thread.start() called more than once.");
    return;
  }
  /* fetch some attribute fields from the Thread */
  pri = (*env)->GetIntField(env, _this, priorityID);
  assert(!((*env)->ExceptionOccurred(env)));
  /* then set up the pthread_attr's */
  pthread_attr_init(&nattr);
#ifndef WITH_PTH_THREADS
  pthread_attr_getschedparam(&nattr, &param);
  param.sched_priority = java_priority_to_sched_priority(pri);
  pthread_attr_setschedparam(&nattr, &param);
#endif
  pthread_attr_setdetachstate(&nattr, PTHREAD_CREATE_DETACHED);
  /* now startup the new pthread */
  pthread_mutex_lock(&(cls.parampass_mutex));
  status = pthread_create(&nthread, &nattr,
			  thread_startup_routine, &cls);
  /* wait for new thread to copy _this before proceeding */
  pthread_cond_wait(&(cls.parampass_cond), &(cls.parampass_mutex));
  /* okay, we're done, man. Release our resources. */
  pthread_cond_destroy(&(cls.parampass_cond));
  pthread_mutex_unlock(&(cls.parampass_mutex));
  pthread_mutex_destroy(&(cls.parampass_mutex));
  pthread_attr_destroy(&nattr);
  /* done! */
}
#endif
#ifdef WITH_USER_THREADS
#include "../user/engine-i386-linux-1.0.h"
/*
 * Class:     java_lang_Thread
 * Method:    start
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_start
  (JNIEnv *env, jobject _this) {
  jint pri;

  struct closure_struct cls =
    { _this , PTHREAD_COND_INITIALIZER, PTHREAD_MUTEX_INITIALIZER };
  int status;
  void * stackptr;
  struct machdep_pthread *mp;
  struct thread_list *tl;
  assert(runID!=NULL/* run() is certainly callable! */);
  /* first of all, see if this thread has already been started. */

  if (FNI_GetJNIData(env, _this)!=NULL) {
    // throw IllegalThreadStateException.
    jclass ex = (*env)->FindClass(env,"java/lang/IllegalThreadStateException");
    if ((*env)->ExceptionOccurred(env)) return;
    (*env)->ThrowNew(env, ex, "Thread.start() called more than once.");
    return;
  }
  /* fetch some attribute fields from the Thread */
  pri = (*env)->GetIntField(env, _this, priorityID);
  assert(!((*env)->ExceptionOccurred(env)));

  
  //build stack and stash it
  INCREMENT_MALLOC(sizeof(struct thread_list));
  tl=malloc(sizeof(struct thread_list));

  stackptr = __machdep_stack_alloc(STACKSIZE);

  __machdep_stack_set(&(tl->mthread), stackptr);


  __machdep_pthread_create(&(tl->mthread), &thread_startup_routine, &cls,STACKSIZE, 0,0);



  /*LOCK ON GTL*/
  tl->next=gtl->next;
  tl->prev=gtl;
  tl->prev->next=tl;
  tl->next->prev=tl;
  

  pthread_mutex_lock(&(cls.parampass_mutex));
  /* wait for new thread to copy _this before proceeding */
  pthread_cond_wait(&(cls.parampass_cond), &(cls.parampass_mutex));
  /* okay, we're done, man. Release our resources. */
  pthread_cond_destroy(&(cls.parampass_cond));
  pthread_mutex_unlock(&(cls.parampass_mutex));
  pthread_mutex_destroy(&(cls.parampass_mutex));
  context_switch();
  /*  while (cls.parampass_cond==0)
      swapthreads();*/
  /* done! */
}
#endif

/*
 * Class:     java_lang_Thread
 * Method:    isInterrupted
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isInterrupted
  (JNIEnv *env, jobject _this, jboolean clearInterrupted) {
  return JNI_FALSE; /* XXX: no thread is ever interrupted. */
}

/*
 * Class:     java_lang_Thread
 * Method:    isAlive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isAlive
  (JNIEnv *env, jobject _this) {
  /* Some comments on this code: first, the is_alive field is perhaps
   * completely unnecessary: EXTRACT_OTHER_ENV(env, somethread) will
   * return non-NULL iff the thread is alive.   But perhaps we'd rather
   * free() the env when the thread object is *garbage collected* (as
   * opposed to when it dies)... this would mean that
   * FNI_DestroyThreadState should be given as an arg to FNI_SetJNIData()
   * instead of as an arg to pthread_key_create().  But would this mean
   * that our thread's LocalRefs stay live until the thread object is
   * collected?  That could be a Bad Thing.  Leaving it as is, for now.
   *  -- CSA [6-jun-00] */
  struct FNI_Thread_State *ts = EXTRACT_OTHER_ENV(env, _this);
  return (ts==NULL) ? JNI_FALSE : ts->is_alive;
}
#endif /* WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS */

#if 0
/*
 * Class:     java_lang_Thread
 * Method:    countStackFrames
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Thread_countStackFrames
  (JNIEnv *, jobject);
#endif

/*
 * Class:     java_lang_Thread
 * Method:    setPriority0
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_setPriority0
  (JNIEnv *env, jobject obj, jint pri) {
#ifdef WITH_HEAVY_THREADS
  struct sched_param param; int policy;
  struct FNI_Thread_State * threadenv = EXTRACT_OTHER_ENV(env, obj);
  if (threadenv == NULL) return; /* thread not yet started. */
  /* get sched_param and mess w/ it */
  pthread_getschedparam(threadenv->pthread, &policy, &param);
  param.sched_priority = java_priority_to_sched_priority(pri);
  pthread_setschedparam(threadenv->pthread, policy, &param);
  /* ta-da! */
#else
  /* do nothing if no thread support. */
#endif
}

#if 0
/*
 * Class:     java_lang_Thread
 * Method:    stop0
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_stop0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_lang_Thread
 * Method:    suspend0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_suspend0
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Thread
 * Method:    resume0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_resume0
  (JNIEnv *, jobject);

#endif
/*
 * Class:     java_lang_Thread
 * Method:    interrupt0
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_interrupt0
  (JNIEnv *env, jobject obj) {
    assert(0);
}
#ifdef WITH_TRANSACTIONS
/* transactional version of this native method */
JNIEXPORT void JNICALL Java_java_lang_Thread_interrupt0_00024_00024withtrans
  (JNIEnv *env, jobject obj, jobject commitrec) {
  assert(0); /* unimplemented */
}
#endif /* WITH_TRANSACTIONS */

/* for compatibility with IBM JDK... */
JNIEXPORT void JNICALL Java_java_lang_Thread_newThreadEvent0
  (JNIEnv *env, jobject _this, jobject thread) {
  fprintf(stderr, "WARNING: IBM JDK may not be fully supported.\n");
}

#if defined(WITH_EVENT_DRIVEN) && !defined(WITH_USER_THREADS)
/* for use by event-driven code. */
JNIEXPORT void JNICALL Java_java_lang_Thread_EDexit
(JNIEnv *env, jobject _this) {
  /* (see also the end of thread_startup_routine() -- keep these in sync. */
  /* call Thread.exit() */
  (*env)->CallNonvirtualVoidMethod(env, _this, thrCls, exitID);
#ifdef WITH_CLUSTERED_HEAPS
  /* give us a chance to deallocate the thread-clustered heap */
  NTHR_free(_this);
#endif
}
#endif /* WITH_EVENT_DRIVEN */
