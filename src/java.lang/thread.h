#ifndef INCLUDED_FNI_THREAD_H
#define INCLUDED_FNI_THREAD_H

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

#include "threadlist.h" /* maintain list of running threads */

#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
#define EXTRACT_OTHER_ENV(env, thread) \
  ( (struct FNI_Thread_State *) FNI_GetJNIData(env, thread) )
#define EXTRACT_PTHREAD_T(env, thread) \
  ( EXTRACT_OTHER_ENV(env, thread)->pthread )
#endif


extern jclass thrCls; /* clazz for java/lang/Thread. */
extern jfieldID priorityID; /* "priority" field in Thread object. */
extern jfieldID daemonID; /* "daemon" field in Thread object. */
extern jmethodID runID; /* Thread.run() method. */
extern jmethodID gettgID; /* Thread.getThreadGroup() method. */
extern jmethodID exitID; /* Thread.exit() method. */
extern jmethodID uncaughtID; /* ThreadGroup.uncaughtException() method. */
#ifdef WITH_REALTIME_JAVA
extern jmethodID cleanupID; /* RealtimeThread.cleanup() method. */
#endif

/* information about priority values -- both w/in java and runtime system */
extern jint MIN_PRIORITY, NORM_PRIORITY, MAX_PRIORITY;
#ifdef WITH_HEAVY_THREADS
extern int sched_min_priority, sched_norm_priority, sched_max_priority;
#endif

/* these functions defined in src/java.lang/thread.c and only
 * used in src/startup.c. */
void FNI_java_lang_Thread_setupMain(JNIEnv *env);
void FNI_java_lang_Thread_finishMain(JNIEnv *env);

#ifdef WITH_HEAVY_THREADS
/* scale priority levels from java values to sched.h values */
static inline
int java_priority_to_sched_priority(jint pr) {
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

static inline
jobject fni_thread_currentThread(JNIEnv *env) {
  assert(((struct FNI_Thread_State *)env)->thread != NULL);
  return ((struct FNI_Thread_State *)env)->thread;
}

static inline
void fni_thread_yield(JNIEnv *env, jclass cls) {
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS
  pthread_yield_np();
#endif
}

static inline
void fni_thread_sleep(JNIEnv *env, jclass cls, jlong millis, jint nanos) {
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
#if defined(WITH_REALTIME_JAVA) && defined(WITH_NOHEAP_SUPPORT)
  ((struct FNI_Thread_State *)env)->noheap =
    (*env)->IsInstanceOf(env, ((struct FNI_Thread_State *)env)->thread,
			 (*env)->
			 FindClass(env,
				   "javax/realtime/NoHeapRealtimeThread"));
#endif  
  /* add this to the running_threads list, unless its a daemon thread */
  if ((*env)->GetBooleanField(env, thread, daemonID) == JNI_FALSE)
    add_running_thread(env);
  /* okay, parameter passing is done. we can unblock the creating thread now.
   * (note that we're careful to make sure we're on the 'running threads'
   *  list before letting the parent --- who may decide to exit -- continue.)
   */
  pthread_cond_signal(&(cls->parampass_cond));
  pthread_mutex_unlock(&(cls->parampass_mutex));
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
#ifdef WITH_REALTIME_JAVA
//  (*env)->CallVoidMethod(env, thread, cleanupID);
//  assert(!((*env)->ExceptionOccurred(env)));
#endif
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
#ifdef WITH_REALTIME_THREADS
  realtime_unschedule_thread(env, thread);
  realtime_destroy_thread(env, cls->thread, cls);
#endif
#ifdef WITH_CLUSTERED_HEAPS
  /* give us a chance to deallocate the thread-clustered heap */
  NTHR_free(thread);
#endif
  return NULL; /* pthread_create expects some value to be returned */
}

#ifndef WITH_USER_THREADS

static inline
void fni_thread_start(JNIEnv *env, jobject _this) {
  jint pri;
  pthread_t nthread; pthread_attr_t nattr;
  struct sched_param param;
  struct closure_struct cls =
    { _this, PTHREAD_COND_INITIALIZER, PTHREAD_MUTEX_INITIALIZER };
  int status;
#if defined(WITH_REALTIME_JAVA) && defined(WITH_NOHEAP_SUPPORT)
  jclass noHeapThreadClass = 
    (*env)->FindClass(env, "javax/realtime/NoHeapRealtimeThread");
#endif
#ifdef RTJ_DEBUG_THREADS
  printf("\nThread.start(%p, %p)", env, FNI_UNWRAP(_this));
#endif
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
#ifdef WITH_PRECISE_GC
  /* may need to stop for GC */
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!(*env)->IsInstanceOf(env, _this, noHeapThreadClass))
#endif
    while (pthread_mutex_trylock(&gc_thread_mutex))
      if (halt_for_GC_flag) halt_for_GC();
#endif  
  pthread_mutex_lock(&(cls.parampass_mutex));
  status = pthread_create(&nthread, &nattr,
			  thread_startup_routine, &cls);
  /* wait for new thread to copy _this before proceeding */
  pthread_cond_wait(&(cls.parampass_cond), &(cls.parampass_mutex));
  /* okay, we're done, man. Release our resources. */
#ifdef WITH_PRECISE_GC
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!(*env)->IsInstanceOf(env, _this, noHeapThreadClass))
#endif
    pthread_mutex_unlock(&gc_thread_mutex);
#endif
  pthread_cond_destroy(&(cls.parampass_cond));
  pthread_mutex_unlock(&(cls.parampass_mutex));
  pthread_mutex_destroy(&(cls.parampass_mutex));
  pthread_attr_destroy(&nattr);
  /* done! */
}
#endif
#ifdef WITH_USER_THREADS
#include "../../user/engine-i386-linux-1.0.h"

static inline
void fni_thread_start(JNIEnv *env, jobject _this) {
  jint pri;

  struct closure_struct cls =
    { _this , PTHREAD_COND_INITIALIZER, PTHREAD_MUTEX_INITIALIZER };
  struct closure_struct *clsp = &cls;
  void * stackptr;
  struct machdep_pthread *mp;
#if !defined(WITH_REALTIME_THREADS)
  struct thread_list *tl;
#else /* WITH_REALTIME_THREADS */
  struct inflated_oobj *tl;
  int switching_state;

#ifdef RTJ_DEBUG_THREADS
  printf("\nThread.start(%p, %p)", env, FNI_UNWRAP(_this));
#endif
  _this = (*env)->NewGlobalRef(env, _this);
  cls.thread = _this;
  clsp = (struct closure_struct *) 
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(cls));
  memcpy(clsp, &cls, sizeof(cls));
  switching_state = StopSwitching();
#endif
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
  INCREMENT_MEM_STATS(sizeof(struct thread_list));
#if !defined(WITH_REALTIME_THREADS)
  tl=malloc(sizeof(struct thread_list));
#else
  tl=(struct inflated_oobj*)getInflatedObject(env, _this);
#endif

  stackptr = __machdep_stack_alloc(STACKSIZE);

  __machdep_stack_set(&(tl->mthread), stackptr);

#ifdef WITH_PRECISE_GC
  /* may need to stop for GC */
  while (pthread_mutex_trylock(&gc_thread_mutex))
    if (halt_for_GC_flag) halt_for_GC();
#endif

  __machdep_pthread_create(&(tl->mthread), &thread_startup_routine, clsp,
			   STACKSIZE, 0,0);



#if !defined(WITH_REALTIME_THREADS)
  /*LOCK ON GTL*/
  tl->next=gtl->next;
  tl->prev=gtl;
  tl->prev->next=tl;
  tl->next->prev=tl;
#endif
  pthread_mutex_lock(&(clsp->parampass_mutex));
  /* wait for new thread to copy _this before proceeding */
#ifdef WITH_REALTIME_THREADS
  realtime_schedule_thread(env, _this);
  StartSwitching();
#endif
  pthread_cond_wait(&(clsp->parampass_cond), &(clsp->parampass_mutex));
#ifdef WITH_PRECISE_GC
  pthread_mutex_unlock(&gc_thread_mutex);
#endif
  /* okay, we're done, man. Release our resources. */
  pthread_cond_destroy(&(clsp->parampass_cond));
  pthread_mutex_unlock(&(clsp->parampass_mutex));
  pthread_mutex_destroy(&(clsp->parampass_mutex));
  context_switch();

  /*  while (clsp->parampass_cond==0)
      swapthreads();*/
  /* done! */
}
#endif

static inline
jboolean fni_thread_isInterrupted
  (JNIEnv *env, jobject _this, jboolean clearInterrupted) {
  return JNI_FALSE; /* XXX: no thread is ever interrupted. */
}

static inline
jboolean fni_thread_isAlive(JNIEnv *env, jobject _this) {
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

static inline
jint fni_thread_countStackFrames(JNIEnv *env, jobject _this) {
  assert(0); /* unimplemented */
}

/*
 * Class:     java_lang_Thread
 * Method:    setPriority0
 * Signature: (I)V
 */
static inline
void fni_thread_setPriority(JNIEnv *env, jobject obj, jint pri) {
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

static inline
void fni_thread_stop(JNIEnv *env, jobject _this, jobject throwable) {
  assert(0); /* unimplemented */
}

static inline
void fni_thread_suspend(JNIEnv *env, jobject _this) {
  assert(0); /* unimplemented */
}

static inline
void fni_thread_resume(JNIEnv *env, jobject _this) {
  assert(0); /* unimplemented */
}

static inline
void fni_thread_interrupt(JNIEnv *env, jobject _this) {
  fprintf(stderr, "WARNING: Thread.interrupt() not implemented.\n");
}
#ifdef WITH_TRANSACTIONS
/* transactional version of this native method */
static inline
void fni_thread_interrupt_withtrans(JNIEnv *env, jobject _this, jobject commitrec) {
  assert(0); /* unimplemented */
}
#endif /* WITH_TRANSACTIONS */

#endif /* INCLUDED_FNI_THREAD_H */
