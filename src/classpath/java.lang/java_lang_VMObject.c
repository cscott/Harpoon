#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_VMObject.h"

#include <assert.h>
#include "../../java.lang/object.h" /* useful library-indep implementations */

#ifdef WITH_TRANSACTIONS
  /* transactions defines own versions of clone() */
#else /* !WITH_TRANSACTIONS */
/*
 * Class:     java_lang_VMObject
 * Method:    clone
 * Signature: (Ljava/lang/Cloneable;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_java_lang_VMObject_clone
  (JNIEnv *env, jclass vmcls, jobject obj) {
    struct claz *claz = FNI_CLAZ(FNI_UNWRAP_MASKED(obj));
    if (claz->component_claz==NULL) {
      /* not an array */
      uint32_t size = claz->size;
      return fni_object_cloneHelper(env, obj, size);
    } else { 
      /* an array of some primitive type */
      assert(0/*unimplemented*/);
      return NULL;
    }
}

/* slightly out-of-place, but here are the array-clone implementations */
#define PRIMITIVEARRAYCLONE(name, type, sig)\
JNIEXPORT jobject JNICALL Java__3##sig##_clone\
  (JNIEnv *env, jobject obj) {\
    jsize len = (*env)->GetArrayLength(env, (jarray) obj);\
    return fni_object_cloneHelper(env, obj, sizeof(struct aarray) + sizeof(type)*len);\
}
PRIMITIVEARRAYCLONE(Boolean, jboolean, Z);
PRIMITIVEARRAYCLONE(Byte, jbyte, B);
PRIMITIVEARRAYCLONE(Char, jchar, C);
PRIMITIVEARRAYCLONE(Short, jshort, S);
PRIMITIVEARRAYCLONE(Int, jint, I);
PRIMITIVEARRAYCLONE(Long, jlong, J);
PRIMITIVEARRAYCLONE(Float, jfloat, F);
PRIMITIVEARRAYCLONE(Double, jdouble, D);
/* not really a primitive =) */
PRIMITIVEARRAYCLONE(Object, struct oobj *, Ljava_lang_Object_2);
#endif /* !WITH_TRANSACTIONS */

/*
 * Class:     java_lang_Object
 * Method:    getClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_Object_getClass
  (JNIEnv *env, jobject obj) {
    return (*env)->GetObjectClass(env, obj);
}

/*
 * Class:     java_lang_VMObject
 * Method:    notify
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMObject_notify
  (JNIEnv *env, jclass vmcls, jobject obj) {
  fni_object_notify(env, obj);
}

/*
 * Class:     java_lang_VMObject
 * Method:    notifyAll
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMObject_notifyAll
  (JNIEnv *env, jclass vmcls, jobject obj) {
  fni_object_notifyAll(env, obj);
}

/*
 * Class:     java_lang_VMObject
 * Method:    wait
 * Signature: (Ljava/lang/Object;JI)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMObject_wait
  (JNIEnv *env, jclass vmcls, jobject obj, jlong ms, jint ns) {
  fni_object_wait(env, obj, ms, ns);
}

#ifdef WITH_TRANSACTIONS
// at the moment, all of this is very bogus.

/* transaction support.  no monitor is held initially. */
/*
 * Class:     java_lang_VMObject
 * Method:    notify
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMObject_notify_00024_00024withtrans
  (JNIEnv *env, jclass vmcls, jobject _this, jobject commitrec) {
  /* called inside transaction context.  since 'too many notifies' is
   * valid semantics, we don't have to worry about undo-ing this notify
   * if the transaction doesn't commit.  Lock, notify, unlock.
   * new transaction can't modify anything until this transaction commits.
   * XXX: possible problem because listener will wake up before this
   * transaction commits, causing a lost notification? */
  FNI_MonitorEnter(env, _this);
  Java_java_lang_VMObject_notify(env, vmcls, _this);
  FNI_MonitorExit(env, _this);
}

/*
 * Class:     java_lang_VMObject
 * Method:    notifyAll
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMObject_notifyAll_00024_00024withtrans
  (JNIEnv *env, jclass vmcls, jobject _this, jobject commitrec) {
  /* same comments as above. */
  FNI_MonitorEnter(env, _this);
  Java_java_lang_VMObject_notifyAll(env, vmcls, _this);
  FNI_MonitorExit(env, _this);
}

/*
 * Class:     java_lang_VMObject
 * Method:    wait
 * Signature: (Ljava/lang/Object;JI)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMObject_wait_00024_00024withtrans
  (JNIEnv *env, jclass vmcls, jobject obj, jlong ms, jint ns,
   jobject commitrec) {
  /* should do a commit, then a wait (ought to be atomic w/respect to
   * notify) and then start a new (sub) transaction.  In other words,
   * really needs to return a CommitRec. */
  assert(0);
}

#endif /* WITH_TRANSACTIONS */
