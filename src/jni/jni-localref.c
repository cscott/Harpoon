/* manage local references */
#include <jni.h>
#include <jni-private.h>

#include <stdlib.h>

jobject FNI_NewLocalRef(JNIEnv *env, jobject_unwrapped obj) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  jobject result;

  if (obj==NULL) return NULL; /* null stays null. */
  /* malloc away... */
  result = malloc(sizeof(*result));
  result->obj = obj;
  /* link this local ref into chain */
  result->next = fts->localrefs.next;
  fts->localrefs.next = result;
  /* done. */
  return result;
}
