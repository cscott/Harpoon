#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include <stdio.h>

jint FNI_GetVersion(JNIEnv *env) {
  assert(FNI_NO_EXCEPTIONS(env));
  return 0x00010001; /* JNI version 1.1 */
}

/* do-nothing stubs */
jint FNI_UnregisterNatives(JNIEnv *env, jclass clazz) {
  assert(FNI_NO_EXCEPTIONS(env));
  return 0;
}

/* complain about an unimplemented method */
void FNI_Unimplemented(void) {
  fprintf(stderr, "Unimplemented JNI function.  Aborting.");
  assert(0);
}
