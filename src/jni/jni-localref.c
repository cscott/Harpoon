/* manage local references */
#include <jni.h>
#include <jni-private.h>

#include <stdlib.h>

jobject FNI_NewLocalRef(jobject_unwrapped obj) {
  jobject result;

  if (obj==NULL) return NULL; /* null stays null. */
  /* er, just malloc away for now */
  result = malloc(sizeof(*result));
  result->obj = obj;
  return result;
}
