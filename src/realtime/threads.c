#include <jni.h>
#include <jni-private.h>
#include <assert.h>
#include "flexthread.h"
#include "threads.h"
#include "../user/threads.h"
#include "MemBlock.h"

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

struct thread_queue_struct* thread_queue;
struct thread_queue_struct* end_thread_queue;
struct thread_queue_struct* empty_queue;

int do_switching = 0; //flag for whether or not to do switching
/* scheduler object to set up - global so it doesn't die after it's set up */
jobject scheduler;

long quanta = 200;
  
/* set flag to do thread switching */
void StartSwitching()
{
  do_switching = 1;
}

/* set flag to NOT do thread switching */
int StopSwitching()
{
  int prev = do_switching;
  do_switching = 0;
  return prev;
}

/* restore switching method in place when stop was called */
void RestoreSwitching(int state)
{
  do_switching = state;
}

JNIEXPORT jint JNICALL Java_javax_realtime_PriorityScheduler_stopSwitchingInC
(JNIEnv* env, jobject _this) {
  return StopSwitching();
}

JNIEXPORT void JNICALL Java_javax_realtime_PriorityScheduler_restoreSwitchingInC
(JNIEnv* env, jobject _this, jint state) {
  RestoreSwitching(state);
}

JNIEXPORT void JNICALL Java_javax_realtime_PriorityScheduler_setQuantaInC
(JNIEnv* env, jobject _this, jlong microsecs) {
  quanta = microsecs;
}

JNIEXPORT jlong JNICALL Java_javax_realtime_RealtimeClock_getTimeInC
(JNIEnv* env, jobject _this) {
  struct timeval time;
  gettimeofday(&time, NULL);
  return time.tv_sec * 1000000 + time.tv_usec;
}

JNIEXPORT jlong JNICALL Java_javax_realtime_PriorityScheduler_getTimeInC
(JNIEnv* env, jobject _this) {
  struct timeval time;
  gettimeofday(&time, NULL);
  return time.tv_sec * 1000000 + time.tv_usec;
}

/* check the time and possibly check for a needed thread switch */
void CheckQuanta(int notimecheck, int force, int actually_transfer)
{
  struct timeval time; //the current time
  JNIEnv* env;         //a JNI environment
  jobject ref_marker;
  jclass schedClass, rttClass; //class of the scheduler & of the RealtimeThread
  jlong threadID; //threadID returned by the scheduler
  jobject scheduler; //scheduler object
  struct thread_queue_struct* threadq;
  /* methods to choose a new thread, and to get the scheduler */
  jmethodID chooseThreadMethod, getSchedMethod, emptyMethod; 
  struct inflated_oobj* infObj; //thread's inflated object
  int switching_state; //save old switching state
  static struct timeval lastCheckTime = {0, 0};

  if(!do_switching && !force) //if thread switching is turned off
    return;

  gettimeofday(&time, NULL);

  // If there was a previous time check
  if (lastCheckTime.tv_sec > 0 || lastCheckTime.tv_usec > 0)
    quanta -= (time.tv_usec - lastCheckTime.tv_usec) +
      (time.tv_sec - lastCheckTime.tv_sec) * 1000000;
  lastCheckTime = time;

  if(quanta <= 0 || notimecheck) {
    switching_state = StopSwitching();

    env = (JNIEnv*)FNI_GetJNIEnv(); //get JNI environment
    ref_marker = ((struct FNI_Thread_State*)env)->localrefs_next;

    /* Get the current scheduler */
    rttClass = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
    assert(!((*env)->ExceptionOccurred(env)));
      
    getSchedMethod = (*env)->GetMethodID(env, rttClass,
					 "getScheduler",
					 "()Ljavax/realtime/Scheduler;");
    assert(!((*env)->ExceptionOccurred(env)));      
    scheduler = (*env)->CallObjectMethod(env, ((struct FNI_Thread_State*)env)->thread, 
					 getSchedMethod);
    assert(!((*env)->ExceptionOccurred(env)));
    if(scheduler == NULL) { //if there is no scheduler
      RestoreSwitching(switching_state); //restore switching
      FNI_DeleteLocalRefsUpTo(env, ref_marker);
      puts("No scheduler: returning");
      return;
    }

    /* Choose the next thread */
    schedClass = (*env)->GetObjectClass(env, scheduler);
    assert(!((*env)->ExceptionOccurred(env)));
      
    chooseThreadMethod = (*env)->GetMethodID(env, schedClass,
					     "chooseThread", "(J)J");
    assert(!((*env)->ExceptionOccurred(env)));
      
    threadq = NULL;
    if(!force) {
      threadID = (*env)->CallLongMethod(env, scheduler, chooseThreadMethod,
					time.tv_sec*1000000 + time.tv_usec);
      //      printf("--- chooseThread sez %d\n", threadID);
      if((*env)->ExceptionOccurred(env))
	(*env)->ExceptionDescribe(env);
      assert(!((*env)->ExceptionOccurred(env)));
	
      if(threadID == 0) { //if no thread was chosen
	RestoreSwitching(switching_state); //restore switching
	FNI_DeleteLocalRefsUpTo(env, ref_marker);
	return;
      }
	
      threadq = lookupThread(threadID);
	
      if(threadq == NULL) { //invalid ID
	RestoreSwitching(switching_state); //restore switching
	FNI_DeleteLocalRefsUpTo(env, ref_marker);
	return;
      }
    }
    else {
      emptyMethod = (*env)->GetMethodID(env, schedClass, "noThreads", "()Z");
      assert(!((*env)->ExceptionOccurred(env)));

      //if((*env)->CallBooleanMethod(env, scheduler, emptyMethod)
      // == JNI_TRUE) {
      if(thread_queue == NULL) {
	currentThread = NULL;
	/* if there are no threads to switch to */
	RestoreSwitching(switching_state); //restore switching
	FNI_DeleteLocalRefsUpTo(env, ref_marker);
	return;
      }

      if((*env)->CallBooleanMethod(env, scheduler, emptyMethod)
	 == JNI_TRUE) {
	/* if there are no threads to switch to */
	RestoreSwitching(switching_state); //restore switching
	currentThread = NULL; //set the current thread to NULL
	FNI_DeleteLocalRefsUpTo(env, ref_marker);
	return;
      }
	
      while(threadq == NULL) {
	gettimeofday(&time, NULL);
	threadID = (*env)->CallLongMethod(env, scheduler,
					  chooseThreadMethod,
					  time.tv_sec*1000000 + time.tv_usec);
	//	printf("--- chooseThread sez %d\n", threadID);
	assert(!((*env)->ExceptionOccurred(env)));
	  
	if(threadID == 0) //if no thread was chosen
	  continue;

	threadq = lookupThread(threadID);
      }
    }
#ifdef WITH_NOHEAP_SUPPORT
    if((((ptroff_t)FNI_UNWRAP(threadq->jthread)) & HEAP_BIT_CHECK) ||
       (!(*env)->IsInstanceOf(env, threadq->jthread, 
			      (*env)->FindClass(env,
						"javax/realtime/NoHeapRealtimeThread")))) {
      //run garbage collector
      /* may need to stop for GC */
#ifdef WITH_PRECISE_GC
      while (pthread_mutex_trylock(&gc_thread_mutex)) {
	if (halt_for_GC_flag) {
	  halt_for_GC();
	}
      }
#endif
    }
      
      
#elif defined(WITH_PRECISE_GC)
    while (pthread_mutex_trylock(&gc_thread_mutex))
      if (halt_for_GC_flag) halt_for_GC();
#endif
      
#ifdef WITH_PRECISE_GC
    pthread_mutex_unlock(&gc_thread_mutex);
#endif
    //threadq->mthread =
    //&((struct inflated_oobj*)getInflatedObject(env, threadq->jthread))->mthread;
    /* get the chosen thread's inflated object */
    RestoreSwitching(switching_state); //restore switching
    FNI_DeleteLocalRefsUpTo(env, ref_marker);
    //      printf("Switching (%s, %d)...\n", actually_transfer?"yes":"no", threadID);
    // if(threadq->mthread->start_argument == NULL)
    //   printf("!!!!mthread (%d) is NULL in CheckQuanta!!!!\n", threadID);

    gettimeofday(&lastCheckTime, NULL);
    //    printf("%06d\n", lastCheckTime.tv_usec);
    if(actually_transfer)
      transfer(threadq); //transfer to the new thread
    else
      currentThread = threadq;

  }
}

void print_queue(struct thread_queue_struct* q, char* message) {
  //  struct thread_queue_struct* t = q;
  //  printf("%s: (", message);
  //  while (t) {
  //    printf("%d,", t->threadID);
  //    t = t->next;
  //  }
  //  printf(")\n");
}

void enqueue(struct thread_queue_struct** h, struct thread_queue_struct** t,
	     struct thread_queue_struct* elem) {
  if (*h == NULL) {
    *h = elem;
    (*h)->prev = NULL;
  }
  else {
    (*t)->next = elem;
    elem->prev = *t;
  }
  *t = elem;
  (*t)->next = NULL;
}

JNIEXPORT void JNICALL Java_javax_realtime_PriorityScheduler_addThreadInC
(JNIEnv* env, jobject _this, jobject thread, jlong threadID) {
  struct thread_queue_struct* open_spot = NULL;
  struct inflated_oobj* infObj =
    (struct inflated_oobj*)getInflatedObject(env, thread);

  print_queue(thread_queue, "BEG addThreadInC queue");

  if(empty_queue == NULL) {
    empty_queue = (struct thread_queue_struct*)
      RTJ_MALLOC_UNCOLLECTABLE(sizeof(struct thread_queue_struct));
    empty_queue->threadID = 0;
    empty_queue->jthread = NULL;
    empty_queue->mthread = NULL;
    empty_queue->prev = NULL;
    empty_queue->next = NULL;
  }

  open_spot = empty_queue;
  empty_queue = empty_queue->next;

  open_spot->threadID = threadID;
  open_spot->jthread = FNI_NewGlobalRef(env, thread);
  open_spot->queue_state = IN_ACTIVE_QUEUE;
  open_spot->prev = NULL;
  open_spot->next = NULL;
  open_spot->mthread = &(infObj->mthread);

  if(open_spot->mthread->start_argument == NULL)
    printf("!!!!mthread (%d) is null in ADD!!!!\n", threadID);

  if(thread_queue == NULL) {
    thread_queue = open_spot;
    end_thread_queue = open_spot;
  }
  else {
    end_thread_queue->next = open_spot;
    open_spot->prev = end_thread_queue;
    end_thread_queue = open_spot;
  }

  print_queue(thread_queue, "END addThreadInC queue");
}
  
JNIEXPORT jlong JNICALL Java_javax_realtime_PriorityScheduler_removeThreadInC
(JNIEnv* env, jobject _this, jobject thread) {
  struct thread_queue_struct* lthread_queue = thread_queue;
  struct thread_queue_struct* remove_spot = NULL;
  jlong threadID;

  print_queue(thread_queue, "BEG removeThreadInC queue");

  if(thread_queue == NULL)
    return 0;

  while(lthread_queue != NULL) {
    if(FNI_UNWRAP_MASKED(lthread_queue->jthread) == 
       FNI_UNWRAP_MASKED(thread)) {
      FNI_DeleteGlobalRef(env, lthread_queue->jthread);
      remove_spot = lthread_queue;
      if (remove_spot == thread_queue)
	thread_queue = thread_queue->next;
      if(remove_spot->prev != NULL)
	remove_spot->prev->next = remove_spot->next;
      if(remove_spot->next != NULL)
	remove_spot->next->prev = remove_spot->prev;
      remove_spot->jthread = NULL;
      remove_spot->mthread = NULL;

      remove_spot->prev = NULL;
      remove_spot->next = empty_queue;
      if (empty_queue)
	empty_queue->prev = remove_spot;
      empty_queue = remove_spot;

      print_queue(thread_queue, "END removeThreadInC queue");
      return remove_spot->threadID;
    }
	
    lthread_queue = lthread_queue->next;
  }
  return 0;
}

void cleanupThreadQueue(JNIEnv* env)
{
  struct thread_queue_struct* nextPiece;

  while(thread_queue != NULL) {
    nextPiece = thread_queue->next;
    if(thread_queue->jthread != NULL)
      FNI_DeleteGlobalRef(env, thread_queue->jthread);
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
  jobject ref_marker = ((struct FNI_Thread_State *)env)->localrefs_next;
  jobject thread = ((struct FNI_Thread_State*)env)->thread;
  jclass rttClass;
  jobject scheduler;
  jmethodID getSchedMethod, disableMethod;

  int switching_state; //prev switching state

  print_queue(thread_queue, "BEG disableThread queue");

  if(queue == NULL || queue->threadID == 0) {
    FNI_DeleteLocalRefsUpTo(env, ref_marker);
    return;
  }

  //printf("Disable Thread %d\n", queue->threadID);

  queue->queue_state = IN_MUTEX_QUEUE;
  switching_state = StopSwitching();

  getSchedMethod = 
    (*env)->GetMethodID(env, (*env)->GetObjectClass(env, thread),
			"getScheduler",
			"()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));
  
  scheduler = (*env)->CallObjectMethod(env, thread, getSchedMethod);
  assert(!((*env)->ExceptionOccurred(env)));
  
  if(scheduler == NULL) {
    FNI_DeleteLocalRefsUpTo(env, ref_marker);
    RestoreSwitching(switching_state);
    return;
  }
  
  disableMethod = (*env)->GetMethodID(env, 
				      (*env)->GetObjectClass(env, scheduler),
				      "disableThread", "(J)V");
  assert(!((*env)->ExceptionOccurred(env)));
      
  (*env)->CallVoidMethod(env, scheduler, disableMethod, queue->threadID);
  assert(!((*env)->ExceptionOccurred(env)));

  FNI_DeleteLocalRefsUpTo(env, ref_marker);
  RestoreSwitching(switching_state);

  print_queue(thread_queue, "END disableThread queue");
}

void EnableThread(struct thread_queue_struct* queue)
{
  JNIEnv* env = FNI_GetJNIEnv();
  jobject ref_marker = ((struct FNI_Thread_State *)env)->localrefs_next;

  jclass rttClass;
  jobject scheduler;
  jmethodID getSchedMethod, enableMethod;

  int switching_state; //prev switching state

  print_queue(thread_queue, "BEG enableThread queue");

  if(queue == NULL || queue->threadID == 0) {
    FNI_DeleteLocalRefsUpTo(env, ref_marker);
    return;
  }

  //printf("Enable Thread %d\n", queue->threadID);

  queue->queue_state = IN_ACTIVE_QUEUE;
  switching_state = StopSwitching();

  rttClass = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
  assert(!((*env)->ExceptionOccurred(env)));
      
  getSchedMethod = (*env)->GetStaticMethodID(env, rttClass,
					     "getScheduler",
					     "()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));
  
  scheduler = (*env)->CallStaticObjectMethod(env, rttClass, getSchedMethod);
  assert(!((*env)->ExceptionOccurred(env)));
  
  if(scheduler == NULL) {
    FNI_DeleteLocalRefsUpTo(env, ref_marker);
    RestoreSwitching(switching_state);
    return;
  }
  enableMethod = (*env)->GetMethodID(env, 
				     (*env)->GetObjectClass(env, scheduler),
				     "enableThread", "(J)V");
  assert(!((*env)->ExceptionOccurred(env)));
      
  (*env)->CallVoidMethod(env, scheduler, enableMethod, queue->threadID);
  assert(!((*env)->ExceptionOccurred(env)));

  FNI_DeleteLocalRefsUpTo(env, ref_marker);
  RestoreSwitching(switching_state);
  print_queue(thread_queue, "END enableThread queue");
}

void EnableThreadList(struct thread_queue_struct* queue)
{
  JNIEnv* env = FNI_GetJNIEnv();
  jobject ref_marker = ((struct FNI_Thread_State *)env)->localrefs_next;

  jclass rttClass;
  jobject scheduler;
  jmethodID getSchedMethod, enableMethod;

  int switching_state;

  print_queue(thread_queue, "BEG enableThreadList queue");

  if(queue == NULL) {
    FNI_DeleteLocalRefsUpTo(env, ref_marker);
    return;
  }

  RestoreSwitching(switching_state);

  rttClass = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
  assert(!((*env)->ExceptionOccurred(env)));
      
  getSchedMethod = (*env)->GetStaticMethodID(env, rttClass,
					     "getScheduler",
					     "()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));
  
  scheduler = (*env)->CallStaticObjectMethod(env, rttClass, getSchedMethod);
  assert(!((*env)->ExceptionOccurred(env)));
  
  if(scheduler == NULL) {
    FNI_DeleteLocalRefsUpTo(env, ref_marker);
    RestoreSwitching(switching_state);
    return;
  }
  
  enableMethod = (*env)->GetMethodID(env, 
				     (*env)->GetObjectClass(env, scheduler),
				     "enableThread", "(J)V");
  assert(!((*env)->ExceptionOccurred(env)));
      
  while(queue != NULL) {
    queue->queue_state = IN_ACTIVE_QUEUE;
    (*env)->CallVoidMethod(env, scheduler, enableMethod, queue->threadID);
    assert(!((*env)->ExceptionOccurred(env)));

    queue = queue->next;
  }

  FNI_DeleteLocalRefsUpTo(env, ref_marker);
  RestoreSwitching(switching_state);
  print_queue(thread_queue, "END enableThreadList queue");
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
  __machdep_pthread_create(&(infObj->mthread), &startMain,
			   mcls,STACKSIZE, 0,0);
  
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
}

void start_realtime_threads(JNIEnv *env, jobject mainthread, jobject args,
			    jclass thrCls) {
  /*methods to get the scheduler, and to add a thread to it*/
  jobject scheduler; //scheduler object
  jmethodID addThreadMethod, getSchedMethod;
  int nest=0;
  jboolean check;

  // Initialize the scheduler
  setScheduler(env, mainthread);

  /* set up the main Java function as a thread, so we can switch back to it */
  FNI_java_lang_Thread_mainThreadSetup(env, mainthread, args);
  
  //get the scheduler
  getSchedMethod = (*env)->GetStaticMethodID(env, thrCls,
					     "getScheduler",
					     "()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));

  scheduler = (*env)->CallObjectMethod(env, mainthread, getSchedMethod);
  assert(!((*env)->ExceptionOccurred(env)));
  
  //add the main Java thread to it
  addThreadMethod = (*env)->GetMethodID(env, 
					FNI_GetObjectClass(env, scheduler),
					"addToFeasibility",
					"(Ljavax/realtime/Schedulable;)Z");
  assert(!((*env)->ExceptionOccurred(env)));
  //  printf("mthread->start_argument is %p\n",
  //	 mainthread->mthread->start_argument);
  check = (*env)->CallBooleanMethod(env, scheduler, addThreadMethod, mainthread);
  assert(!((*env)->ExceptionOccurred(env)));
  assert(check == JNI_TRUE);
  
  //  printf("About to start switching\n");
  StartSwitching(); //startup thread switching
  
  setjmp(main_return_jump); //set a jump point here so we can return when done
  // by cata: A dirty hack so we don't run CheckQuanta the second time through
  if (++nest == 1)
    CheckQuanta(1, 1, 1); //check the threads to start the program
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
void* startMain(void* mclosure) {
  int top_of_stack; /* special variable holding top-of-stack position */
  
  jclass claz; //the class of the main Java thread
  /* the main method id, id for getting the scheduler,
     and an id for removing threads */
  jboolean check;
  jmethodID mid, getSchedMethod, removeThreadMethod;
  /* an object for this thread, it's thread group, and the scheduler */
  jobject thread, threadgroup, scheduler;
  /* an exception thrown by the thread */
  jthrowable threadexc;
  /* cast the incoming argument to a main_closure_struct */
  struct main_closure_struct* mcls = (struct main_closure_struct*)mclosure;
  
  JNIEnv* env = FNI_CreateJNIEnv(); //create a JNI Environment
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
#if defined(WITH_REALTIME_JAVA) && defined(WITH_NOHEAP_SUPPORT)
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
  StopSwitching(); //stop thread switching

  /* get the scheduler */
  getSchedMethod = 
    (*env)->GetMethodID(env, (*env)->GetObjectClass(env, thread),
			"getScheduler", "()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));
  scheduler = (*env)->CallObjectMethod(env,
				       thread,
				       getSchedMethod);
  assert(!((*env)->ExceptionOccurred(env)));
  /* remove this thread from the scheduler */
  removeThreadMethod =
    (*env)->GetMethodID(env, (*env)->GetObjectClass(env, scheduler),
			"removeFromFeasibility",
			"(Ljavax/realtime/Schedulable;)Z");
  assert(!((*env)->ExceptionOccurred(env)));
  check = (*env)->CallBooleanMethod(env, scheduler, 
				    removeThreadMethod, thread);
  assert(!((*env)->ExceptionOccurred(env)));
  assert(check == JNI_TRUE);

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
#ifdef WITH_CLUSTERED_HEAPS
  /* give us a chance to deallocate the thread-clustered heap */
  NTHR_free(thread);
#endif
  /* main has finished, so it is time to exit, set currentThread to NULL */
  // by cata: wait a second, what if we have other threads still running?
  //  currentThread = NULL;
  RTJ_FREE(mcls); //free the closure structure
  CheckQuanta(1, 1, 0);
  StartSwitching();
}

void realtime_unschedule_thread(JNIEnv *env, jobject thread) {
  /* methods to get the scheduler and remove a thread */
  jmethodID getSchedMethod, removeThreadMethod;
  /* methods to check if there are threads, and to pick one to switch to */
  jmethodID emptyMethod, chooseThreadMethod;
  /* the threadID of the next thread to run */
  jlong threadID;
  /* the scheduler */
  jobject scheduler;
  
  StopSwitching(); //stop thread switching
  
  /* get the scheduler */
  getSchedMethod = (*env)->GetStaticMethodID(env,
					     FNI_GetObjectClass(env, thread),
					     "getScheduler",
					     "()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));
  scheduler = (*env)->CallStaticObjectMethod(env,
					     FNI_GetObjectClass(env, thread),
					     getSchedMethod);
  assert(!((*env)->ExceptionOccurred(env)));
  /* remove the thread from the scheduler */
  removeThreadMethod =
    (*env)->GetMethodID(env, FNI_GetObjectClass(env, scheduler),
			"removeFromFeasibility",
			"(Ljavax/realtime/Schedulable;)V");
  assert(!((*env)->ExceptionOccurred(env)));
  (*env)->CallVoidMethod(env, scheduler, removeThreadMethod, thread);
  assert(!((*env)->ExceptionOccurred(env)));
}

void realtime_destroy_thread(JNIEnv *env, jobject thread, void *cls) {
  struct thread_queue_struct *oldthread;
  oldthread = currentThread;
  FNI_DeleteGlobalRef(env, thread); //remove the global ref to the thread
  RTJ_FREE(cls); //free the closure argument

  CheckQuanta(1, 1, 0);
  FNI_DestroyThreadState(oldthread);
  StartSwitching();
}

void setScheduler(JNIEnv *env, jobject thread) {
	jclass schedClazz = (*env)->FindClass(env, "javax/realtime/Scheduler");
	jmethodID getDefID, setSchedID;
	jobject sched;
  assert(!((*env)->ExceptionOccurred(env)));
  getDefID = (*env)->GetStaticMethodID(env, schedClazz, 
				       "getDefaultScheduler", 
				       "()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));
  sched = (*env)->CallStaticObjectMethod(env, schedClazz, getDefID, NULL);
  assert(!((*env)->ExceptionOccurred(env)));
	setSchedID = (*env)->GetMethodID(env, FNI_GetObjectClass(env, thread),
																	 "setScheduler",
																	 "(Ljavax/realtime/Scheduler;)V");
  assert(!((*env)->ExceptionOccurred(env)));
	(*env)->CallVoidMethod(env, thread, setSchedID, sched);
  assert(!((*env)->ExceptionOccurred(env)));
}
