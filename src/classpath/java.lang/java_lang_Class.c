#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_Class.h"

#include <assert.h>
#include "../../java.lang/class.h" /* useful library-indep implementations */

/*
 * Class:     java_lang_Class
 * Method:    isInstance
 * Signature: (Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isInstance
  (JNIEnv *env, jobject cls, jobject obj) {
    return fni_class_isInstance(env, (jclass)cls, obj);
}

/*
 * Class:     java_lang_Class
 * Method:    isAssignableFrom
 * Signature: (Ljava/lang/Class;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isAssignableFrom
  (JNIEnv *env, jobject cls1, jclass cls2) {
    return fni_class_isAssignableFrom(env, (jclass)cls1, cls2);
}

/*
 * Class:     java_lang_Class
 * Method:    isInterface
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isInterface
  (JNIEnv *env, jobject cls) {
    return fni_class_isInterface(env, (jclass)cls);
}

/*
 * Class:     java_lang_Class
 * Method:    isArray
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isArray
  (JNIEnv *env, jobject cls) {
    return fni_class_isArray(env, (jclass)cls);
}

/*
 * Class:     java_lang_Class
 * Method:    isPrimitive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isPrimitive
  (JNIEnv *env, jobject cls) {
    return fni_class_isPrimitive(env, (jclass)cls);
}

/*
 * Class:     java_lang_Class
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_Class_getName
  (JNIEnv *env, jobject cls) {
    return fni_class_getName(env, (jclass)cls);
}

/*
 * Class:     java_lang_Class
 * Method:    getSuperclass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getSuperclass
  (JNIEnv *env, jobject cls) {
    return fni_class_getSuperclass(env, (jclass)cls);
}

/*
 * Class:     java_lang_Class
 * Method:    getInterfaces
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getInterfaces
  (JNIEnv *env, jobject cls) {
    return fni_class_getInterfaces(env, (jclass)cls);
}

/*
 * Class:     java_lang_Class
 * Method:    getComponentType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getComponentType
   (JNIEnv *env, jobject cls) {
    return fni_class_getComponentType(env, (jclass)cls);
}

/*
 * Class:     java_lang_Class
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Class_getModifiers
  (JNIEnv *env, jobject cls) {
    return fni_class_getModifiers(env, (jclass)cls);
}

/*
 * Class:     java_lang_Class
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getDeclaringClass
  (JNIEnv *env, jobject cls) {
    assert(0); /* unimplemented */
}

/*
 * Class:     java_lang_Class
 * Method:    getClasses
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getClasses
  (JNIEnv *env, jobject cls) {
    assert(0); /* unimplemented */
}

/*
 * Class:     java_lang_Class
 * Method:    getFields
 * Signature: ()[Ljava/lang/reflect/Field;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getFields
  (JNIEnv *env, jobject cls) {
    return fni_class_getMembers(env, (jclass)cls, ONLY_PUBLIC, FIELDS);
}

/*
 * Class:     java_lang_Class
 * Method:    getMethods
 * Signature: ()[Ljava/lang/reflect/Method;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getMethods
  (JNIEnv *env, jobject cls) {
    return fni_class_getMembers(env, (jclass)cls, ONLY_PUBLIC, METHODS);
}

/*
 * Class:     java_lang_Class
 * Method:    getConstructors
 * Signature: ()[Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getConstructors
  (JNIEnv *env, jobject cls) {
    return fni_class_getMembers(env, (jclass)cls, ONLY_PUBLIC, CONSTRUCTORS);
}

/*
 * Class:     java_lang_Class
 * Method:    getField
 * Signature: (Ljava/lang/String;)Ljava/lang/reflect/Field;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getField
  (JNIEnv *env, jobject cls, jstring name) {
    return fni_class_getField(env, (jclass)cls, name, ONLY_PUBLIC);
}

/*
 * Class:     java_lang_Class
 * Method:    getMethod
 * Signature: (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getMethod
  (JNIEnv *env, jobject cls, jstring name, jobjectArray paramTypes) {
    return fni_class_getMethod(env, (jclass)cls, name, paramTypes,
			       ONLY_PUBLIC);
}

/*
 * Class:     java_lang_Class
 * Method:    getConstructor
 * Signature: ([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getConstructor
  (JNIEnv *env, jobject cls, jobjectArray paramTypes) {
    return fni_class_getConstructor(env, (jclass)cls, paramTypes, ONLY_PUBLIC);
}

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredClasses
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredClasses
  (JNIEnv *env, jobject cls) {
    assert(0); /* unimplemented */
}

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredFields
 * Signature: ()[Ljava/lang/reflect/Field;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredFields
  (JNIEnv *env, jobject cls) {
    return fni_class_getMembers(env, (jclass)cls, ONLY_DECLARED, FIELDS);
}

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredMethods
 * Signature: ()[Ljava/lang/reflect/Method;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredMethods
  (JNIEnv *env, jobject cls) {
    return fni_class_getMembers(env, (jclass)cls, ONLY_DECLARED, METHODS);
}

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredConstructors
 * Signature: ()[Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getDeclaredConstructors
  (JNIEnv *env, jobject cls) {
    return fni_class_getMembers(env, (jclass)cls, ONLY_DECLARED, CONSTRUCTORS);
}

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredField
 * Signature: (Ljava/lang/String;)Ljava/lang/reflect/Field;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getDeclaredField
  (JNIEnv *env, jobject cls, jstring name) {
    return fni_class_getField(env, (jclass)cls, name, ONLY_DECLARED);
}

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredMethod
 * Signature: (Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getDeclaredMethod
  (JNIEnv *env, jobject cls, jstring name, jobjectArray paramTypes) {
    return fni_class_getMethod(env, (jclass)cls, name, paramTypes,
			       ONLY_DECLARED);
}

/*
 * Class:     java_lang_Class
 * Method:    getDeclaredConstructor
 * Signature: ([Ljava/lang/Class;)Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getDeclaredConstructor
  (JNIEnv *env, jobject cls, jobjectArray paramTypes) {
    return fni_class_getConstructor(env, (jclass)cls, paramTypes,
				    ONLY_DECLARED);
}
