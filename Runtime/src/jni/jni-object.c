/* object operations. */
#include <jni.h>
#include <jni-private.h>

#include <assert.h>
#include <stdlib.h>

#include "compiler.h" /* for likely()/unlikely() */
#include "../java.lang/class.h" /* for fni_class_isInterface */

/* Allocates a new Java object without invoking any of the constructors
 * for the object. Returns a reference to the object. 
 * The clazz argument must not refer to an array class. 
 * Returns a Java object, or NULL if the object cannot be constructed. 
 * THROWS: 
 *   InstantiationException: if the class is an interface or an abstract
 *                           class. 
 *   OutOfMemoryError: if the system runs out of memory. 
 */
jobject FNI_AllocObject (JNIEnv *env, jclass clazz) {
  return FNI_AllocObject_using(env, clazz, NULL/* use default alloc func */);
}
/* allocate using a specific allocation function */
jobject FNI_AllocObject_using (JNIEnv *env, jclass clazz,
			       void *(*allocfunc)(size_t length)) {
  struct FNI_classinfo *info;
  struct claz *claz;
  uint32_t size;

  assert(FNI_NO_EXCEPTIONS(env) && clazz!=NULL);

  info = FNI_GetClassInfo(clazz);
  assert(info->name[0] != '[');
  /* FIXME: we don't check to see whether it's an interface or abstract */
  claz = info->claz;
  size = claz->size; /* size, including the header */
  return FNI_Alloc(env, info, claz, allocfunc, size);
}

/* Constructs a new Java object. The method ID indicates which constructor
 * method to invoke. This ID must be obtained by calling GetMethodID() with
 * <init> as the method name and void (V) as the return type. 
 *
 * The clazz argument must not refer to an array class. 
 *
 * Returns a Java object, or NULL if the object cannot be constructed. 
 * THROWS: 
 *   InstantiationException: if the class is an interface or an abstract
 *                           class. 
 *   OutOfMemoryError: if the system runs out of memory. 
 *   Any exceptions thrown by the constructor. 
 */
jobject FNI_NewObject(JNIEnv *env, jclass clazz, jmethodID methodID, ...) {
  jobject result;
  va_list varargs;
  assert(FNI_NO_EXCEPTIONS(env));
  va_start(varargs, methodID);
  result=FNI_NewObjectV(env, clazz, methodID, varargs);
  va_end(varargs);
  return result;
}
jobject FNI_NewObjectA(JNIEnv *env, jclass clazz, jmethodID methodID,
		       jvalue *args) {
  jobject result;
  assert(FNI_NO_EXCEPTIONS(env));
  result = FNI_AllocObject(env, clazz);
  if (unlikely(FNI_ExceptionOccurred(env)!=NULL)) return result; /* bail */
#ifdef WITH_ROLE_INFER
  NativeassignUID(env,result,clazz);
#endif
  FNI_CallNonvirtualVoidMethodA(env, result, clazz, methodID, args);
  return result;
}
jobject FNI_NewObjectV(JNIEnv *env, jclass clazz, jmethodID methodID,
		       va_list args) {
  jobject result;
  assert(FNI_NO_EXCEPTIONS(env));
  result = FNI_AllocObject(env, clazz);
  if (unlikely(FNI_ExceptionOccurred(env)!=NULL)) return result; /* bail */
#ifdef WITH_ROLE_INFER
  NativeassignUID(env,result,clazz);
#endif
  FNI_CallNonvirtualVoidMethodV(env, result, clazz, methodID, args);
  return result;
}

/* Returns the class of an object. 
 * Returns a Java class object.
 */
jclass FNI_GetObjectClass (JNIEnv *env, jobject obj) {
  assert(FNI_NO_EXCEPTIONS(env) && obj != NULL);
  return FNI_WRAP(FNI_CLAZ(FNI_UNWRAP_MASKED(obj))->class_object);
}

/* Tests whether an object is an instance of a class. 
 * Returns JNI_TRUE if obj can be cast to clazz; otherwise,
 * returns JNI_FALSE. A NULL object can be cast to any class. 
 */
jboolean FNI_IsInstanceOf (JNIEnv *env, jobject obj, jclass clazz) {
  struct claz * objclz, * clsclz;

  assert(FNI_NO_EXCEPTIONS(env) && clazz != NULL);

  if (obj==NULL) return JNI_TRUE;/* flakey: not the same as java' instanceof */

  objclz = FNI_CLAZ(FNI_UNWRAP_MASKED(obj));
  clsclz = FNI_GetClassInfo(clazz)->claz;
  /* different tests depending on whether clazz is a class or interface type*/
  if (0==clsclz->scaled_class_depth && clsclz!=clsclz->display[0]) {
    /* this is an interface or array of interfaces.
     * use the interface instanceof test. */
    struct claz **ilist;
    for (ilist=objclz->interfaces; *ilist!=NULL; ilist++)
      if (*ilist == clsclz) return JNI_TRUE;
    return JNI_FALSE;
  }
  /* this is a class; use the class instanceof test */
  return (objclz->display[clsclz->scaled_class_depth/sizeof(struct claz *)]
	  == clsclz) ? JNI_TRUE : JNI_FALSE;
}

/* Tests whether two references refer to the same Java object.  */
jboolean FNI_IsSameObject (JNIEnv *env, jobject ref1, jobject ref2) {
  return (FNI_UNWRAP_MASKED(ref1) == FNI_UNWRAP_MASKED(ref2)) ? JNI_TRUE : JNI_FALSE;
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
/* Determines whether an object of clazz1 can be safely cast to clazz2.
 *
 * Returns JNI_TRUE if any of the following are true: 
 *
 *   The first and second class arguments refer to the same Java class. 
 *   The first class is a subclass of the second class. 
 *   The first class has the second class as one of its interfaces. 
 */
jboolean FNI_IsAssignableFrom(JNIEnv *env, jclass clazz1, jclass clazz2) {
  struct claz *claz1 = FNI_GetClassInfo((jclass)clazz1)->claz;
  struct claz *claz2 = FNI_GetClassInfo((jclass)clazz2)->claz;
  struct claz **ilist;
  int depth;
  assert(fni_class_isInterface(env, clazz1)==JNI_FALSE);
  /* first and second class arguments refer to the same java class? */
  if (claz1==claz2) return JNI_TRUE;
  /* the first class is a subclass of the second class? */
  depth = claz2->scaled_class_depth / sizeof(struct claz *);
  if (claz1->display[depth] == claz2) return JNI_TRUE;
  /* the first class has the second class as one of its interfaces? */
  for (ilist=claz1->interfaces; *ilist!=NULL; ilist++)
    if (*ilist == claz2) return JNI_TRUE;
  /* okay, no tests were true.  must not be assignable */
  return JNI_FALSE;
}
