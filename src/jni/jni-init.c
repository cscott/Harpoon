/* initialization code for the JNI. */
#include <jni.h>
#include <jni-private.h>
extern struct JNINativeInterface FLEX_JNI_vtable;

#include <stdlib.h>

/* no global refs, initially. */
struct _jobject FNI_globalrefs = { NULL, NULL };

JNIEnv * FNI_ThreadInit (void) {
  struct FNI_Thread_State * env = malloc(sizeof(*env));
  env->vtable = &FLEX_JNI_vtable;
  env->exception = NULL;
  env->localrefs.obj = NULL;
  env->localrefs.next= NULL;
  return (JNIEnv *) env;
}
