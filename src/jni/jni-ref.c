/* manage local references */
#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include <stdlib.h>
#include <string.h> /* for memset */
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
#include "flexthread.h"

jobject FNI_NewLocalRef(JNIEnv *env, jobject_unwrapped obj) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  if (obj==NULL) return NULL; /* null stays null. */
  assert(fts->localrefs_next < fts->localrefs_end);
  assert(fts->localrefs_stack <= fts->localrefs_next);
  fts->localrefs_next->obj = obj;
  return (jobject) fts->localrefs_next++;
}

/* convenience function for runtime jni stub */
jobject_unwrapped FNI_Unwrap(jobject obj) {
  return FNI_UNWRAP(obj);
}

/* clear local refs in stack frame */
/* deletes local refs up to *and including* markerRef.
 * as a special case, markerRef may == fts->localrefs_next; i.e.
 * it may point to the next *unallocated* space, in which case
 * this function is a no-op. */
void FNI_DeleteLocalRefsUpTo(JNIEnv *env, jobject markerRef) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  /* assert validity of localrefs stack */
  /*  note that at least one item must be on stack, but stack may be full */
  assert(fts->localrefs_next <= fts->localrefs_end); /* stack may be full */
  assert(fts->localrefs_stack < fts->localrefs_next);/* at least one item */
  /* assert validity of marker ref: either on localrefs stack or one beyond */
  assert(markerRef            <= fts->localrefs_end);/* may point to unalloc */
  assert(fts->localrefs_stack <= markerRef);         /* may point to first */
  assert(markerRef            <= fts->localrefs_next);/*may point to unalloc */
#ifdef BDW_CONSERVATIVE_GC
  /* clear unused stack space, so as not to confuse conservative collector */
  memset(markerRef, 0, fts->localrefs_next - markerRef);
#endif
  fts->localrefs_next = markerRef;
}

void FNI_DeleteLocalRef(JNIEnv *env, jobject localRef) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  assert(FNI_NO_EXCEPTIONS(env));
  /* can't delete it w/o a lot of work; we just zero it out */
  localRef->obj=NULL; /* won't keep anything live */
  /* shrink the stack from the top, if possible */
#ifdef WITH_PRECISE_C_BACKEND
  /* precise-c pushes NULLs onto the stack occasionally; be conservative */
  if (localRef+1==fts->localrefs_next)
    fts->localrefs_next = localRef;
#else
  /* this works as long as there aren't any *valid* localrefs with value NULL*/
  while ((fts->localrefs_stack < fts->localrefs_next) &&
	 ((fts->localrefs_next-1)->obj==NULL))
    fts->localrefs_next--;
#endif /* WITH_PRECISE_C_BACKEND */
}

/*-----------------------------------------------------------------*/
FLEX_MUTEX_DECLARE_STATIC(globalref_mutex);

/* global refs are stored in a doubly-linked list */
jobject FNI_NewGlobalRef(JNIEnv * env, jobject obj) {
  jobject_globalref result;
  assert(FNI_NO_EXCEPTIONS(env));
  assert(obj!=NULL);
  /* malloc away... */
  result = 
#ifdef WITH_PRECISE_GC
    malloc
#elif defined(BDW_CONSERVATIVE_GC)
#ifdef WITH_GC_STATS
    GC_malloc_uncollectable_stats
#else
    GC_malloc_uncollectable
#endif
#elif WITH_REALTIME_JAVA
    RTJ_MALLOC_UNCOLLECTABLE
#else /* okay, use system-default malloc */
    malloc
#endif
      (sizeof(*result));
  result->jobject.obj = obj->obj;
  /* acquire global lock */
  FLEX_MUTEX_LOCK(&globalref_mutex);
  result->next = FNI_globalrefs.next;
  result->prev = &FNI_globalrefs;
  if (result->next) result->next->prev = result;
  FNI_globalrefs.next = result;
  /* release global lock */
  FLEX_MUTEX_UNLOCK(&globalref_mutex);
  /* done. */
  return (jobject) result;
}

void FNI_DeleteGlobalRef (JNIEnv *env, jobject _globalRef) {
  jobject_globalref globalRef = (jobject_globalref) _globalRef;
  assert(FNI_NO_EXCEPTIONS(env));
  /* acquire global lock */
  FLEX_MUTEX_LOCK(&globalref_mutex);
  /* always a prev, due to header; not always a next */
  globalRef->prev->next = globalRef->next;
  if (globalRef->next) globalRef->next->prev = globalRef->prev;
  /* release global lock */
  FLEX_MUTEX_UNLOCK(&globalref_mutex);
  /* clean up */
  globalRef->next = globalRef->prev = NULL; /* safety first */
#ifdef BDW_CONSERVATIVE_GC
  GC_free(globalRef);
#elif WITH_REALTIME_JAVA
  RTJ_FREE(globalRef);
#else /* system-default malloc... */
  free(globalRef);
#endif
}
