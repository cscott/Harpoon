#include "config.h"
#include <jni.h>
#include <jni-private.h>
#include "java_lang_VMObject.h"
#ifdef WITH_TRANSACTIONS
# include "transact/transjni.h"
#endif /* WITH_TRANSACTIONS */

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
#ifdef WITH_TRANSACTIONS /* HACK HACK HACK */
  /* called inside transaction context.  since 'too many notifies' is
   * valid semantics, we don't have to worry about undo-ing this notify
   * if the transaction doesn't commit.  Lock, notify, unlock.
   * new transaction can't modify anything until this transaction commits.
   * XXX: possible problem because listener will wake up before this
   * transaction commits, causing a lost notification? */
  if (currTrans(env)) FNI_MonitorEnter(env, obj);
#endif
  fni_object_notify(env, obj);
#ifdef WITH_TRANSACTIONS
  if (currTrans(env)) FNI_MonitorExit(env, obj);
#endif
}

/*
 * Class:     java_lang_VMObject
 * Method:    notifyAll
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMObject_notifyAll
  (JNIEnv *env, jclass vmcls, jobject obj) {
#ifdef WITH_TRANSACTIONS /* HACK HACK HACK */
  /* same comments/problems as above */
  if (currTrans(env)) FNI_MonitorEnter(env, obj);
#endif
  fni_object_notifyAll(env, obj);
#ifdef WITH_TRANSACTIONS
  if (currTrans(env)) FNI_MonitorExit(env, obj);
#endif
}

/*
 * Class:     java_lang_VMObject
 * Method:    wait
 * Signature: (Ljava/lang/Object;JI)V
 */
JNIEXPORT void JNICALL Java_java_lang_VMObject_wait
  (JNIEnv *env, jclass vmcls, jobject obj, jlong ms, jint ns) {
#ifdef WITH_TRANSACTIONS
  /* should do a commit, then a wait (ought to be atomic w/respect to
   * notify) and then start a new (sub) transaction.  In other words,
   * really needs to return a CommitRec. */
  assert(!currTrans(env));
#endif
  fni_object_wait(env, obj, ms, ns);
}
