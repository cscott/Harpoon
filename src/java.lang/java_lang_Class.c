#include <jni.h>
#include <jni-private.h>
#include "java_lang_Class.h"

#include <assert.h>

static void wrapNthrow(JNIEnv *env, char *exclsname);
#ifdef WITH_INIT_CHECK
static int  staticinit(JNIEnv *env, jclass cls);
#endif /* WITH_INIT_CHECK */

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
    /* actually, we need to create and throw a ClassNotFoundException if
     * things didn't go well; FindClass makes a NoClassDefFoundError,
     * which isn't the right type for us. */
    if ((*env)->ExceptionOccurred(env)) goto error;
    return result;

  error:
    wrapNthrow(env, "java/lang/ClassNotFoundException");
    return NULL;
}
#ifdef WITH_INIT_CHECK
JNIEXPORT jclass JNICALL Java_java_lang_Class_forName_00024_00024initcheck
  (JNIEnv *env, jclass cls, jstring str) {
  jclass result, clscls;
  jmethodID methodID;
  /* initialize java.lang.Class */
  clscls = (*env)->FindClass(env, "java/lang/Class");
  if (clscls && !staticinit(env, clscls)) return NULL; /* failed init */
  /* invoke Class.forName() */
  result = Java_java_lang_Class_forName(env, cls, str);
  /* now initialize the loaded class */
  if (result && !staticinit(env, result)) return NULL; /* failed init */
  return result;
}
#endif /* WITH_INIT_CHECK */

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
#ifdef WITH_INIT_CHECK
// run the static initializer for the given class.
static int staticinit(JNIEnv *env, jclass c) {
  jmethodID methodID; jclass sc;
  /* XXX: Doesn't initialize interfaces */
  sc = (*env)->GetSuperclass(env, c);
  if (sc && !staticinit(env, sc)) return 0; /* fail if superclass init fails */
  methodID=(*env)->GetStaticMethodID(env, c, "<clinit>", "()V");
  if (methodID==NULL) {
    (*env)->ExceptionClear(env); /* no static initializer, ignore */
  } else {
    (*env)->CallStaticVoidMethod(env, c, methodID);
    if ((*env)->ExceptionOccurred(env)) {
      wrapNthrow(env, "java/lang/ExceptionInInitializerError");
      return 0; /* exception in initializer */
    }
  }
  return 1; /* success */
}
#endif /* WITH_INIT_CHECK */


/*
 * Class:     java_lang_Class
 * Method:    newInstance
 * Signature: ()Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_Class_newInstance
  (JNIEnv *env, jobject cls) {
    jobject result;
    jmethodID methodID;
    /* okay, get constructor for this object and create it. */
    methodID=(*env)->GetMethodID(env, (jclass) cls, "<init>", "()V");
    /* if methodID=NULL, throw InstantiationException */
    if ((*env)->ExceptionOccurred(env)) goto error;
    result = (*env)->NewObject(env, (jclass) cls, methodID);
    if ((*env)->ExceptionOccurred(env)) goto error;
    return result;
    
  error:
    wrapNthrow(env, "java/lang/InstantiationException");
    return NULL;
}

#ifdef WITH_INIT_CHECK
JNIEXPORT jobject JNICALL Java_java_lang_Class_newInstance_00024_00024initcheck
  (JNIEnv *env, jobject cls) {
  /* this next static init may be redundant; class should be initialized
   * when Class object fetched via forName() or suchlike. */
  if (!staticinit(env, cls)) return NULL; /* init failed */
  return Java_java_lang_Class_newInstance(env, cls);
}
#endif /* WITH_INIT_CHECK */

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
    /* primitives have null in the first slot of the display. */
    if (thisclz->display[0]!=NULL) return JNI_FALSE;
    /* but so do interfaces.  weed them out using the interface list. */
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
    /* replace / with . so that we do the same thing as Sun's JDKs */
    char buf[strlen(info->name)+1], *dst; const char *src;
    for (src=info->name, dst=buf; *src; src++, dst++)
      *dst = (*src=='/')?'.':*src;
    *dst='\0';
    /* okay, create java string and go home */
    return (*env)->NewStringUTF(env, buf);
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
  return (*env)->GetSuperclass(env, (jclass) _this);
}

/*
 * Class:     java_lang_Class
 * Method:    getInterfaces
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_Class_getInterfaces
  (JNIEnv *env, jobject _this) {
  struct claz *thisclz = FNI_GetClassInfo((jclass)_this)->claz;
  struct claz **cp;
  jclass clscls;
  jobjectArray r;
  jsize i, ninterfaces = 0;
  /* count length of interfaces list */
  for (cp = thisclz->interfaces; *cp!=NULL; cp++)
    ninterfaces++;
  /* now create a Class array of the proper size */
  clscls = (*env)->FindClass(env, "java/lang/Class");
  if (!clscls) return NULL;
  r = (*env)->NewObjectArray(env, ninterfaces, clscls, NULL);
  if (!r) return NULL;
  /* set the elements of the Class array to the proper things. */
  for (i=0; i<ninterfaces; i++)
    (*env)->SetObjectArrayElement
      (env, r, i, FNI_WRAP(thisclz->interfaces[i]->class_object));
  /* done! */
  return r;
}

/*
 * Class:     java_lang_Class
 * Method:    getComponentType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Class_getComponentType
  (JNIEnv *env, jobject _this) {
  struct claz *thisclz = FNI_GetClassInfo((jclass)_this)->claz;
  struct claz *compclz = thisclz->component_claz;
  return compclz ? FNI_WRAP(compclz->class_object) : NULL;
}
#ifdef WITH_INIT_CHECK
JNIEXPORT jclass JNICALL Java_java_lang_Class_getComponentType_00024_00024initcheck
  (JNIEnv *env, jobject _this) {
  jclass r = Java_java_lang_Class_getComponentType(env, _this);
  if (!staticinit(env, r)) return NULL; /* init failed */
  return r;
}
#endif /* WITH_INIT_CHECK */

#if 0
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
