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

struct oobj_with_clheap {
  clheap_t clheap;
  struct oobj oobj;
};
#define CLHEAP_FROM_OOBJ(oobj) \
  (((struct oobj_with_clheap *) (((char *)oobj)-sizeof(clheap_t)))->clheap)

#ifdef REALLY_DO_ALLOC

#if defined(WITH_THREADS) && !defined(WITH_EVENT_DRIVEN)
/* Heavy-weight threads.  Not the thing to use if you've got an
 * event-driven transformation going on w/ your code. */
# define FETCH_THIS_THREAD() \
	(((struct FNI_Thread_State *)FNI_GetJNIEnv())->thread)
# define FETCH_THIS_THREAD_UNWRAPPED() FNI_UNWRAP(FETCH_THIS_THREAD())

#elif !defined(WITH_THREADS) && defined(WITH_EVENT_DRIVEN)
/* Event-driven code.  No heavy-weight threading allowed. */
# define FETCH_THIS_THREAD_UNWRAPPED() \
	Flex_harpoon_Analysis_ContBuilder_Scheduler_currentThread
# define FETCH_THIS_THREAD() FNI_WRAP(FETCH_THIS_THREAD_UNWRAPPED())

#else /* some other case */
# error unimplemented
#endif
#endif

void *NTHR_malloc(size_t size) {
#ifdef REALLY_DO_ALLOC
  return NTHR_malloc_other(size, FETCH_THIS_THREAD_UNWRAPPED());
#else
  UPDATE_STATS(thr, size);
  return NGBL_malloc_noupdate(size);
#endif
}
void *NTHR_malloc_first(size_t size) {
  clheap_t clh;
  struct oobj_with_clheap *result;
  UPDATE_STATS(thr, size);
#ifdef REALLY_DO_ALLOC
  clh = clheap_create();
  // the above line might be changed to pool clheaps.
  result = clheap_alloc(clh, size+sizeof(clheap_t));
  result->clheap = clh;
  return &(result->oobj);
#else
  return NGBL_malloc_noupdate(size);
#endif
}
void *NTHR_malloc_other(size_t size, struct oobj *oobj) {
  clheap_t clh; void *result;
  UPDATE_STATS(thr, size);
#ifdef REALLY_DO_ALLOC
  clh = CLHEAP_FROM_OOBJ(oobj);
  result = clheap_alloc(clh, size);
  if (result!=NULL) return result;
  printf("OVERFLOW FROM THREAD HEAP %p: %d bytes\n", clh, size);
  return NGBL_malloc_noupdate(size);
#else
  return NGBL_malloc_noupdate(size);
#endif
}
/* release a thread-clustered heap */
void NTHR_free(jobject obj) {
#ifdef REALLY_DO_ALLOC
  /* warning -- don't gc in here! We're going to unwrap the obj... */
  struct oobj *oobj = FNI_UNWRAP(obj);
  /* see if this might be a thread w/ a clustered heap */
#ifdef BDW_CONSERVATIVE_GC
  /* we're going to be uber-tricky here and ask the GC where the start of
   * this object is.  If there's margin, then it's on a clustered heap. */
  if (GC_base(oobj)!=oobj)
    clheap_detach(CLHEAP_FROM_OOBJ(oobj));
#elif 1
  /* risky assumptions! */
  if (CLHEAP_FROM_OOBJ(oobj)==(((char*)oobj)-sizeof(clheap_t)))
    clheap_detach(CLHEAP_FROM_OOBJ(oobj));
#else
# error Um, I dunno how you can tell whether this thread has an attached heap.
#endif
#endif
}
