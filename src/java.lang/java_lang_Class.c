#include <jni.h>
#include <jni-private.h>
#include "java_lang_Class.h"

#include <assert.h>

/*
 * Class:     java_lang_Class
 * Method:    forName
 * Signature: (Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_forName
  (JNIEnv *env, jclass cls, jstring str) {
    const char *name = (*env)->GetStringUTFChars(env, str, NULL);
    jclass result;
    char buf[strlen(name)+1], *cp;
    /* change . to / */
    strcpy(buf, name);
    for (cp=buf; *cp != '\0'; cp++)
      if (*cp == '.') *cp = '/';
    /* now look up translated name */
    result = (*env)->FindClass(env, buf);
    /* release memory and we're done! */
    (*env)->ReleaseStringUTFChars(env, str, name);
    return result;
}

// try to wrap currently active exception as the exception specified by
// the exclsname parameter.  if this fails, just throw the original exception.
static void wrapNthrow(JNIEnv *env, char *exclsname) {
    jthrowable ex = (*env)->ExceptionOccurred(env), nex;
    jobject descstr;
    jclass exCls, nexCls;
    jmethodID consID, toStrID;
    assert(ex); // exception set on entrance.
    (*env)->ExceptionClear(env);
    exCls = (*env)->GetObjectClass(env, ex);
    if ((*env)->ExceptionOccurred(env)) goto error;
    toStrID = (*env)->GetMethodID(env, exCls,
				  "toString", "()Ljava/lang/String;");
    if ((*env)->ExceptionOccurred(env)) goto error;
    descstr = (*env)->CallObjectMethod(env, ex, toStrID);
    if ((*env)->ExceptionOccurred(env)) goto error;
    nexCls = (*env)->FindClass(env, exclsname);
    if ((*env)->ExceptionOccurred(env)) goto error;
    consID = (*env)->GetMethodID(env, nexCls,
				 "<init>", "(Ljava/lang/String;)V");
    if ((*env)->ExceptionOccurred(env)) goto error;
    nex = (*env)->NewObject(env, nexCls, consID, descstr);
    if ((*env)->ExceptionOccurred(env)) goto error;
    (*env)->Throw(env, nex);
    return;
 error: // throw original error.
    (*env)->ExceptionClear(env);
    (*env)->Throw(env, ex);
    return;
}

/*
 * Class:     java_lang_Class
 * Method:    newInstance
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_newInstance
  (JNIEnv *env, jobject cls) {
    
    jobject result;
    jmethodID methodID=(*env)->GetMethodID(env, (jclass) cls, "<init>", "()V");
    /* if methodID=NULL, throw InstantiationException */
    if ((*env)->ExceptionOccurred(env)) goto error;
    result = (*env)->NewObject(env, (jclass) cls, methodID);
    if ((*env)->ExceptionOccurred(env)) goto error;
    return result;
    
  error:
    wrapNthrow(env, "java/lang/InstantiationException");
    return NULL;
}

/*
 * Class:     java_lang_Class
 * Method:    isInstance
 * Signature: (Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isInstance
  (JNIEnv *env, jobject cls, jobject obj) {
    /* May not work properly for obj==null */
    return (*env)->IsInstanceOf(env, obj, (jclass)cls);
}

/*
 * Class:     java_lang_Class
 * Method:    isAssignableFrom
 * Signature: (Ljava/lang/Class;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isAssignableFrom
  (JNIEnv *env, jobject obj, jclass cls) {
    return (*env)->IsAssignableFrom(env, (*env)->GetObjectClass(env, obj),cls);
}

/*
 * Class:     java_lang_Class
 * Method:    isInterface
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isInterface
  (JNIEnv *env, jobject cls) {
    /* Look for the class on its own interface list */
    struct claz *thisclz = FNI_GetClassInfo((jclass)cls)->claz;
    struct claz **ilist;
    for (ilist=thisclz->interfaces; *ilist!=NULL; ilist++)
      if (*ilist == thisclz) return JNI_TRUE;
    return JNI_FALSE;
}

/*
 * Class:     java_lang_Class
 * Method:    isArray
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isArray
  (JNIEnv *env, jobject cls) {
    struct claz *thisclz = FNI_GetClassInfo((jclass)cls)->claz;
    return (thisclz->component_claz==NULL) ? JNI_FALSE : JNI_TRUE;
}

/*
 * Class:     java_lang_Class
 * Method:    isPrimitive
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_lang_Class_isPrimitive
  (JNIEnv *env, jobject cls) {
    struct claz *thisclz = FNI_GetClassInfo((jclass)cls)->claz;
    /* weed out all non-primitives except for java/lang/Object */
    if (thisclz->display[0]!=thisclz) return JNI_FALSE;
    /* weed out java/lang/Object */
    if (*(thisclz->interfaces)!=NULL) return JNI_FALSE;
    return JNI_TRUE;
}

/*
 * Class:     java_lang_Class
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_Class_getName
  (JNIEnv *env, jobject cls) {
    struct FNI_classinfo *info = FNI_GetClassInfo((jclass)cls);
    return (*env)->NewStringUTF(env, info->name);
}

#if 0
/*
 * Class:     java_lang_Class
 * Method:    getClassLoader
 * Signature: ()Ljava/lang/ClassLoader;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getClassLoader
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getSuperclass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getSuperclass
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getInterfaces
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getInterfaces
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getComponentType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getComponentType
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_Class_getModifiers
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    getSigners
 * Signature: ()[Ljava/lang/Object;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getSigners
  (JNIEnv *, jobject);

/*
 * Class:     java_lang_Class
 * Method:    setSigners
 * Signature: ([Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_Class_setSigners
  (JNIEnv *, jobject, jobjectArray);
#endif

/*
 * Class:     java_lang_Class
 * Method:    getPrimitiveClass
 * Signature: (Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getPrimitiveClass
  (JNIEnv *env, jclass cls, jstring str) {
    return Java_java_lang_Class_forName(env, cls, str);
}

#if 0
/*
 * Class:     java_lang_Class
 * Method:    getFields0
 * Signature: (I)[Ljava/lang/reflect/Field;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getFields0
  (JNIEnv *, jobject, jint);

/*
 * Class:     java_lang_Class
 * Method:    getMethods0
 * Signature: (I)[Ljava/lang/reflect/Method;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getMethods0
  (JNIEnv *, jobject, jint);

/*
 * Class:     java_lang_Class
 * Method:    getConstructors0
 * Signature: (I)[Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getConstructors0
  (JNIEnv *, jobject, jint);

/*
 * Class:     java_lang_Class
 * Method:    getField0
 * Signature: (Ljava/lang/String;I)Ljava/lang/reflect/Field;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getField0
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     java_lang_Class
 * Method:    getMethod0
 * Signature: (Ljava/lang/String;[Ljava/lang/Class;I)Ljava/lang/reflect/Method;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getMethod0
  (JNIEnv *, jobject, jstring, jobjectArray, jint);

/*
 * Class:     java_lang_Class
 * Method:    getConstructor0
 * Signature: ([Ljava/lang/Class;I)Ljava/lang/reflect/Constructor;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_getConstructor0
  (JNIEnv *, jobject, jobjectArray, jint);

#endif
