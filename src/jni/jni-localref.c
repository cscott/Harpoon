/* manage local references */
#include <jni.h>
#include <jni-private.h>

#include <assert.h>
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

void FNI_DeleteLocalRef(JNIEnv *env, jobject localRef) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  jobject prev;
  assert(FNI_NO_EXCEPTIONS(env));
  /* scan through local refs until we find it. */
  for (prev = &(fts->localrefs); prev->next != NULL; prev=prev->next)
    if (prev->next == localRef) break;
  if (prev->next == localRef) {
    /* only free and unlink if we've really found localRef */
    free(localRef);
    prev->next = prev->next->next;
  }
}

jobject FNI_NewGlobalRef(JNIEnv * env, jobject obj) {
  jobject result;
  assert(FNI_NO_EXCEPTIONS(env));
  assert(obj!=NULL);
  /* malloc away... */
  result = malloc(sizeof(*result));
  result->obj = obj->obj;
  /* XXX: should acquire global lock */
  result->next = FNI_globalrefs.next;
  FNI_globalrefs.next = result;
  /* done. */
  return result;
}

void FNI_DeleteGlobalRef (JNIEnv *env, jobject globalRef) {
  jobject prev;
  assert(FNI_NO_EXCEPTIONS(env));
  /* XXX: should acquire global lock. */
  /* scan through local refs until we find it. */
  for (prev = &FNI_globalrefs; prev->next != NULL; prev=prev->next)
    if (prev->next == globalRef) break;
  if (prev->next == globalRef) {
    /* only free and unlink if we've really found localRef */
    free(globalRef);
    prev->next = prev->next->next;
  }
}

jboolean FNI_IsSameObject (JNIEnv *env, jobject ref1, jobject ref2) {
  return (FNI_UNWRAP(ref1) == FNI_UNWRAP(ref2)) ? JNI_TRUE : JNI_FALSE;
}
