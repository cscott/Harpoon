/* Field-related JNI functions.  [CSA] */
#include <jni.h>
#include "jni-private.h"

#include <assert.h>

void FNI_SetStaticObjectField(JNIEnv *env, jclass clazz,
			      jfieldID fieldID, jobject value) {
  assert(FNI_NO_EXCEPTIONS(env));
#ifdef WITH_ROLE_INFER
  Java_java_lang_RoleInference_fieldassign(env, NULL, NULL, fieldID, value);
#endif
  *((jobject_unwrapped *)(fieldID->offset)) = FNI_UNWRAP(value);
}
#define FNI_SETSTATICFIELD(name,type) \
void FNI_SetStatic##name##Field(JNIEnv *env, jclass clazz, \
				jfieldID fieldID, type value) { \
  assert(FNI_NO_EXCEPTIONS(env)); \
  *((type *)(fieldID->offset)) = value; \
}
FORPRIMITIVETYPES(FNI_SETSTATICFIELD);

jobject FNI_GetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  assert(FNI_NO_EXCEPTIONS(env));
  return FNI_WRAP(*((jobject_unwrapped *)(fieldID->offset)));
}
#define FNI_GETSTATICFIELD(name,type) \
type FNI_GetStatic##name##Field(JNIEnv *env, jclass clazz, jfieldID fieldID) {\
  assert(FNI_NO_EXCEPTIONS(env)); \
  return *((type *)(fieldID->offset)); \
}
FORPRIMITIVETYPES(FNI_GETSTATICFIELD);

void FNI_SetObjectField(JNIEnv *env, jobject obj,
			jfieldID fieldID, jobject value){
  assert(FNI_NO_EXCEPTIONS(env));
#ifdef WITH_ROLE_INFER
  Java_java_lang_RoleInference_fieldassign(env, NULL, obj, fieldID, value);
#endif
  *((jobject_unwrapped *)(fieldID->offset+(ptroff_t)FNI_UNWRAP(obj))) =
    FNI_UNWRAP(value);
}
#define FNI_SETFIELD(name,type) \
void FNI_Set##name##Field(JNIEnv *env, jobject obj, \
			  jfieldID fieldID, type value) { \
  assert(FNI_NO_EXCEPTIONS(env)); \
  *((type *)(fieldID->offset+(ptroff_t)FNI_UNWRAP(obj))) = value; \
}
FORPRIMITIVETYPES(FNI_SETFIELD);

jobject FNI_GetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  assert(FNI_NO_EXCEPTIONS(env));
  return FNI_WRAP(*((jobject_unwrapped *)(fieldID->offset +
					  (ptroff_t)FNI_UNWRAP(obj))));
}
#define FNI_GETFIELD(name,type) \
type FNI_Get##name##Field(JNIEnv *env, jobject obj, jfieldID fieldID) {\
  assert(FNI_NO_EXCEPTIONS(env)); \
  return *((type *)(fieldID->offset + (ptroff_t)FNI_UNWRAP(obj))); \
}
FORPRIMITIVETYPES(FNI_GETFIELD);

