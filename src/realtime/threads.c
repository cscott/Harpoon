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
  struct jobject* ref_marker;
  jclass schedClass, rttClass; //class of the scheduler & of the RealtimeThread
  jlong threadID; //threadID returned by the scheduler
  jobject scheduler; //thread and scheduler objects
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
      
    getSchedMethod = (*env)->GetStaticMethodID(env, rttClass,
					       "getScheduler",
					       "()Ljavax/realtime/Scheduler;");
    assert(!((*env)->ExceptionOccurred(env)));      
    scheduler = (*env)->CallStaticObjectMethod(env, rttClass, getSchedMethod);
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
       (!(*env)->IsInstanceOf(env, threadq->jthread, (*env)->FindClass(env,
								       "javax/realtime/NoHeapRealtimeThread")))) {
      //run garbage collector
      /* may need to stop for GC */
#ifdef WITH_PRECISE_GC
      while (pthread_mutex_trylock(&gc_thread_mutex)) {
	if (halt_for_GC_flag) {
	  halt_for_GC();
	}
	//	  else {
	//halt_for_GC_flag = 1;
	//CheckQuanta(0, 0, 1);
	//}
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
    if(threadq->mthread->start_argument == NULL)
      printf("!!!!mthread (%d) is NULL in CheckQuanta!!!!\n", threadID);

    gettimeofday(&lastCheckTime, NULL);
    printf("%06d\n", lastCheckTime.tv_usec);
    if(actually_transfer)
      transfer(threadq); //transfer to the new thread
    else
      currentThread = threadq;

  }
}

// set up the scheduler
void setupScheduler()
{
  JNIEnv *jnienv = (JNIEnv*)FNI_GetJNIEnv(); //JNI Env
  jobject ref_marker = ((struct FNI_Thread_State *)jnienv)->localrefs_next;
  jclass schedClass, rttClass; //scheduler & RealtimeThread classes
  /* set current scheduler & scheduler construction methods */
  jmethodID setSchedMethod, schedConstruct;

  /* Get Scheduler instance & setup */
  schedClass = (*jnienv)->FindClass(jnienv,
				    "javax/realtime/PriorityScheduler");
  assert(!((*jnienv)->ExceptionOccurred(jnienv)));

  schedConstruct =
    (*jnienv)->GetStaticMethodID(jnienv, schedClass,
				 "getScheduler",
				 "()Ljavax/realtime/PriorityScheduler;");
  assert(!((*jnienv)->ExceptionOccurred(jnienv)));
  scheduler = (*jnienv)->CallStaticObjectMethod(jnienv, schedClass,
						schedConstruct);
  assert(!((*jnienv)->ExceptionOccurred(jnienv)));
  rttClass = (*jnienv)->FindClass(jnienv, "javax/realtime/RealtimeThread");
  assert(!((*jnienv)->ExceptionOccurred(jnienv)));
  setSchedMethod = (*jnienv)->GetStaticMethodID(jnienv, rttClass,
						"setScheduler",
						"(Ljavax/realtime/Scheduler;)V");
  assert(!((*jnienv)->ExceptionOccurred(jnienv)));
  (*jnienv)->CallStaticVoidMethod(jnienv, rttClass, setSchedMethod, scheduler);
  
  FNI_DeleteLocalRefsUpTo(jnienv, ref_marker);
}

struct thread_queue_struct* thread_queue;
struct thread_queue_struct* end_thread_queue;
struct thread_queue_struct* empty_queue;

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
  // by cata: This dies horribly, and I'm too lazy to debug it...
/*   while(empty_queue != NULL) { */
/*     nextPiece = empty_queue->next; */
/*     RTJ_FREE(empty_queue); */
/*     empty_queue = nextPiece; */
/*   } */
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
  jobject* ref_marker = ((struct FNI_Thread_State *)env)->localrefs_next;

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
  jobject* ref_marker = ((struct FNI_Thread_State *)env)->localrefs_next;

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
  jobject* ref_marker = ((struct FNI_Thread_State *)env)->localrefs_next;

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
