/* Thread heap allocation function. */

#include <jni.h>
#include <jni-private.h> /* for struct oobj */
#include "alloc.h"	/* for prototypes for NTHR_malloc* */
#include "config.h"	/* for WITH_THREADS */
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

#if WITH_THREADS
# define FETCH_THIS_THREAD() \
	(((struct FNI_Thread_State *)FNI_GetJNIEnv())->thread)
#else
# error unimplemented
#endif

void *NTHR_malloc(size_t size) {
  return NTHR_malloc_other(size, FNI_UNWRAP(FETCH_THIS_THREAD()));
}
void *NTHR_malloc_first(size_t size) {
  clheap_t clh;
  struct oobj_with_clheap *result;
  UPDATE_STATS(thr, size);
  clh = clheap_create();
  // the above line might be changed to pool clheaps.
  result = clheap_alloc(clh, size+sizeof(clheap_t));
  result->clheap = clh;
  return &(result->oobj);
}
void *NTHR_malloc_other(size_t size, struct oobj *oobj) {
  clheap_t clh;
  UPDATE_STATS(thr, size);
  clh = CLHEAP_FROM_OOBJ(oobj);
  return clheap_alloc(clh, size);
}
/* release a thread-clustered heap */
void NTHR_free(jobject obj) {
  /* warning -- don't gc in here! We're going to unwrap the obj... */
  struct oobj *oobj = FNI_UNWRAP(obj);
  /* see if this might be a thread w/ a clustered heap */
#ifdef BDW_CONSERVATIVE_GC
  /* we're going to be uber-tricky here and ask the GC where the start of
   * this object is.  If there's margin, then it's on a clustered heap. */
  if (GC_base(oobj)!=oobj)
    clheap_detach(CLHEAP_FROM_OOBJ(oobj));
#else
# error Um, I dunno how you can tell whether this thread has an attached heap.
#endif
}
