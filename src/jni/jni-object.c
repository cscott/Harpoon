/* object operations. */
#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include <stdlib.h>

/* Allocates a new Java object without invoking any of the constructors
 * for the object. Returns a reference to the object. 
 * The clazz argument must not refer to an array class. 
 */
jobject FNI_AllocObject (JNIEnv *env, jclass clazz) {
  struct FNI_classinfo *info;
  u_int32_t size;
  struct oobj_offset *newobj;

  assert(FNI_NO_EXCEPTIONS(env) && clazz!=NULL);

  info = FNI_GetClassInfo(clazz);
  assert(info->name[0] != '[');
  size = info->claz->size; /* size, including the header */
  newobj = malloc(size);
  memset(newobj, 0, size);
  newobj->hashcode = (u_int32_t) OOBJ_UNOFFSET(newobj);
  newobj->obj.claz = info->claz;
  return FNI_WRAP(OOBJ_UNOFFSET(newobj));
}

/* Returns the class of an object. 
 * Returns a Java class object.
 */
jclass FNI_GetObjectClass (JNIEnv *env, jobject obj) {
  assert(FNI_NO_EXCEPTIONS(env) && obj != NULL);
  return FNI_WRAP(FNI_UNWRAP(obj)->claz->class_object);
}

/* Tests whether an object is an instance of a class. 
 * Returns JNI_TRUE if obj can be cast to clazz; otherwise,
 * returns JNI_FALSE. A NULL object can be cast to any class. 
 */
jboolean FNI_IsInstanceOf (JNIEnv *env, jobject obj, jclass clazz) {
  struct claz * objclz, * clsclz;

  assert(FNI_NO_EXCEPTIONS(env) && clazz != NULL);

  if (obj==NULL) return JNI_TRUE;/* flakey: not the same as java' instanceof */

  objclz = FNI_UNWRAP(obj)->claz;
  clsclz = FNI_GetClassInfo(clazz)->claz;
  /* first check invariant on scaled_class_depth.. */
  assert(objclz->display[objclz->scaled_class_depth/sizeof(struct claz *)]
	 == objclz && clsclz ==
	 clsclz->display[clsclz->scaled_class_depth/sizeof(struct claz *)]);
  return (objclz->display[clsclz->scaled_class_depth/sizeof(struct claz *)]
	  == clsclz) ? JNI_TRUE : JNI_FALSE;
}

/* Tests whether two references refer to the same Java object.  */
jboolean FNI_IsSameObject (JNIEnv *env, jobject ref1, jobject ref2) {
  return (FNI_UNWRAP(ref1) == FNI_UNWRAP(ref2)) ? JNI_TRUE : JNI_FALSE;
}


/* If clazz represents any class other than the class Object, then this
 * function returns the object that represents the superclass of the class
 * specified by clazz.
 *
 * If clazz specifies the class Object, or clazz represents an interface,
 * this function returns NULL. */
jclass FNI_GetSuperclass (JNIEnv *env, jclass clazz) {
  struct claz * clsclz;
  int depth;

  assert(FNI_NO_EXCEPTIONS(env) && clazz != NULL);
  clsclz = FNI_GetClassInfo(clazz)->claz;
  depth = clsclz->scaled_class_depth / sizeof(struct claz *);
  if (depth < 1) return NULL;
  return FNI_WRAP(clsclz->display[depth-1]->class_object);
}
