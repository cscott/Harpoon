#include <jni.h>
#include <jni-private.h>
#include "java_lang_Thread.h"

#include <assert.h>
#include "config.h"
#include <errno.h>
#ifdef WITH_HEAVY_THREADS
#include <pthread.h>
#include <sched.h>
#include <sys/time.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include <time.h>

#ifdef WITH_HEAVY_THREADS
#define EXTRACT_OTHER_ENV(env, thread) \
  ( (struct FNI_Thread_State *) (*env)->GetIntField(env, thread, tID) )
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
  struct thread_list *nlist = malloc(sizeof(struct thread_list));
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
}  
static void wait_on_running_thread() {
  pthread_mutex_lock(&running_threads_mutex);
  while (running_threads.next != NULL)
    pthread_cond_wait(&running_threads_cond, &running_threads_mutex);
  pthread_mutex_unlock(&running_threads_mutex);
}

#endif /* WITH_HEAVY_THREADS */

static jfieldID tID; /* Thread field to store corresponding pthread. */
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

void FNI_java_lang_Thread_setupMain(JNIEnv *env) {
  jclass thrCls, thrGrpCls;
  jmethodID thrConsID, thrGrpConsID;
  jfieldID thrNPID;
  jstring mainStr;
  jobject mainThr, mainThrGrp;
  /* make the name of the group and the thread. */
  mainStr = (*env)->NewStringUTF(env, "main");
  assert(!((*env)->ExceptionOccurred(env)));
  /* first make main thread group object. */
  thrGrpCls  = (*env)->FindClass(env, "java/lang/ThreadGroup");
  assert(!((*env)->ExceptionOccurred(env)));
  thrGrpConsID = (*env)->GetMethodID(env, thrGrpCls, "<init>", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  mainThrGrp = (*env)->NewObject(env, thrGrpCls, thrGrpConsID);
  assert(!((*env)->ExceptionOccurred(env)));
  /* now make main thread object. */
  thrCls  = (*env)->FindClass(env, "java/lang/Thread");
  assert(!((*env)->ExceptionOccurred(env)));
  thrConsID = (*env)->GetMethodID(env, thrCls, "<init>", "("
				  "Ljava/lang/ThreadGroup;"
				  "Ljava/lang/Runnable;"
				  "Ljava/lang/String;)V");
  assert(!((*env)->ExceptionOccurred(env)));
  mainThr = (*env)->AllocObject(env, thrCls);
  /* put thread in env structure as 'current thread' */
  assert(((struct FNI_Thread_State *)env)->thread == NULL);
  ((struct FNI_Thread_State *)env)->thread = mainThr;
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
  /* make Thread object-to-pthread mapping. */
  tID = (*env)->GetFieldID(env, thrCls, "PrivateInfo","I");
  assert(!((*env)->ExceptionOccurred(env)));
#ifdef WITH_HEAVY_THREADS
  ((struct FNI_Thread_State *)env)->pthread = pthread_self();
  assert(sizeof(jint)==sizeof(env));// this don't work on 64-bit boxen, gah!
  (*env)->SetIntField(env, mainThr, tID, (jint) env);
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
#ifdef WITH_HEAVY_THREADS
  /* make pthread_key_t before the fat lady sings */
  pthread_key_create(&running_threads_key, remove_running_thread);
#endif /* WITH_HEAVY_THREADS */
  /* done! */
}

/* wait for all non-main non-daemon threads to terminate */
void FNI_java_lang_Thread_finishMain(JNIEnv *env) {
#ifdef WITH_HEAVY_THREADS
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

#if 0
/*
 * Class:     java_lang_Thread
 * Method:    yield
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_Thread_yield
  (JNIEnv *env, jclass cls);
#endif

/*
 * Class:     java_lang_Thread
 * Method:    sleep
 * Signature: (J)V
 */
/* causes the *currently-executing* thread to sleep */
JNIEXPORT void JNICALL Java_java_lang_Thread_sleep
  (JNIEnv *env, jclass cls, jlong millis) {
#if 0 && defined(WITH_HEAVY_THREADS)
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
#else
  struct timespec amt;
  amt.tv_sec = millis/1000;
  amt.tv_nsec = 1000*(millis%1000);
  nanosleep(&amt, &amt);
  // XXX check for interruption and throw InterruptedException
#endif
}

#ifdef WITH_HEAVY_THREADS
struct closure_struct {
  jobject thread;
  pthread_cond_t parampass_cond;
  pthread_mutex_t parampass_mutex;
};
static void * thread_startup_routine(void *closure) {
  struct closure_struct *cls = (struct closure_struct *)closure;
  JNIEnv *env = FNI_CreateJNIEnv();
  jobject thread, threadgroup; jthrowable threadexc;
  /* make sure creating thread is in cond_wait before proceeding. */
  pthread_mutex_lock(&(cls->parampass_mutex));
  /* copy thread wrapper to local stack */
  thread = FNI_NewLocalRef(env, FNI_UNWRAP(cls->thread));
  /* okay, parameter passing is done. we can unblock the creating thread now.*/
  pthread_mutex_unlock(&(cls->parampass_mutex));
  pthread_cond_signal(&(cls->parampass_cond));
  /* fill in the blanks in env */
  ((struct FNI_Thread_State *)env)->thread = thread;
  ((struct FNI_Thread_State *)env)->pthread = pthread_self();
  assert(sizeof(jint)==sizeof(env));// this don't work on 64-bit boxen, gah!
  (*env)->SetIntField(env, thread, tID, (jint) env);
  assert(!((*env)->ExceptionOccurred(env)));
  /* add this to the running_threads list, unless its a daemon thread */
  if ((*env)->GetBooleanField(env, thread, daemonID) == JNI_FALSE)
    add_running_thread(pthread_self());
  /* okay, now start run() method */
  (*env)->CallVoidMethod(env, thread, runID);
  if ( (threadexc = (*env)->ExceptionOccurred(env)) != NULL) {
    // call thread.getThreadGroup().uncaughtException(thread, exception)
    (*env)->ExceptionClear(env); /* clear the thread's exception */
    threadgroup = (*env)->CallObjectMethod(env, thread, gettgID);
    (*env)->CallVoidMethod(env, threadgroup, uncaughtID, thread, threadexc);
  }
  /* this thread is dead now.  give it a chance to clean up. */
#if 0 /* FIXME: BROKEN! */
  (*env)->CallVoidMethod(env, thread, exitID);
#endif
  /* ta-da, done! */
}

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
  /* fetch some attribute fields from the Thread */
  pri = (*env)->GetIntField(env, _this, priorityID);
  assert(!((*env)->ExceptionOccurred(env)));
  /* then set up the pthread_attr's */
  pthread_attr_init(&nattr);
  pthread_attr_getschedparam(&nattr, &param);
  param.sched_priority = java_priority_to_sched_priority(pri);
  pthread_attr_setschedparam(&nattr, &param);
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
#endif /* WITH_HEAVY_THREADS */

#if 0
/*
 * Class:     java_lang_Thread
 * Method:    isInterrupted
 * Signature: (Z)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isInterrupted
  (JNIEnv *, jobject, jboolean);

/*
 * Class:     java_lang_Thread
 * Method:    isAlive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Thread_isAlive
  (JNIEnv *, jobject);

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

/* for compatibility with IBM JDK... */
JNIEXPORT void JNICALL Java_java_lang_Thread_newThreadEvent0
  (JNIEnv *env, jobject _this, jobject thread) {
  fprintf(stderr, "WARNING: IBM JDK may not be fully supported.\n");
}
