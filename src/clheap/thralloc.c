/* Thread heap allocation function. */

#include <jni.h>
#include <jni-private.h> /* for struct oobj */
#include "alloc.h"	/* for prototypes for NTHR_malloc* */
#include "config.h"	/* for WITH_THREADS */
#include "misc.h"       /* for REALLY_DO_ALLOC, etc */
#include "stats.h"      /* for UPDATE_STATS */
#include "clheap.h"	/* clheap_create, etc */
#ifdef BDW_CONSERVATIVE_GC
# include "gc.h"	/* for GC_base */
#endif
#include "flexthread.h" /* for mutex ops */

struct oobj_with_clheap {
  clheap_t clheap;
  struct oobj oobj;
};
#define CLHEAP_FROM_OOBJ(oobj) \
  (((struct oobj_with_clheap *) (((char *)oobj)-sizeof(clheap_t)))->clheap)

#ifdef REALLY_DO_THR_ALLOC

#if !defined(WITH_EVENT_DRIVEN)
/* Heavy-weight threads.  Not the thing to use if you've got an
 * event-driven transformation going on w/ your code. */
# define FETCH_THIS_THREAD() \
	(((struct FNI_Thread_State *)FNI_GetJNIEnv())->thread)
# define FETCH_THIS_THREAD_UNWRAPPED() FNI_UNWRAP(FETCH_THIS_THREAD())

#elif !defined(WITH_THREADS) && defined(WITH_EVENT_DRIVEN)
/* Event-driven code.  No heavy-weight threading allowed. */
extern struct oobj *_Flex_harpoon_Analysis_ContBuilder_Scheduler_currentThread;
# define FETCH_THIS_THREAD_UNWRAPPED() \
	_Flex_harpoon_Analysis_ContBuilder_Scheduler_currentThread
# define FETCH_THIS_THREAD() FNI_WRAP(FETCH_THIS_THREAD_UNWRAPPED())

#elif defined(WITH_USER_THREADS) && defined(WITH_EVENT_DRIVEN)
/* User threads look remarkable like heavy-weight threads. =) */
# define FETCH_THIS_THREAD() \
	(((struct FNI_Thread_State *)FNI_GetJNIEnv())->thread)
# define FETCH_THIS_THREAD_UNWRAPPED() FNI_UNWRAP(FETCH_THIS_THREAD())
#else /* some other case */
# error unimplemented
#endif
#endif

/* this function implements thread heap pooling. */
static int pool_pos = POOLSIZE-1; /* startup thread gets its own heap */
static clheap_t last_pool = NULL;
FLEX_MUTEX_DECLARE_STATIC(pool_mutex);
static clheap_t next_clheap() {
  clheap_t result;
  INCREMENT_STATS(threads_created, 1);
  FLEX_MUTEX_LOCK(&pool_mutex);
  if (pool_pos==0 || last_pool==NULL) {
    /* we create heaps with one attachment, which we detach before moving
     * to the next pool.  This guarantees that the heap won't be freed
     * before we've finished stuffing threads into it. */
    if (last_pool!=NULL) clheap_detach(last_pool); /* free last_pool */
    last_pool = clheap_create(); /* created with one use */
  }
  pool_pos = (pool_pos+1)%POOLSIZE;
  clheap_attach(result = last_pool); /* increment use count for the clheap */
  FLEX_MUTEX_UNLOCK(&pool_mutex);
  return result;
}

void *NTHR_malloc(size_t size) {
#ifdef REALLY_DO_THR_ALLOC
#if 0
  printf("THREAD ALLOCATING %ld bytes from %p\n",
	 (long) size, __builtin_return_address(0));
#endif
  return NTHR_malloc_other(size, FETCH_THIS_THREAD_UNWRAPPED());
#else
  UPDATE_NIFTY_STATS(thr, size);
  return NGBL_malloc_noupdate(size);
#endif
}
void *NTHR_malloc_with_heap(size_t size) {
  clheap_t clh;
  struct oobj_with_clheap *result;
  UPDATE_NIFTY_STATS(thr, size);
#ifdef REALLY_DO_THR_ALLOC
 tryagain:
  clh = next_clheap();
  result = clheap_alloc(clh, size+sizeof(clheap_t));
  if (result==NULL) { /* oops!  our thread heap is full! force new one. */
    FLEX_MUTEX_LOCK(&pool_mutex);
    pool_pos=0;
    FLEX_MUTEX_UNLOCK(&pool_mutex);
    clheap_detach(clh);
    goto tryagain;
  }
  result->clheap = clh;
  return &(result->oobj);
#else
  return NGBL_malloc_noupdate(size);
#endif
}
void *NGBL_malloc_with_heap(size_t size) {
  clheap_t clh;
  struct oobj_with_clheap *result;
  UPDATE_NIFTY_STATS(gbl, size);
#ifdef REALLY_DO_THR_ALLOC
  clh = next_clheap();
  // the above line might be changed to pool clheaps.
  result = NGBL_malloc_noupdate(size+sizeof(clheap_t));
  result->clheap = clh;
  return &(result->oobj);
#else
  return NGBL_malloc_noupdate(size);
#endif
}
void *NTHR_malloc_other(size_t size, struct oobj *oobj) {
  clheap_t clh; void *result;
  UPDATE_NIFTY_STATS(thr, size);
#ifdef REALLY_DO_THR_ALLOC
  clh = CLHEAP_FROM_OOBJ(oobj);
  result = clheap_alloc(clh, size);
  if (result!=NULL) return result;
  INCREMENT_STATS(thr_bytes_overflow,size);/*record overflow from thread heap*/
  return NGBL_malloc_noupdate(size);
#else
  return NGBL_malloc_noupdate(size);
#endif
}
/* release a thread-clustered heap */
void NTHR_free(jobject obj) {
#ifdef REALLY_DO_THR_ALLOC
  /* warning -- don't gc in here! We're going to unwrap the obj... */
  struct oobj *oobj = FNI_UNWRAP(obj);
  /* see if this might be a thread w/ a clustered heap */
#ifdef BDW_CONSERVATIVE_GC
  /* we're going to be uber-tricky here and ask the GC where the start of
   * this object is.  If there's margin, then it's on a clustered heap. */
  if (GC_base(oobj)!=NULL && GC_base(oobj)!=oobj)
    clheap_detach(CLHEAP_FROM_OOBJ(oobj));
#elif 1
  /* risky assumption: assume every thread has an attached heap. */
  clheap_detach(CLHEAP_FROM_OOBJ(oobj));
#else
# error Um, I dunno how you can tell whether this thread has an attached heap.
#endif
#endif
}
