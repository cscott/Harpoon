/* Functions to allocate objects.  Currently is malloc()-based. */
#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include <stdlib.h>
#include "config.h"
#if defined(WITH_REALTIME_JAVA) || defined(WITH_REALTIME_JAVA_STUBS)
#include "../realtime/RTJconfig.h"
#endif
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif
#ifdef WITH_PRECISE_GC
#include "jni-gc.h"
#endif
#ifdef WITH_CLUSTERED_HEAPS
#include "../clheap/alloc.h"
#endif
#ifdef WITH_REALTIME_JAVA
#include "../realtime/RTJmalloc.h"
#endif

/* eventually many of these? */
void *FNI_RawAlloc(JNIEnv *env, jsize length) {
#if defined(WITH_CLUSTERED_HEAPS)
  return NGBL_malloc(length);
#elif defined(WITH_REALTIME_JAVA)
  return RTJ_malloc(length);
#elif defined(WITH_PRECISE_GC)
  return precise_malloc(length);
#elif defined(BDW_CONSERVATIVE_GC)
#ifdef WITH_GC_STATS
  return GC_malloc_stats(length);
#else
  return GC_malloc(length);
#endif
#else /* okay, use system-default... */
  return malloc(length);
#endif
}

/* Allocate and zero-out memory for the specified object type.
 * Returns NULL and throws OutOfMemoryError if it cannot
 * allocate the memory.  If allocfunc is NULL, uses FNI_RawAlloc.
 * Efficiency hack: 'info' may be NULL (in which case it is only
 * looked up if claz has a finalizer that needs to be registered). */
jobject FNI_Alloc(JNIEnv *env, struct FNI_classinfo *info, struct claz *claz,
		  void *(*allocfunc)(jsize length), jsize length) {
  struct oobj *newobj, *masked;
  jobject result;

  assert(claz); /* info may be NULL.  claz may not be. */
  newobj = (allocfunc==NULL) ? FNI_RawAlloc(env,length) : (*allocfunc)(length);
  if (newobj==NULL) {
    FNI_ThrowNew(env, FNI_FindClass(env, "java/lang/OutOfMemoryError"),
		 "JNI: allocation failed");
    return NULL;
  }
  masked = (struct oobj *) PTRMASK(newobj);
  memset(masked, 0, length);
#ifdef WITH_CLAZ_SHRINK
  masked->claz_index = claz->claz_index;
  assert(claz->claz_index < (1<<(8*WITH_CLAZ_SHRINK)));
#else
  masked->claz = claz;
#endif
  /* note -- setting the last bit also has the convenient property of
   * eliminating a possible self-cycle that would keep conservative gc
   * from finalizing the object. */
  masked->hashunion.hashcode = 1 | (ptroff_t) masked; /* low bit always set */
  /* FIXME: register finalizer.  */
  result = FNI_WRAP(newobj);
#if defined(WITH_REALTIME_JAVA) && defined(WITH_MEMORYAREA_TAGS)
  RTJ_tagObject(env, result);
#endif
  return result;
}
