/* Functions to allocate objects.  Currently is malloc()-based. */
#include <jni.h>
#include <jni-private.h>

#include <stdlib.h>
#include "config.h"
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif

/* eventually many of these? */
void *FNI_RawAlloc(JNIEnv *env, jsize length) {
  return malloc(length);
}

/* Allocate and zero-out memory for the specified object type.
 * Returns NULL and throws OutOfMemoryError if it cannot
 * allocate the memory. */
jobject FNI_Alloc(JNIEnv *env, struct FNI_classinfo *info, jsize length) {
  struct oobj_offset *newobj;

  newobj = FNI_RawAlloc(env, length);
  if (newobj==NULL) {
    FNI_ThrowNew(env, FNI_FindClass(env, "java/lang/OutOfMemoryError"),
		 "JNI: allocation failed");
    return NULL;
  }
  memset(newobj, 0, length);
  newobj->hashcode = (u_int32_t) OOBJ_UNOFFSET(newobj);
  newobj->obj.claz = info->claz;
  return FNI_WRAP(OOBJ_UNOFFSET(newobj));
}
