/* Field-related JNI functions.  [CSA] */
#include <jni.h>
#include "jni-private.h"
#include "write_barrier.h"

#include <assert.h>

void FNI_SetStaticObjectField(JNIEnv *env, jclass clazz,
			      jfieldID fieldID, jobject value) {
  assert(FNI_NO_EXCEPTIONS(env));
#ifdef WITH_ROLE_INFER
  Java_java_lang_RoleInference_fieldassign(env, NULL, NULL, fieldID->reflectinfo, value);
#endif
  *((jobject_unwrapped *)PTRMASK(fieldID->offset)) = FNI_UNWRAP(value);
}
#define FNI_SETSTATICFIELD(name,type) \
void FNI_SetStatic##name##Field(JNIEnv *env, jclass clazz, \
				jfieldID fieldID, type value) { \
  assert(FNI_NO_EXCEPTIONS(env)); \
  *((type *)PTRMASK(fieldID->offset)) = value; \
}
FORPRIMITIVETYPES(FNI_SETSTATICFIELD);

jobject FNI_GetStaticObjectField(JNIEnv *env, jclass clazz, jfieldID fieldID) {
  assert(FNI_NO_EXCEPTIONS(env));
  return FNI_WRAP(*((jobject_unwrapped *)PTRMASK(fieldID->offset)));
}
#define FNI_GETSTATICFIELD(name,type) \
type FNI_GetStatic##name##Field(JNIEnv *env, jclass clazz, jfieldID fieldID) {\
  assert(FNI_NO_EXCEPTIONS(env)); \
  return *((type *)PTRMASK(fieldID->offset)); \
}
FORPRIMITIVETYPES(FNI_GETSTATICFIELD);

#if defined(WITH_TRANSACTIONS)
/* all of the following methods have different implementations when
 * running with transactions support: */
# include "transact/jni-field.h"
#else
/* your regularly scheduled program: */
void FNI_SetObjectField(JNIEnv *env, jobject obj,
			jfieldID fieldID, jobject value){
  assert(FNI_NO_EXCEPTIONS(env));
#ifdef WITH_ROLE_INFER
  Java_java_lang_RoleInference_fieldassign(env, NULL, obj, fieldID->reflectinfo, value);
#endif
#ifdef WITH_GENERATIONAL_GC
  generational_write_barrier((jobject_unwrapped *) 
			     (fieldID->offset +
			      (ptroff_t)FNI_UNWRAP_MASKED(obj)));
#endif
  *((jobject_unwrapped *)(fieldID->offset+(ptroff_t)FNI_UNWRAP_MASKED(obj))) =
    FNI_UNWRAP(value);
}
#define FNI_SETFIELD(name,type) \
void FNI_Set##name##Field(JNIEnv *env, jobject obj, \
			  jfieldID fieldID, type value) { \
  assert(FNI_NO_EXCEPTIONS(env)); \
  *((type *)(fieldID->offset+(ptroff_t)FNI_UNWRAP_MASKED(obj))) = value; \
}
FORPRIMITIVETYPES(FNI_SETFIELD);

jobject FNI_GetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  assert(FNI_NO_EXCEPTIONS(env));
  return FNI_WRAP(*((jobject_unwrapped *)(fieldID->offset +
					  (ptroff_t)FNI_UNWRAP_MASKED(obj))));
}
#define FNI_GETFIELD(name,type) \
type FNI_Get##name##Field(JNIEnv *env, jobject obj, jfieldID fieldID) {\
  assert(FNI_NO_EXCEPTIONS(env)); \
  return *((type *)(fieldID->offset + (ptroff_t)FNI_UNWRAP_MASKED(obj))); \
}
FORPRIMITIVETYPES(FNI_GETFIELD);

#endif /* !WITH_TRANSACTIONS */
