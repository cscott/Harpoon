#ifndef REALTIME_THREAD_H_INCLUDED
#define REALTIME_THREAD_H_INCLUDED

#include "SchedulerData.h"
#include "RealtimeThreadData.h"
# include <setjmp.h> //for setjmp and longjmp
# include "../java.lang/threadlist.h" /* for add_running_thread */

#define WITH_REALTIME_THREADS_MEASURE_JITTER 1

jmp_buf main_return_jump; //a jump point for when the main thread exits

void StartSwitching(); //turn thread switching on

int StopSwitching();  //turn thread switching off

int IsSwitching();

void RestoreSwitching(int state); //restore prev switching state

/* check for thread switch */
void setupScheduler(); //setup scheduler

void cleanupThreadQueue(); //cleanup the threadQueue

struct thread_queue_struct* lookupThread(jlong threadID);

void DisableThread(struct thread_queue_struct* queue);

void EnableThread(struct thread_queue_struct* queue);

void EnableThreadList(struct thread_queue_struct* queue);

void settimer();

void setQuanta(jlong microsecs);

void context_switch();

void addThreadInC(JNIEnv* env, jobject thread, jlong threadID);

jlong removeThreadInC(JNIEnv* env, jobject thread);

#define IN_ACTIVE_QUEUE 0
#define IN_MUTEX_QUEUE 1

struct thread_queue_struct {
  JNIEnv* jnienv;
  jlong threadID;
  jobject jthread;
  struct machdep_pthread* mthread;
  struct thread_queue_struct* prev;
  struct thread_queue_struct* next;
};

extern struct thread_queue_struct* thread_queue;

extern struct thread_queue_struct* junk_queue;

jclass thrCls; /* clazz for java/lang/Thread. */
jfieldID priorityID; /* "priority" field in Thread object. */
jfieldID daemonID; /* "daemon" field in Thread object. */
jmethodID runID; /* Thread.run() method. */
jmethodID gettgID; /* Thread.getThreadGroup() method. */
jmethodID exitID; /* Thread.exit() method. */
jmethodID uncaughtID; /* ThreadGroup.uncaughtException() method. */
jmethodID cleanupID; /* RealtimeThread.cleanup() method. */


void print_queue(struct thread_queue_struct* q, char* message);
void enqueue(struct thread_queue_struct** h, struct thread_queue_struct** t,
	     struct thread_queue_struct* elem);

void start_realtime_threads(JNIEnv *env, jobject mainthread, jobject args, jclass thrCls);

void realtime_destroy_thread(JNIEnv *env, jobject thread, void *cls);
void realtime_schedule_thread(JNIEnv *env, jobject thread);
void realtime_unschedule_thread(JNIEnv *env, jobject thread);
void realtime_schedule_ID(JNIEnv *env, jlong threadID);
void realtime_unschedule_ID(JNIEnv *env, jlong threadID);

void* startMain(void* mclosure);

void initScheduler(JNIEnv *env, jobject thread);

void setupEnv(JNIEnv *env);
void destroyEnv();

void printScheduler();

#ifdef WITH_NOHEAP_SUPPORT
int BeginNoHeap(JNIEnv *env);
void EndNoHeap(JNIEnv *env, int state);
#endif

void Threads_init(JNIEnv *env);
#endif

#ifndef WITH_REALTIME_THREADS
void transfer();
#else
void transfer(struct thread_queue_struct * mthread);
#endif

