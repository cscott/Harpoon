/* Functions to allocate objects.  Currently is malloc()-based. */
#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include <stdlib.h>
#include "config.h"
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

/* eventually many of these? */
void *FNI_RawAlloc(JNIEnv *env, jsize length) {
#if defined(WITH_CLUSTERED_HEAPS)
  return NGBL_malloc(length);
#elif defined(WITH_PRECISE_GC)
  return precise_malloc(length);
#elif defined(BDW_CONSERVATIVE_GC)
  return GC_malloc(length);
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
  struct oobj *newobj;

  assert(claz); /* info may be NULL.  claz may not be. */
  newobj = (allocfunc==NULL) ? FNI_RawAlloc(env,length) : (*allocfunc)(length);
  if (newobj==NULL) {
    FNI_ThrowNew(env, FNI_FindClass(env, "java/lang/OutOfMemoryError"),
		 "JNI: allocation failed");
    return NULL;
  }
  memset(newobj, 0, length);
  newobj->claz = claz;
  /* note -- setting the last bit also has the convenient property of
   * eliminating a possible self-cycle that would keep conservative gc
   * from finalizing the object. */
  newobj->hashunion.hashcode = 1 | (ptroff_t) newobj; /* low bit always set */
  /* FIXME: register finalizer.  */
  return FNI_WRAP(newobj);
}
