#ifndef INCLUDED_FNI_SYSTEM_H
#define INCLUDED_FNI_SYSTEM_H

#include "config.h"
#include <string.h> /* for memmove */

#ifdef WITH_ROLE_INFER
# include "../../roles/roles.h" /*for Java_java_lang_RoleInference_arraycopy */
#endif

static inline 
jint fni_system_identityHashCode(JNIEnv *env, jclass cls, jobject obj) {
#ifndef WITH_HASHLOCK_SHRINK
    jobject_unwrapped oobj = FNI_UNWRAP_MASKED(obj);
    ptroff_t hashcode = oobj->hashunion.hashcode;
    if ((hashcode & 1) == 0)
      hashcode = INFLATED_MASK(oobj->hashunion.inflated)->hashcode;
    return (jint) (hashcode>>2);
#else
# ifndef BDW_CONSERVATIVE_GC
#  error This technique does not work w/ a non-conservative GC.
# endif
    return (ptroff_t) HIDE_POINTER(FNI_UNWRAP_MASKED(obj));
#endif /* WITH_HASHLOCK_SHRINK */
}

/* factor out arraycopy checks to make variant impls easier. */
static inline 
int fni_system_do_arraycopy_checks(JNIEnv *env, jobject src, jint srcpos,
				   jobject dst, jint dstpos, jint length) {
    jsize srclen, dstlen;
    int isPrimitive=0;
    static jclass arrcls=NULL;
    FLEX_MUTEX_DECLARE_STATIC(arrcls_lock);

    /* initialize arrcls */
    if (!arrcls) {
      FLEX_MUTEX_LOCK(&arrcls_lock);
      if (!arrcls) { /* double-check after aquiring lock */
	arrcls = (*env)->NewGlobalRef
	  (env, (*env)->FindClass(env, "[Ljava/lang/Object;"));
      }
      FLEX_MUTEX_UNLOCK(&arrcls_lock);
    }

    /* do checks */
    if (src==NULL || dst==NULL) {
      /* throw NullPointerException */
      jclass nulcls = (*env)->FindClass(env, "java/lang/NullPointerException");
      jmethodID methodID=(*env)->GetMethodID(env, nulcls, "<init>", "()V");
      (*env)->Throw(env, (*env)->NewObject(env, nulcls, methodID));
      return -1;
    }
    if (FNI_CLAZ(FNI_UNWRAP_MASKED(src))->component_claz==NULL) {
      jclass asecls = (*env)->FindClass
	(env, "java/lang/ArrayStoreException");
      (*env)->ThrowNew(env, asecls, "src not an array");
      return -1;
    }
    if (FNI_CLAZ(FNI_UNWRAP_MASKED(dst))->component_claz==NULL) {
      jclass asecls = (*env)->FindClass
	(env, "java/lang/ArrayStoreException");
      (*env)->ThrowNew(env, asecls, "dst not an array");
      return -1;
    }
    if ((*env)->IsInstanceOf(env, src, arrcls)==JNI_FALSE ||
	(*env)->IsInstanceOf(env, dst, arrcls)==JNI_FALSE ) {
      /* one or both is an array of primitive type... */
      if (FNI_CLAZ(FNI_UNWRAP_MASKED(src)) !=
	  FNI_CLAZ(FNI_UNWRAP_MASKED(dst)) ) {
	jclass asecls = (*env)->FindClass
	  (env, "java/lang/ArrayStoreException");
	(*env)->ThrowNew(env, asecls, "primitive array types don't match");
	return -1;
      }
      isPrimitive = 1;
    }
    /* length checks */
    srclen = (*env)->GetArrayLength(env, (jarray) src);
    dstlen = (*env)->GetArrayLength(env, (jarray) dst);
    if ((srcpos < 0) || (dstpos < 0) || (length < 0) ||
	(srcpos+length > srclen) || (dstpos+length > dstlen)) {
      jclass oobcls = (*env)->FindClass
	(env,"java/lang/ArrayIndexOutOfBoundsException");
      (*env)->ThrowNew(env, oobcls, "index out of bounds");
      return -1;
    }
    return isPrimitive;
}

static inline
void fni_system_arraycopy
  (JNIEnv *env, jclass syscls,
   jobject src, jint srcpos, jobject dst, jint dstpos,
   jint length) {

    int isPrimitive=fni_system_do_arraycopy_checks
      (env, src, srcpos, dst, dstpos, length);
    if (isPrimitive<0) return; /* exception occurred. */
    
    /* for primitive array, we're all set: */
    if (isPrimitive) {
      struct aarray *_src, *_dst;
      int size=0;
      assert(FNI_GetClassInfo(FNI_GetObjectClass(env, src))->name[0]=='[');
      switch(FNI_GetClassInfo(FNI_GetObjectClass(env, src))->name[1]) {
      case 'Z': size = sizeof(jboolean); break;
      case 'B': size = sizeof(jbyte); break;
      case 'C': size = sizeof(jchar); break;
      case 'S': size = sizeof(jshort); break;
      case 'I': size = sizeof(jint); break;
      case 'J': size = sizeof(jlong); break;
      case 'F': size = sizeof(jfloat); break;
      case 'D': size = sizeof(jdouble); break;
      default: assert(0); /* what kind of primitive array is this? */
      }
      _src=(struct aarray*) FNI_UNWRAP_MASKED(src);
      _dst=(struct aarray*) FNI_UNWRAP_MASKED(dst);
      /* note: we use memmove to allow the areas to overlap. */
      memmove(((char *)&(_dst->element_start))+(dstpos*size),
	     ((char *)&(_src->element_start))+(srcpos*size),
	     size*length);
      return;
    } else {
      /* check for overlap */
      int backward = ( FNI_IsSameObject(env, src, dst) && srcpos < dstpos );
      int i = backward ? (length-1) : 0;
#ifdef WITH_ROLE_INFER
      Java_java_lang_RoleInference_arraycopy(env, syscls, src, srcpos, dst, dstpos, length);
#endif
      while (backward ? (i >= 0) : (i < length)) {
	jobject o = (*env)->GetObjectArrayElement(env, src, srcpos+i);
	(*env)->SetObjectArrayElement(env, dst, dstpos+i, o);
	if ((*env)->ExceptionOccurred(env)!=NULL) return;
	if (backward) i--; else i++;
      }
      return;
    }
}

#endif /* INCLUDED_FNI_SYSTEM_H */
