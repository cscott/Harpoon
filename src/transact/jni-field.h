/* our own definitions of the JNI field methods */
#include "transact/transjni.h" /* JNI support functions */

#ifdef WITH_ROLE_INFER
# error Unsupported
#endif
#ifdef WITH_GENERATIONAL_GC
# error Unsupported
#endif

void FNI_SetObjectField(JNIEnv *env, jobject obj,
			jfieldID fieldID, jobject value) {
  assert(FNI_NO_EXCEPTIONS(env));
  TRANSJNI_Set_Object(env, obj, fieldID, FNI_UNWRAP(value));
}
#define FNI_SETFIELD(name,type) \
void FNI_Set##name##Field(JNIEnv *env, jobject obj, \
			  jfieldID fieldID, type value) { \
  assert(FNI_NO_EXCEPTIONS(env)); \
  TRANSJNI_Set_##name(env, obj, fieldID, value); \
}
FORPRIMITIVETYPES(FNI_SETFIELD);

jobject FNI_GetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  assert(FNI_NO_EXCEPTIONS(env));
  return FNI_WRAP(TRANSJNI_Get_Object(env, obj, fieldID));
}
#define FNI_GETFIELD(name,type) \
type FNI_Get##name##Field(JNIEnv *env, jobject obj, jfieldID fieldID) {\
  assert(FNI_NO_EXCEPTIONS(env)); \
  return TRANSJNI_Get_##name(env, obj, fieldID); \
}
FORPRIMITIVETYPES(FNI_GETFIELD);
