#include <jni.h>
#include <jni-private.h>
#include "config.h"

#include "threadlist.h"

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


#if WITH_HEAVY_THREADS || WITH_PTH_THREADS

struct thread_list {
  struct thread_list *prev;
  pthread_t pthread;
#ifdef WITH_PRECISE_GC
  struct FNI_Thread_State *thrstate; // for GC
#endif
  struct thread_list *next;
};

static struct thread_list running_threads = { NULL, 0,
#ifdef WITH_PRECISE_GC
					      NULL, /* thrstate */
#endif /* WITH_PRECISE_GC */
					      NULL }; /*header node*/
static pthread_key_t running_threads_key;
static pthread_cond_t running_threads_cond = PTHREAD_COND_INITIALIZER;

#ifndef WITH_PRECISE_GC
static pthread_mutex_t running_threads_mutex = PTHREAD_MUTEX_INITIALIZER;
#else
/* mutex for garbage collection vs thread addition/deletion */
pthread_mutex_t gc_thread_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t running_threads_mutex = PTHREAD_MUTEX_INITIALIZER;
int num_running_threads = 1;
#endif


void add_running_thread(JNIEnv *env) {
  struct FNI_Thread_State* ts = (struct FNI_Thread_State*)env;
  /* safe to use malloc -- no pointers to garbage collected memory in here */
  struct thread_list *nlist = malloc(sizeof(struct thread_list));
  INCREMENT_MEM_STATS(sizeof(struct thread_list));
  nlist->prev = &running_threads;
#ifdef WITH_PRECISE_GC
  nlist->thrstate = ts;
#endif /* WITH_PRECISE_GC */
  nlist->pthread = ts->pthread;
  pthread_mutex_lock(&running_threads_mutex);
  nlist->next = running_threads.next;
  if (nlist->next) nlist->next->prev = nlist;
  running_threads.next = nlist;
#ifdef WITH_PRECISE_GC
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!(ts->noheap))
#endif
    num_running_threads++;
#endif /* WITH_PRECISE_GC */
  pthread_mutex_unlock(&running_threads_mutex);
  pthread_setspecific(running_threads_key, nlist);
}

void remove_running_thread(void *cl) {
  struct thread_list *nlist = (struct thread_list *) cl;
#ifdef WITH_PRECISE_GC
  // may need to stop for GC
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!nlist->thrstate->noheap)
#endif
    while (pthread_mutex_trylock(&gc_thread_mutex))
      if (halt_for_GC_flag) halt_for_GC();
#endif
  pthread_mutex_lock(&running_threads_mutex);
  if (nlist->prev) nlist->prev->next = nlist->next;
  if (nlist->next) nlist->next->prev = nlist->prev;
#ifdef WITH_PRECISE_GC
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!nlist->thrstate->noheap) {
#endif
    num_running_threads--;
    pthread_mutex_unlock(&gc_thread_mutex);
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  }
#endif
#endif
  pthread_cond_signal(&running_threads_cond);
  pthread_mutex_unlock(&running_threads_mutex);
  free(nlist);
  DECREMENT_MEM_STATS(sizeof(struct thread_list));
}  
void wait_on_running_thread() {
#ifdef WITH_PRECISE_GC
  // may need to stop for GC
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!((struct FNI_Thread_State*)FNI_GetJNIEnv())->noheap)
#endif
    while (pthread_mutex_trylock(&gc_thread_mutex))
      if (halt_for_GC_flag) halt_for_GC();
#endif
  pthread_mutex_lock(&running_threads_mutex);
#ifdef WITH_PRECISE_GC
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!((struct FNI_Thread_State*)FNI_GetJNIEnv())->noheap) {
#endif
    // one less thread to wait for
    num_running_threads--;
    pthread_mutex_unlock(&gc_thread_mutex);
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  }
#endif
#endif
  while (running_threads.next != NULL) {
    pthread_cond_wait(&running_threads_cond, &running_threads_mutex);
  }
  pthread_mutex_unlock(&running_threads_mutex);
}

#endif /* WITH_HEAVY_THREADS || WITH_PTH_THREADS */

#if WITH_USER_THREADS
#define EXTRACT_OTHER_ENV(env, thread) \
  ( (struct FNI_Thread_State *) FNI_GetJNIData(env, thread) )
#define EXTRACT_PTHREAD_T(env, thread) \
  ( EXTRACT_OTHER_ENV(env, thread)->pthread )

#ifdef WITH_PRECISE_GC
struct gc_thread_list {
  struct FNI_Thread_State *thrstate;
  struct gc_thread_list *next;
};

static struct gc_thread_list gc_running_threads = { NULL, NULL };

/* mutex for garbage collection vs thread addition/deletion */
pthread_mutex_t gc_thread_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t running_threads_mutex = PTHREAD_MUTEX_INITIALIZER;
int num_running_threads = 1;
#else
static pthread_mutex_t running_threads_mutex = PTHREAD_MUTEX_INITIALIZER;
#endif
static pthread_cond_t running_threads_cond = PTHREAD_COND_INITIALIZER;

void add_running_thread(JNIEnv *env) {
  struct FNI_Thread_State* thrstate = (struct FNI_Thread_State*)env;
#ifdef WITH_PRECISE_GC
  struct gc_thread_list *gctl;
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (thrstate->noheap) return;
#endif
  gctl = malloc(sizeof(struct gc_thread_list));
  gctl->thrstate = thrstate;
  gctl->next = gc_running_threads.next;
  pthread_mutex_lock(&running_threads_mutex);
  gc_running_threads.next = gctl;
  num_running_threads++;
  pthread_mutex_unlock(&running_threads_mutex);
#endif
}

void remove_running_thread() {
#ifdef WITH_PRECISE_GC
  struct gc_thread_list *gctl = gc_running_threads.next, *prev = NULL;
  // may need to stop for GC
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!((struct FNI_Thread_State*)FNI_GetJNIEnv())->noheap)
#endif
    while (pthread_mutex_trylock(&gc_thread_mutex))
      if (halt_for_GC_flag) halt_for_GC();
#endif
  pthread_mutex_lock(&running_threads_mutex);
#ifdef WITH_PRECISE_GC
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!((struct FNI_Thread_State*)FNI_GetJNIEnv())->noheap) {
#endif
    while (gctl) {
      if (gctl->thrstate == (struct FNI_Thread_State *)FNI_GetJNIEnv()) {
	if (prev) 
	  prev->next = gctl->next;
	else 
	  gc_running_threads.next = gctl->next;
	free(gctl);
	break;
      }
      prev = gctl;
      gctl = gctl->next;
    }
    num_running_threads--;
    pthread_mutex_unlock(&gc_thread_mutex);
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  }
#endif
#endif
  pthread_cond_signal(&running_threads_cond);
  pthread_mutex_unlock(&running_threads_mutex);
}

void wait_on_running_thread() {
#ifdef WITH_PRECISE_GC
  // may need to stop for GC
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!((struct FNI_Thread_State*)FNI_GetJNIEnv())->noheap)
#endif
    while (pthread_mutex_trylock(&gc_thread_mutex))
      if (halt_for_GC_flag) halt_for_GC();
#endif
  pthread_mutex_lock(&running_threads_mutex);
#ifdef WITH_PRECISE_GC
  // one less thread to wait for
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (!((struct FNI_Thread_State*)FNI_GetJNIEnv())->noheap) {
#endif
    num_running_threads--;
    pthread_mutex_unlock(&gc_thread_mutex);
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  }
#endif
#endif
  while((gtl!=gtl->next)||(ioptr!=NULL)) {
    pthread_cond_wait(&running_threads_cond, &running_threads_mutex);
  }
  pthread_mutex_unlock(&running_threads_mutex);
}

#endif /* WITH_USER_THREADS */

#ifdef WITH_PRECISE_GC
#if WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
/* effects: decrements the number of threads that the GC waits for */
void decrement_running_thread_count() {
  // we don't want to block in case GC is occurring
  // since we are still counted as a running thread
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (((struct FNI_Thread_State*)FNI_GetJNIEnv())->noheap) return;
#endif
  while (pthread_mutex_trylock(&gc_thread_mutex))
    if (halt_for_GC_flag) halt_for_GC();
  num_running_threads--;
  pthread_mutex_unlock(&gc_thread_mutex);
}

/* effects: increments the number of threads that the GC waits for */
void increment_running_thread_count() {
  // we want to block if someone else has the lock.
  // even if GC is occurring, it won't be waiting
  // for us since we are off the thread count
  // we definitely don't want to call halt_for_GC().
#if defined(WITH_NOHEAP_SUPPORT) && defined(WITH_REALTIME_JAVA)
  if (((struct FNI_Thread_State*)FNI_GetJNIEnv())->noheap) return;
#endif
  pthread_mutex_lock(&gc_thread_mutex);
  num_running_threads++;
  pthread_mutex_unlock(&gc_thread_mutex);
}

void find_other_thread_local_refs(struct FNI_Thread_State *curr_thrstate) {
#ifdef WITH_USER_THREADS
  struct gc_thread_list *rt = &gc_running_threads;
#else
  struct thread_list *rt = &running_threads;
#endif
  while(rt->next != NULL) {
    struct FNI_Thread_State *thrstate = rt->next->thrstate;
    if (thrstate != curr_thrstate) {
      error_gc("Other thread (%p)\n", thrstate);
      handle_local_refs_for_thread(thrstate);
    }
    rt = rt->next;
  }
}
#endif // WITH_HEAVY_THREADS || WITH_PTH_THREADS || WITH_USER_THREADS
#endif // WITH_PRECISE_GC
