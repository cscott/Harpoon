#include <assert.h>
#include <jni.h>
#include "jni-private.h"
#include "java_lang_reflect_Constructor.h"
#include "java_lang_reflect_Method.h"
#include "java_lang_reflect_Modifier.h"
#include "reflect-util.h" /* for REFLECT_staticinit */

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Constructor_getModifiers
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getModifiers(env, _this);
}

/* helper */
static inline
jobject _newInstance
  (JNIEnv *env, jobject constructorobj, jobjectArray args, jboolean init) {
  struct FNI_method2info *method; /* method information */
  jclass methodclazz; /* declaring class of method */
  jobject result;
  
  assert(constructorobj!=NULL);
  method = FNI_GetMethodInfo(constructorobj);
  assert(method!=NULL);
  methodclazz = FNI_WRAP(method->declaring_class_object);
  assert(!(*env)->ExceptionOccurred(env));

#ifdef WITH_INIT_CHECK
  if (init)
    if (!REFLECT_staticinit(env, methodclazz)) return NULL; /* init failed */
#endif /* WITH_INIT_CHECK */

  /* check that declaring class is not abstract. */
  if (FNI_GetClassInfo(methodclazz)->modifiers & 
      java_lang_reflect_Modifier_ABSTRACT) {
      jclass excls=(*env)->FindClass(env,"java/lang/IllegalAccessException");
      (*env)->ThrowNew(env, excls,
		       "attempted instantiation of an abstract class");
      return NULL;
  }
  /* create zero-filled object instance. */
  result = (*env)->AllocObject(env, methodclazz);
  if ((*env)->ExceptionOccurred(env)) return NULL; /* bail */
  /* okay, now invoke constructor */
  Java_java_lang_reflect_Method_invoke(env, constructorobj, result, args);
  if ((*env)->ExceptionOccurred(env)) return NULL; /* bail */
  /* done, ta-da! */
  return result;
}
/*
 * Class:     java_lang_reflect_Constructor
 * Method:    newInstance
 * Signature: ([Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Constructor_newInstance
  (JNIEnv *env, jobject constructorobj, jobjectArray args) {
  return _newInstance(env, constructorobj, args, JNI_FALSE);
}
#ifdef WITH_INIT_CHECK
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Constructor_newInstance_00024_00024initcheck
  (JNIEnv *env, jobject constructorobj, jobjectArray args) {
  return _newInstance(env, constructorobj, args, JNI_TRUE);
}
#endif /* WITH_INIT_CHECK */

#if !defined(WITHOUT_HACKED_REFLECTION) /* this is our hacked implementation */
/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Constructor_getDeclaringClass
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getDeclaringClass(env, _this);
}

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_reflect_Constructor_getName
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getName(env, _this);
}

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getParameterTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Constructor_getParameterTypes
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getParameterTypes(env, _this);
}

/*
 * Class:     java_lang_reflect_Constructor
 * Method:    getExceptionTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Constructor_getExceptionTypes
  (JNIEnv *env, jobject _this) {
    return Java_java_lang_reflect_Method_getExceptionTypes(env, _this);
}

#endif /* !WITHOUT_HACKED_REFLECTION */
