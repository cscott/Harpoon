/* Functions to allocate objects.  Currently is malloc()-based. */
#include <jni.h>
#include <jni-private.h>

#include <stdlib.h>
#include "config.h"
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif
#ifdef BDW_CONSERVATIVE_GC
#include "gc.h"
#endif

/* eventually many of these? */
void *FNI_RawAlloc(JNIEnv *env, jsize length) {
#ifdef BDW_CONSERVATIVE_GC
  return GC_malloc(length);
#else /* okay, use system-default... */
  return malloc(length);
#endif
}

/* Allocate and zero-out memory for the specified object type.
 * Returns NULL and throws OutOfMemoryError if it cannot
 * allocate the memory. */
jobject FNI_Alloc(JNIEnv *env, struct FNI_classinfo *info, jsize length) {
  struct oobj *newobj;

  newobj = FNI_RawAlloc(env, length);
  if (newobj==NULL) {
    FNI_ThrowNew(env, FNI_FindClass(env, "java/lang/OutOfMemoryError"),
		 "JNI: allocation failed");
    return NULL;
  }
  memset(newobj, 0, length);
  newobj->claz = info->claz;
  newobj->hashcode = (u_int32_t) newobj;
  return FNI_WRAP(newobj);
}
