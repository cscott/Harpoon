#include "config.h" /* for WITH_INIT_CHECK, WITH_TRANSACTIONS */
#include <jni.h>
#include <jni-private.h>
#include "java_lang_Class.h"
#include "../../java.lang.reflect/java_lang_reflect_Member.h"
#include "../../java.lang.reflect/java_lang_reflect_Modifier.h"
#include "../../java.lang.reflect/reflect-util.h"
#ifdef WITH_TRANSACTIONS
# include "../../transact/transact.h"
#endif

#include <assert.h>
#define DEFINE_MEMBER_FUNCTIONS /* we want all the good stuff from class.h */
#include "../../java.lang/class.h" /* useful library-indep implementations */

/*
 * Class:     java_lang_Class
 * Method:    forName
 * Signature: (Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_forName
  (JNIEnv *env, jclass cls, jstring str) {
    return fni_class_forName(env, str);
}

#ifdef WITH_INIT_CHECK
JNIEXPORT jclass JNICALL Java_java_lang_Class_forName_00024_00024initcheck
  (JNIEnv *env, jclass cls, jstring str) {
    return fni_class_forName_initcheck(env, str);
}
#endif /* WITH_INIT_CHECK */
#ifdef WITH_TRANSACTIONS
JNIEXPORT jclass JNICALL Java_java_lang_Class_forName_00024_00024withtrans
  (JNIEnv *env, jclass cls, jobject commitrec, jstring str) {
    return fni_class_forName_withtrans(env, str, commitrec);
}
#endif /* WITH_TRANSACTIONS */

/*
 * Class:     java_lang_Class
 * Method:    newInstance
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_newInstance
  (JNIEnv *env, jobject cls) {
    return fni_class_newInstance(env, (jclass) cls);
}

#ifdef WITH_INIT_CHECK
JNIEXPORT jobject JNICALL Java_java_lang_Class_newInstance_00024_00024initcheck
  (JNIEnv *env, jobject cls) {
    return fni_class_newInstance_initcheck(env, (jclass) cls);
}
#endif /* WITH_INIT_CHECK */

#ifdef WITH_TRANSACTIONS
JNIEXPORT jobject JNICALL Java_java_lang_Class_newInstance_00024_00024withtrans
  (JNIEnv *env, jobject cls, jobject commitrec) {
    return fni_class_newInstance_withtrans(env, (jclass) cls, commitrec);
}
#endif /* WITH_TRANSACTIONS */

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
 * Method:    getClassLoader
 * Signature: ()Ljava/lang/ClassLoader;
 */
    /**
     * Determines the class loader for the class. 
     *
     * @return  the class loader that created the class or interface
     *          represented by this object, or <code>null</code> if the
     *          class was not created by a class loader.
     */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getClassLoader
  (JNIEnv *env, jobject _this) {
  return NULL; // XXX let's say nothing was ever created by a class loader.
}

/*
 * Class:     java_lang_Class
 * Method:    getSuperclass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getSuperclass
  (JNIEnv *env, jobject _this) {
    return fni_class_getSuperclass(env, (jclass) _this);
}

/*
 * Class:     java_lang_Class
 * Method:    getInterfaces
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getInterfaces
  (JNIEnv *env, jobject _this) {
    return fni_class_getInterfaces(env, (jclass) _this);
}

/*
 * Class:     java_lang_Class
 * Method:    getComponentType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getComponentType
  (JNIEnv *env, jobject _this) {
    return fni_class_getComponentType(env, (jclass) _this);
}
#ifdef WITH_INIT_CHECK
JNIEXPORT jclass JNICALL Java_java_lang_Class_getComponentType_00024_00024initcheck
  (JNIEnv *env, jobject _this) {
    return fni_class_getComponentType_initcheck(env, (jclass) _this);
}
#endif /* WITH_INIT_CHECK */

/*
 * Class:     java_lang_Class
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Class_getModifiers
  (JNIEnv *env, jclass _this) {
    return fni_class_getModifiers(env, (jclass) _this);
}

/*
 * Class:     java_lang_Class
 * Method:    getSigners
 * Signature: ()[Ljava/lang/Object;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getSigners
  (JNIEnv *env, jobject cls) {
  assert(0/*unimplemented*/);
}

/*
 * Class:     java_lang_Class
 * Method:    setSigners
 * Signature: ([Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Class_setSigners
  (JNIEnv *env, jobject cls, jobjectArray signers) {
  fprintf(stderr, "WARNING: Class.setSigners() unimplemented.\n");
}

/*
 * Class:     java_lang_Class
 * Method:    getPrimitiveClass
 * Signature: (Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getPrimitiveClass
  (JNIEnv *env, jclass cls, jstring str) {
    return fni_class_getPrimitiveClass(env, str);
}

/*
 * Class:     java_lang_Class
 * Method:    getFields0
 * Signature: (I)[Ljava/lang/reflect/Field;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getFields0
  (JNIEnv *env, jclass cls, jint which) {
    enum _fni_class_restrictionType _which = NONE;
    if (which==java_lang_reflect_Member_PUBLIC) _which = ONLY_PUBLIC;
    if (which==java_lang_reflect_Member_DECLARED) _which = ONLY_DECLARED;
    return fni_class_getMembers(env, cls, _which, FIELDS);
}

/*
 * Class:     java_lang_Class
 * Method:    getMethods0
 * Signature: (I)[Ljava/lang/reflect/Method;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getMethods0
  (JNIEnv *env, jclass cls, jint which) {
    enum _fni_class_restrictionType _which = NONE;
    if (which==java_lang_reflect_Member_PUBLIC) _which = ONLY_PUBLIC;
    if (which==java_lang_reflect_Member_DECLARED) _which = ONLY_DECLARED;
    return fni_class_getMembers(env, cls, _which, METHODS);
}

/*
 * Class:     java_lang_Class
 * Method:    getConstructors0
 * Signature: (I)[Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getConstructors0
  (JNIEnv *env, jclass cls, jint which) {
    enum _fni_class_restrictionType _which = NONE;
    if (which==java_lang_reflect_Member_PUBLIC) _which = ONLY_PUBLIC;
    if (which==java_lang_reflect_Member_DECLARED) _which = ONLY_DECLARED;
    return fni_class_getMembers(env, cls, _which, CONSTRUCTORS);
}

/*
 * Class:     java_lang_Class
 * Method:    getField0
 * Signature: (Ljava/lang/String;I)Ljava/lang/reflect/Field;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getField0
  (JNIEnv *env, jclass cls, jstring name, jint which) {
    enum _fni_class_restrictionType _which = NONE;
    if (which==java_lang_reflect_Member_PUBLIC) _which = ONLY_PUBLIC;
    if (which==java_lang_reflect_Member_DECLARED) _which = ONLY_DECLARED;
    return fni_class_getField(env, cls, name, _which);
}

/*
 * Class:     java_lang_Class
 * Method:    getMethod0
 * Signature: (Ljava/lang/String;[Ljava/lang/Class;I)Ljava/lang/reflect/Method;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getMethod0
  (JNIEnv *env, jclass cls, jstring name, jobjectArray paramTypes, jint which){
    enum _fni_class_restrictionType _which = NONE;
    if (which==java_lang_reflect_Member_PUBLIC) _which = ONLY_PUBLIC;
    if (which==java_lang_reflect_Member_DECLARED) _which = ONLY_DECLARED;
    return fni_class_getMethod(env, cls, name, paramTypes, _which);
}

/*
 * Class:     java_lang_Class
 * Method:    getConstructor0
 * Signature: ([Ljava/lang/Class;I)Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getConstructor0
  (JNIEnv *env, jclass cls, jobjectArray paramTypes, jint which) {
    enum _fni_class_restrictionType _which = NONE;
    if (which==java_lang_reflect_Member_PUBLIC) _which = ONLY_PUBLIC;
    if (which==java_lang_reflect_Member_DECLARED) _which = ONLY_DECLARED;
    return fni_class_getConstructor(env, cls, paramTypes, _which);
}
