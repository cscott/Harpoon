/* Array-related JNI functions. [CSA] */
#include <jni.h>
#include "jni-private.h"

#include <assert.h>

jsize FNI_GetArrayLength(JNIEnv *env, jarray array) {
  struct aarray *a = (struct aarray *) FNI_UNWRAP(array);
  return a->length;
}
