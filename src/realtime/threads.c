#include "config.h"
#include <assert.h>
#include <jni.h>

#ifndef WITH_REALTIME_THREADS
JNIEXPORT jint JNICALL Java_javax_realtime_Scheduler_beginAtomic
  (JNIEnv *env, jobject scheduler) {
  assert(0);
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_endAtomic
  (JNIEnv *env, jobject scheduler, jint state) {
  assert(0);
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_addThreadInC
(JNIEnv* env, jobject _this, jobject thread, jlong threadID) {
  assert(0);
}

JNIEXPORT jlong JNICALL Java_javax_realtime_Scheduler_removeThreadInC
(JNIEnv* env, jobject _this, jobject thread) {
  assert(0);
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_setQuanta
(JNIEnv* env, jobject _this, jlong microsecs) {
  assert(0);
}

#else
#include <jni-private.h>
#include <signal.h>
#include <sys/time.h>
#include <string.h> /* for memset */
#include "flexthread.h"
#include "threads.h"
#include "../user/threads.h"
#include "MemBlock.h"
#include "../java.lang/threadlist.h" /* for add_running_thread */

#ifdef WITH_PRECISE_GC
#include "jni-gc.h"
#include "jni-gcthreads.h"
#endif

#define HEAP_BIT_CHECK 1

/* this structure will hold the information 
 * needed to start the main Java thread
 */
struct main_closure_struct {
  jobject thread;        //the thread object
  jobject args;          //args from the command line
  pthread_cond_t parampass_cond;   //thread info
  pthread_mutex_t parampass_mutex;
};

static pthread_mutex_t runMainThread_mutex;
struct thread_queue_struct* thread_queue;

/* scheduler object to set up - global so it doesn't die after it's set up */
jobject scheduler;

static int mainStarted = 0;

struct itimerval quanta;

static jclass RealtimeThreadClaz;
static jmethodID RealtimeThread_getScheduler;
static jmethodID RealtimeThread_initScheduler;
static jmethodID RealtimeThread_schedule;
static jmethodID RealtimeThread_unschedule;

static jclass SchedulerClaz;
static jmethodID Scheduler_jAddCThread;
static jmethodID Scheduler_jChooseThread;
static jmethodID Scheduler_jDisableThread;
static jmethodID Scheduler_jEnableThread;
static jmethodID Scheduler_jNumThreads;
static jmethodID Scheduler_jRemoveCThread;
static jmethodID Scheduler_print;

void Threads_init(JNIEnv *env) {
  RealtimeThreadClaz = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThreadClaz = (*env)->NewGlobalRef(env, RealtimeThreadClaz);
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThread_getScheduler = (*env)->GetMethodID(env, RealtimeThreadClaz, "getScheduler",
						    "()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThread_initScheduler = (*env)->GetMethodID(env, RealtimeThreadClaz, "initScheduler", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThread_schedule = (*env)->GetMethodID(env, RealtimeThreadClaz, "schedule", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThread_unschedule = (*env)->GetMethodID(env, RealtimeThreadClaz, "unschedule", "()V");
  assert(!((*env)->ExceptionOccurred(env)));

  SchedulerClaz = (*env)->FindClass(env, "javax/realtime/Scheduler");
  assert(!((*env)->ExceptionOccurred(env)));
  SchedulerClaz = (*env)->NewGlobalRef(env, SchedulerClaz);
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jAddCThread = (*env)->GetStaticMethodID(env, SchedulerClaz, "jAddCThread", "(J)V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jChooseThread = (*env)->GetMethodID(env, SchedulerClaz, "jChooseThread", "(J)J");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jDisableThread = (*env)->GetStaticMethodID(env, SchedulerClaz, "jDisableThread", 
						       "(Ljavax/realtime/RealtimeThread;J)V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jEnableThread = (*env)->GetStaticMethodID(env, SchedulerClaz, "jEnableThread", 
						      "(Ljavax/realtime/RealtimeThread;J)V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jNumThreads = (*env)->GetStaticMethodID(env, SchedulerClaz, "jNumThreads", "()J");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jRemoveCThread = (*env)->GetStaticMethodID(env, SchedulerClaz, "jRemoveCThread", "(J)V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_print = (*env)->GetStaticMethodID(env, SchedulerClaz, "print", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  setQuanta((jlong)0);
  settimer();
}

#ifndef WITH_REALTIME_THREADS_PREEMPT
struct timeval compareSwitch;

void CheckTimeSwitch() {
  struct timeval time;
  if ((!quanta.it_value.tv_sec)&&(!quanta.it_value.tv_usec)) return;
  gettimeofday(&time, NULL);

  if ((time.tv_sec>compareSwitch.tv_sec)||
      ((time.tv_sec==compareSwitch.tv_sec)&&
       (time.tv_usec>=compareSwitch.tv_usec))) {
    context_switch();
  }  
}
#endif

void context_switch() {
  sigset_t empty_mask;
  sigset_t old_set;
#ifdef RTJ_DEBUG_THREADS
  printf("\n    context_switch()");
#endif
  sigemptyset(&empty_mask);
  sigprocmask(SIG_BLOCK, &empty_mask, &old_set);
  /* Lock/thread end during atomic section => deadlock */
  assert(!sigismember(&old_set, SIGALRM)); 
  raise(SIGALRM);
}

inline int IsSwitching() {
  sigset_t mask;
  sigset_t empty;
  sigemptyset(&empty);
  sigprocmask(SIG_BLOCK, &empty, &mask);
  return !sigismember(&mask, SIGALRM);
} 

/* set flag to do thread switching */
inline void StartSwitching()
{
  sigset_t alarm_mask;
#ifdef RTJ_DEBUG_THREADS
  if (!IsSwitching()) printf("\n    StartSwitching()");
#endif
  sigemptyset(&alarm_mask);
  sigaddset(&alarm_mask, SIGALRM);
  sigprocmask(SIG_UNBLOCK, &alarm_mask, NULL);
}

/* set flag to NOT do thread switching */
inline int StopSwitching()
{
  sigset_t old_set;
  sigset_t alarm_mask;
#ifdef RTJ_DEBUG_THREADS
  if (IsSwitching()) printf("\n    StopSwitching()");
#endif
  sigemptyset(&alarm_mask);
  sigaddset(&alarm_mask, SIGALRM);
  sigprocmask(SIG_BLOCK, &alarm_mask, &old_set);
  return !sigismember(&old_set, SIGALRM);
}

/* restore switching method in place when stop was called */
inline void RestoreSwitching(int state)
{
#ifdef RTJ_DEBUG_THREADS
  if (IsSwitching()!=state) printf("\n    RestoreSwitching(%d)", state);
#endif
  if (state) {
    StartSwitching();
  } else {
    StopSwitching();
  }
}

#ifdef WITH_NOHEAP_SUPPORT
/* protect regions that should be run with the privileges/responsibilities of a NoHeapRealtimeThread */
inline int BeginNoHeap(JNIEnv* env) {
  int noheap = ((struct FNI_Thread_State*)env)->noheap;
#ifdef RTJ_DEBUG_THREADS
  printf("\n    BeginNoHeap(%p)", env);
#endif
  ((struct FNI_Thread_State*)env)->noheap = 1;
  return noheap;
}

inline void EndNoHeap(JNIEnv* env, int state) {
#ifdef RTJ_DEBUG_THREADS
  printf("\n    EndNoHeap(%p, %d)", env, state);
#endif
  ((struct FNI_Thread_State*)env)->noheap = state;
}
#endif

JNIEXPORT jint JNICALL Java_javax_realtime_Scheduler_beginAtomic
  (JNIEnv *env, jobject scheduler) {
  return StopSwitching();
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_endAtomic
  (JNIEnv *env, jobject scheduler, jint state) {
  RestoreSwitching(state);
}

void setQuanta(jlong microsecs) {
#ifdef RTJ_DEBUG_THREADS
  printf("\n  setQuanta(%lld)", (long long)microsecs);
#endif
  memset(&quanta, 0, sizeof(struct itimerval));
  quanta.it_value.tv_sec  = microsecs/1000000;
  quanta.it_value.tv_usec = microsecs%1000000;
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_setQuanta
(JNIEnv* env, jobject _this, jlong microsecs) {
#ifdef RTJ_DEBUG_THREADS
  printf("\n  Scheduler.setQuanta(%p, %p, %lld)", env, 
	 _this, (long long)microsecs);
#endif
  setQuanta(microsecs);
}

JNIEXPORT jlong JNICALL Java_javax_realtime_RealtimeClock_getTimeInC
(JNIEnv* env, jobject _this) {
  struct timeval time;
  gettimeofday(&time, NULL);
#ifdef RTJ_DEBUG_THREADS
  printf("\n  (%d s,%d us) = RealtimeClock.getTimeInC(%p, %p)", 
	 (int)time.tv_sec, (int)time.tv_usec, env, _this);
#endif
  return time.tv_sec * 1000000 + time.tv_usec;
}

/* check the time and possibly check for a needed thread switch */
void CheckQuanta(int signal)
{
  struct timeval time; //the current time
  JNIEnv* env;         //a JNI environment
  jobject ref_marker;
  jlong threadID; //threadID returned by the scheduler
  jobject scheduler; //scheduler object
  struct thread_queue_struct* threadq;
  sigset_t pending;
#ifdef WITH_NOHEAP_SUPPORT
  int noheap;
#endif
  setQuanta((jlong)0); /* Don't switch until setup for next period */
  settimer(); /* Register handler */
  sigpending(&pending);
  if (sigismember(&pending, SIGALRM)) return; /* Make sure CheckQuanta is top before switching */
  gettimeofday(&time, NULL);
#ifdef RTJ_DEBUG_THREADS  
  printf("\nCheckQuanta()");
#endif
  
  env = (JNIEnv*)FNI_GetJNIEnv(); //get JNI environment
  ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
#ifdef WITH_NOHEAP_SUPPORT
  noheap = BeginNoHeap(env);
#endif

  /* Get the current scheduler */
  scheduler = (*env)->CallObjectMethod(env, ((struct FNI_Thread_State*)env)->thread, 
				       RealtimeThread_getScheduler, NULL);
  assert(!((*env)->ExceptionOccurred(env)));
  if(scheduler == NULL) { //if there is no scheduler
    FNI_DeleteLocalRefsUpTo(env, ref_marker);
#ifdef RTJ_DEBUG_THREADS
    printf("\n  No scheduler: returning");
#endif
#ifdef WITH_NOHEAP_SUPPORT
    EndNoHeap(env, noheap);
#endif
    return;
  }
  
  /* Choose the next thread */
  threadq = NULL;
  threadID = (*env)->CallLongMethod(env, scheduler, Scheduler_jChooseThread,
				    time.tv_sec*1000000 + time.tv_usec);
#ifdef RTJ_DEBUG_THREADS
  printf("\n  %d = ChooseThread(%p, %p, (%d s, %d us))", 
	 (int)threadID, env, FNI_UNWRAP_MASKED(scheduler), 
	 (int)time.tv_sec, (int)time.tv_usec);
#endif
  if((*env)->ExceptionOccurred(env))
    (*env)->ExceptionDescribe(env);
  assert(!((*env)->ExceptionOccurred(env)));
  
  if(threadID == 0) { //if no thread was chosen
    FNI_DeleteLocalRefsUpTo(env, ref_marker);
#ifdef WITH_NOHEAP_SUPPORT
    EndNoHeap(env, noheap);
#endif
    return;
  }
  
  threadq = lookupThread(threadID);
  assert(threadq != NULL); // invalid ID
  
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
#ifdef WITH_NOHEAP_SUPPORT
  EndNoHeap(env, noheap);
#endif
  transfer(threadq); //transfer to the new thread
}

#ifdef WITH_REALTIME_THREADS_PREEMPT
void settimer() {
  struct sigaction timer;
#ifdef RTJ_DEBUG_THREADS
  printf("\n  settimer()");
#endif
  timer.sa_handler=&CheckQuanta;
  sigemptyset(&timer.sa_mask);
  timer.sa_flags=SA_ONESHOT;
  sigaction(SIGALRM, &timer, NULL);
  siginterrupt(SIGALRM, 0); /* Allow system calls to be restarted after CheckQuanta */
  setitimer(ITIMER_REAL, &quanta, NULL);
}
#else
void settimer() {
  struct timeval time;
  long long int microsecs;
  gettimeofday(&time, NULL);
  microsecs = time.tv_sec * 1000000 + time.tv_usec;
  microsecs += quanta.it_value.tv_sec * 1000000 + quanta.it_value.tv_usec;
  compareSwitch.it_value.tv_sec  = microsecs/1000000;
  compareSwitch.it_value.tv_usec = microsecs%1000000;
}
#endif

void print_queue(struct thread_queue_struct* q, char* message) {
#ifdef RTJ_DEBUG_THREADS
  struct thread_queue_struct* t = q;
  printf("\n  %s: (", message);
  while (t) {
    printf("%lld,", (long long int)t->threadID);
    t = t->next;
  }
  printf(")");
#endif
  printScheduler();
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_addThreadInC
(JNIEnv* env, jobject _this, jobject thread, jlong threadID) {
  struct thread_queue_struct* open_spot = 
    RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct thread_queue_struct), 1);
  struct inflated_oobj* infObj =
    (struct inflated_oobj*)getInflatedObject(env, thread);

#ifdef RTJ_DEBUG_THREADS
  printf("\naddThreadInC(%lld)", (long long int)threadID);
#endif

  print_queue(thread_queue, "BEG addThreadInC queue");

  open_spot->threadID = threadID;
  open_spot->jthread = FNI_NewGlobalRef(env, thread);
  open_spot->mthread = &(infObj->mthread);

#ifdef RTJ_DEBUG_THREADS
  if(open_spot->mthread->start_argument == NULL) {
    printf("\n!!!!mthread (%lld) is null in ADD!!!!", (long long int)threadID);
  }
#endif

  open_spot->next = thread_queue;
  thread_queue = open_spot;

  print_queue(thread_queue, "END addThreadInC queue");
}

JNIEXPORT jlong JNICALL Java_javax_realtime_Scheduler_removeThreadInC
(JNIEnv* env, jobject _this, jobject thread) {
  struct thread_queue_struct* prev_queue = thread_queue;
  struct thread_queue_struct* lthread_queue = thread_queue;
  print_queue(thread_queue, "BEG removeThreadInC queue");

  while (lthread_queue != NULL) { 
    if(FNI_UNWRAP_MASKED(lthread_queue->jthread) == 
       FNI_UNWRAP_MASKED(thread)) {
      struct thread_queue_struct* remove_spot;
      jlong l = lthread_queue->threadID;
      FNI_DeleteGlobalRef(env, (remove_spot = lthread_queue)->jthread);
      if (prev_queue == lthread_queue) {
	  thread_queue = thread_queue->next;
      } else {
	  prev_queue->next = lthread_queue->next;
      }
//      RTJ_FREE(remove_spot);
      print_queue(thread_queue, "END removeThreadInC queue");
      return l;
    }
    lthread_queue = (prev_queue = lthread_queue)->next;
  }
  print_queue(thread_queue, "END removeThreadInC queue");
  return 0;
}

void cleanupThreadQueue(JNIEnv* env)
{
  struct thread_queue_struct* nextPiece;

#ifdef RTJ_DEBUG_THREADS
  printf("\ncleanupThreadQueue(%p)", env);
#endif
  while(thread_queue != NULL) {
    nextPiece = thread_queue->next;
    if(thread_queue->jthread != NULL)
      (*env)->DeleteGlobalRef(env, thread_queue->jthread);
    RTJ_FREE(thread_queue);
    thread_queue = nextPiece;
  }
}

struct thread_queue_struct* lookupThread(jlong threadID)
{
  struct thread_queue_struct* lthread_queue = thread_queue;
    
  print_queue(thread_queue, "BEG lookup queue");

  while(lthread_queue != NULL) {
    if(lthread_queue->threadID == threadID)
      return lthread_queue;

    lthread_queue = lthread_queue->next;
  }

  return NULL;
}

void DisableThread(struct thread_queue_struct* queue)
{
  JNIEnv* env = FNI_GetJNIEnv();
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
  int switching_state = StopSwitching();
#ifdef WITH_NOHEAP_SUPPORT
  int noheap = BeginNoHeap(env);
#endif
#ifdef RTJ_DEBUG_THREADS
  printf("\nDisableThread %lld", (long long int)queue->threadID);
#endif
  print_queue(thread_queue, "BEG disableThread queue");
  (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_jDisableThread, 
			       queue->jthread, queue->threadID);
  assert(!((*env)->ExceptionOccurred(env)));
  print_queue(thread_queue, "END disableThread queue");
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
#ifdef WITH_NOHEAP_SUPPORT
  EndNoHeap(env, noheap);
#endif
  RestoreSwitching(switching_state);
  context_switch(); /* Switch away, since this thread is now blocked. */
}

void EnableThread(struct thread_queue_struct* queue)
{
  JNIEnv* env = FNI_GetJNIEnv();
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
  int switching_state = StopSwitching();
#ifdef WITH_NOHEAP_SUPPORT
  int noheap = BeginNoHeap(env);
#endif
#ifdef RTJ_DEBUG_THREADS
  printf("\nEnableThread %lld", (long long int)queue->threadID);
#endif
  print_queue(thread_queue, "BEG enableThread queue");
  if(queue == NULL || queue->threadID == 0) return;
  (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_jEnableThread, 
			       queue->jthread, queue->threadID);
  assert(!((*env)->ExceptionOccurred(env)));
  print_queue(thread_queue, "END enableThread queue");
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
#ifdef WITH_NOHEAP_SUPPORT
  EndNoHeap(env, noheap);
#endif
  RestoreSwitching(switching_state);
}

void EnableThreadList(struct thread_queue_struct* queue)
{
  print_queue(thread_queue, "BEG enableThreadList queue");

  while(queue != NULL) {
    EnableThread(queue);
    queue = queue->next;
  }
  
  print_queue(thread_queue, "END enableThreadList queue");
}

jlong realtime_num_threads(JNIEnv* env) {
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
  jlong nThreads;
  int switching_state = StopSwitching();
#ifdef WITH_NOHEAP_SUPPORT
  int noheap = BeginNoHeap(env);
#endif
#ifdef RTJ_DEBUG_THREADS
  printf("\nrealtime_num_threads(%p)", env);
#endif
  nThreads = (*env)->CallStaticLongMethod(env, SchedulerClaz, Scheduler_jNumThreads, NULL);
  assert(!((*env)->ExceptionOccurred(env)));
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
#ifdef WITH_NOHEAP_SUPPORT
  EndNoHeap(env, noheap);
#endif
  RestoreSwitching(switching_state);
  return nThreads;
}

/* setup the 'main' function as a thread */
void FNI_java_lang_Thread_mainThreadSetup
(JNIEnv *env, jobject mainThr, jobject args) {
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
  jint pri; //the thread's priority
  void * stackptr; //pointer to the stack

  struct inflated_oobj* infObj; //inflated object to hang on mainThr
  
  /* create the structure that will be passed to startMain later */
  /* use RTJ_MALLOC_UNCOLLECTABLE so it will stay in memory when */
  /* this function exits */
  struct main_closure_struct oldmcls =
  {mainThr,args, PTHREAD_COND_INITIALIZER, PTHREAD_MUTEX_INITIALIZER};
  struct main_closure_struct* mcls = (struct main_closure_struct *)
    RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct main_closure_struct));
  struct thread_queue_struct* tc;
  memcpy(mcls, &oldmcls, sizeof(struct main_closure_struct));
  
  /* get the inflated object for mainThr */
  infObj = (struct inflated_oobj*)getInflatedObject(env, mainThr);

  /* get the threads priority */
  pri = (*env)->GetIntField(env, mainThr, priorityID);
  assert(!((*env)->ExceptionOccurred(env)));

  stackptr = __machdep_stack_alloc(STACKSIZE); //create the stack

  __machdep_stack_set(&(infObj->mthread), stackptr); //set the thread's stack

#ifdef WITH_PRECISE_GC
  /* may need to stop for GC */
  while (pthread_mutex_trylock(&gc_thread_mutex))
    if (halt_for_GC_flag) halt_for_GC();
#endif

  /* create the thread with startMain as its startup routine and */
  /* mcls as its start argument */
  currentThread->threadID = -1; /** -1 is the pre-main thread */
  currentThread->next = thread_queue;
  (thread_queue = currentThread)->prev = NULL;
  currentThread->jthread = NULL;

  realtime_schedule_ID(env, (jlong)-1);
  
  __machdep_pthread_create(&(infObj->mthread), &startMain,
			   mcls,STACKSIZE, 0,0);

  /* schedule the thread for execution */
  realtime_schedule_thread(env, mainThr);
  tc = currentThread;

  pthread_mutex_init(&runMainThread_mutex, NULL);
  setQuanta(0);
  settimer();
  StartSwitching(); //startup thread switching
  /* wait for thread to copy mainThr */
  pthread_mutex_lock(&(mcls->parampass_mutex));
  pthread_cond_wait(&(mcls->parampass_cond), &(mcls->parampass_mutex));
#ifdef WITH_PRECISE_GC
  pthread_mutex_unlock(&gc_thread_mutex);
#endif
  pthread_cond_destroy(&(mcls->parampass_cond));
  pthread_mutex_unlock(&(mcls->parampass_mutex));
  pthread_mutex_destroy(&(mcls->parampass_mutex));
  /* release locks & return */

  FNI_DeleteLocalRefsUpTo(env, ref_marker);

  currentThread = tc;
}

void start_realtime_threads(JNIEnv *env, jobject mainthread, jobject args,
			    jclass thrCls) {
  /*methods to get the scheduler, and to add a thread to it*/
#ifdef RTJ_DEBUG_THREADS
  printf("\nstart_realtime_threads(%p, %p, %p, %p)", env, 
	 FNI_UNWRAP_MASKED(mainthread), args, thrCls);
#endif

  setQuanta(1000); /* Default value is 1 millisecond */

  // Initialize the scheduler
  initScheduler(env, mainthread);

  /* set up the main Java function as a thread, so we can switch back to it */
  FNI_java_lang_Thread_mainThreadSetup(env, mainthread, args);

  assert(currentThread != NULL);
  
#ifdef RTJ_DEBUG_THREADS
  printf("\n  Running main thread...");
#endif
  pthread_mutex_lock(&runMainThread_mutex);
  pthread_mutex_destroy(&runMainThread_mutex);
#ifdef RTJ_DEBUG_THREADS
  printf("\n  ...and we're back!");
#endif

  /* And we're back - the main Java thread has exited */
  StopSwitching(); //stop Thread switching
  cleanupThreadQueue(env); //get rid of the threadQueue
  if (currentThread == NULL) //make sure main still has access to the env for exit
    setupEnv(env); 
}

void setupEnv(JNIEnv *env) {
  currentThread = RTJ_CALLOC_UNCOLLECTABLE(sizeof(struct thread_queue_struct), 1);
  currentThread->jnienv = env; 
}

void destroyEnv() {
  if (currentThread != NULL) RTJ_FREE(currentThread);
  currentThread = NULL;
}

/* start the main Java thread */
// Need to merge this with the stuff located in startup.c
void* startMain(void* mclosure) {
  int top_of_stack; /* special variable holding top-of-stack position */
  
  jclass claz; //the class of the main Java thread
  /* the main method id, id for getting the scheduler,
     and an id for removing threads */
  jmethodID mid;
  /* an object for this thread, it's thread group, and the scheduler */
  jobject thread, threadgroup;
  /* an exception thrown by the thread */
  jthrowable threadexc;
  /* cast the incoming argument to a main_closure_struct */
  struct main_closure_struct* mcls = (struct main_closure_struct*)mclosure;
  
  JNIEnv* env = FNI_CreateJNIEnv(); //create a JNI Environment
  int switching_state;
  assert(mainStarted==0); //let me know if this fails - WSB
  mainStarted = 1;
  ((struct FNI_Thread_State *)env)->stack_top = &top_of_stack;
  /* This thread is alive! */
  ((struct FNI_Thread_State *)env)->is_alive = JNI_TRUE;
  
  /* make sure creating thread is in cond_wait before proceeding. */
  pthread_mutex_lock(&(mcls->parampass_mutex));
  /* copy thread wrapper to local stack */
  thread = FNI_NewLocalRef(env, FNI_UNWRAP_MASKED(mcls->thread));

  /* copy thread information */
  ((struct FNI_Thread_State *)env)->thread = thread;
  ((struct FNI_Thread_State *)env)->pthread = pthread_self();

  FNI_SetJNIData(env, thread, env, NULL);
#ifdef WITH_NOHEAP_SUPPORT
  ((struct FNI_Thread_State *)env)->noheap =
    (*env)->IsInstanceOf(env, 
			 ((struct FNI_Thread_State *)env)->thread,
			 (*env)->FindClass(env,
				      "javax/realtime/NoHeapRealtimeThread"));
#endif  
  /* add this to the running_threads list, unless its a daemon thread */
  if((*env)->GetBooleanField(env, thread, daemonID) == JNI_FALSE)
    add_running_thread(env);
  /* okay, parameter passing is done. we can unblock the creating thread now.
   * (note that we're careful to make sure we're on the 'running threads'
   *  list before letting the parent --- who may decide to exit -- continue.)
   */
  pthread_mutex_lock(&runMainThread_mutex);
  pthread_cond_signal(&(mcls->parampass_cond));
  pthread_mutex_unlock(&(mcls->parampass_mutex));
  /* okay, now start run() method */

  /* get the class of the main Java thead, and an id for the main method */
  claz = (*env)->FindClass(env, FNI_javamain);
  assert(!((*env)->ExceptionOccurred(env)));
  mid = (*env)->GetStaticMethodID(env, claz, "main",
				  "([Ljava/lang/String;)V");
  assert(!((*env)->ExceptionOccurred(env)));

  /* call main */
  (*env)->CallStaticVoidMethod(env, claz, mid, mcls->args);
  if ( (threadexc = (*env)->ExceptionOccurred(env)) != NULL) {
    // call thread.getThreadGroup().uncaughtException(thread, exception)
     /* clear the thread's exception */
    (*env)->ExceptionClear(env);
    threadgroup = (*env)->CallObjectMethod(env, thread, gettgID);
    (*env)->CallVoidMethod(env, threadgroup, 
			   uncaughtID, thread, threadexc);
  }
  /* this thread is dead now.  give it a chance to clean up. */
  /* (this also removes the thread from the ThreadGroup) */
  /* (see also Thread.EDexit() -- keep these in sync) */

  

#ifdef RTJ_DEBUG_THREADS
  printf("\nWait for all of the other threads to finish!\n");
#endif
  while (realtime_num_threads(env)>1) {
    settimer();
    context_switch();
  }
  switching_state = StopSwitching();
  /* call it's exit function to clean up */
  // by cata: I commented this out
  //  (*env)->CallNonvirtualVoidMethod(env, thread, thrCls, exitID);
  //  assert(!((*env)->ExceptionOccurred(env)));
  /* This thread is dead now. */
  ((struct FNI_Thread_State *)env)->is_alive = JNI_FALSE;
  /** Get rid of the JNIEnv in the JNIData for the thread, since it is going
   *  to be destroyed by the thread clean-up code [see isAlive() ] */
  FNI_SetJNIData(env, thread, NULL, NULL);
  /* Notify others that it's dead (before we deallocate the thread object!). */
  FNI_MonitorEnter(env, thread);
  FNI_MonitorNotify(env, thread, JNI_TRUE);
  FNI_MonitorExit(env, thread);
  assert(!((*env)->ExceptionOccurred(env)));
  /* remove this thread from the scheduler */
  pthread_mutex_unlock(&runMainThread_mutex);
  realtime_unschedule_thread(env, thread);
  StartSwitching();
  context_switch();
#ifdef WITH_CLUSTERED_HEAPS
  /* give us a chance to deallocate the thread-clustered heap */
  NTHR_free(thread);
#endif
  RTJ_FREE(mcls); //free the closure structure
  destroyEnv();
  /* pthreads return a value on join, 
   * but Java doesn't do this - so we just return NULL */
  return NULL;  
}

void realtime_schedule_thread(JNIEnv *env, jobject thread) {
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
  int switching_state = StopSwitching();
#ifdef RTJ_DEBUG_THREADS
  printf("\n  realtime_schedule_thread(%p, %p)", env, thread);
#endif
  printScheduler();
  (*env)->CallVoidMethod(env, thread, RealtimeThread_schedule, NULL);
  assert(!((*env)->ExceptionOccurred(env)));
  printScheduler();
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
  RestoreSwitching(switching_state);
}

void realtime_unschedule_thread(JNIEnv *env, jobject thread) {
  /* remove the thread from the scheduler */
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
  int switching_state = StopSwitching();
#ifdef RTJ_DEBUG_THREADS
  printf("\n  realtime_unschedule_thread(%p, %p)", env, thread);
#endif
  printScheduler();
  (*env)->CallVoidMethod(env, thread, RealtimeThread_unschedule, NULL);
  assert(!((*env)->ExceptionOccurred(env)));
  printScheduler();
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
  RestoreSwitching(switching_state);
}

void realtime_schedule_ID(JNIEnv *env, jlong ID) {
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
  int switching_state = StopSwitching();
#ifdef RTJ_DEBUG_THREADS
  printf("\n  realtime_schedule_ID(%p, %lld)", env, (long long int)ID);
#endif
  printScheduler();
  (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_jAddCThread, ID);
  assert(!((*env)->ExceptionOccurred(env)));
  printScheduler();
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
  RestoreSwitching(switching_state);
}

void realtime_unschedule_ID(JNIEnv *env, jlong ID) {
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
  int switching_state = StopSwitching();
#ifdef RTJ_DEBUG_THREADS
  printf("\n  realtime_schedule_ID(%p, %lld)", env, (long long int)ID);
#endif
  printScheduler();
  (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_jRemoveCThread, ID);
  assert(!((*env)->ExceptionOccurred(env)));
  printScheduler();
  FNI_DeleteLocalRefsUpTo(env, ref_marker);  
  RestoreSwitching(switching_state);
}

void realtime_destroy_thread(JNIEnv *env, jobject thread, void *cls) {
  struct thread_queue_struct *oldthread;
  int switching_state = StopSwitching();
#ifdef RTJ_DEBUG_THREADS
  printf("\n  realtime_destroy_thread(%p, %p, %p)", env, thread, cls);
#endif
  oldthread = currentThread;
  FNI_DeleteGlobalRef(env, thread); //remove the global ref to the thread
  RTJ_FREE(cls); //free the closure argument
  RestoreSwitching(switching_state);
  context_switch();
}

void initScheduler(JNIEnv *env, jobject thread) {
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
#ifdef RTJ_DEBUG_THREADS
  printf("\n  initScheduler(%p, %p)", env, thread);
#endif
  (*env)->CallVoidMethod(env, thread, RealtimeThread_initScheduler, NULL);
  assert(!((*env)->ExceptionOccurred(env)));
  printScheduler();
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
}

void printScheduler() {
#ifdef RTJ_DEBUG_THREADS
  JNIEnv* env = FNI_GetJNIEnv();
  int switching_state = StopSwitching();
#ifdef WITH_NOHEAP_SUPPORT
  int noheap = BeginNoHeap(env);
#endif
  jobject ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;
  (*env)->CallStaticVoidMethod(env, SchedulerClaz, Scheduler_print, NULL);
  assert(!((*env)->ExceptionOccurred(env)));
  FNI_DeleteLocalRefsUpTo(env, ref_marker);
#ifdef WITH_NOHEAP_SUPPORT
  EndNoHeap(env, noheap);
#endif
  RestoreSwitching(switching_state);
#endif
}
#endif
