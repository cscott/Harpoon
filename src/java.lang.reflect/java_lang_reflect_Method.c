#include "config.h" /* for WITH_INIT_CHECK, etc */
#include <assert.h>
#include <string.h>
#include <jni.h>
#include "jni-private.h"
#include "java_lang_reflect_Method.h"
#include "java_lang_reflect_Modifier.h"
#include "reflect-util.h"

/*
 * Class:     java_lang_reflect_Method
 * Method:    getModifiers
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_java_lang_reflect_Method_getModifiers
  (JNIEnv *env, jobject _this) {
    return FNI_GetMethodInfo(_this)->modifiers;
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    invoke
 * Signature: (Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;
 */
static jobject Flex_java_lang_reflect_Method_invoke
  (JNIEnv *env, jobject methodobj, jobject receiverobj, jobjectArray args,
   jboolean with_init_check) {
  struct FNI_method2info *method; /* method information */
  jclass methodclazz; /* declaring class of method */
  char *desc; /* method descriptor */
  char *sigptr; /* temporary pointer within the method descriptor */
  int nparams; /* number of parameters to the method */
  char retdesc; /* first character of return type descriptor */
  jvalue retval; /* return value of the method */
  jboolean isConstructor=JNI_FALSE; /* is this methodobj a constructor? */
  
  assert(methodobj!=NULL);
  method = FNI_GetMethodInfo(methodobj);
  assert(method!=NULL);
  methodclazz = FNI_WRAP(method->declaring_class_object);
  assert(!(*env)->ExceptionOccurred(env));

  /* according to spec, we need to do receiver checks first */
  if (!(method->modifiers & java_lang_reflect_Modifier_STATIC)) {
    if (receiverobj==NULL) {
      jclass excls = (*env)->FindClass(env, "java/lang/NullPointerException");
      (*env)->ThrowNew(env, excls, "null receiver for non-static field");
      return NULL;
    }
    if (!(*env)->IsInstanceOf(env, receiverobj, methodclazz)) {
      jclass excls=(*env)->FindClass(env,"java/lang/IllegalArgumentException");
      (*env)->ThrowNew(env, excls,
		       "receiver not instance of field declaring class");
      return NULL;
    }
  }
  if (method->modifiers & java_lang_reflect_Modifier_ABSTRACT) {
      jclass excls=(*env)->FindClass(env,"java/lang/IllegalAccessException");
      (*env)->ThrowNew(env, excls,
		       "attempted invocation of an abstract method");
      return NULL;
  }
  /* check number of args (note that args==null is equiv to 0-element args) */
  desc = method->methodID->desc; assert(desc && *desc=='(');
  for (nparams=0, sigptr=desc+1; sigptr!=NULL && *sigptr!=')';
       sigptr=REFLECT_advanceDescriptor(sigptr), nparams++)
    /* do nothing */;
  if (nparams != (args ? (*env)->GetArrayLength(env, args) : 0)) {
    jclass excls=(*env)->FindClass(env, "java/lang/IllegalArgumentException");
    (*env)->ThrowNew(env, excls, "incorrect number of arguments");
    return NULL;
  }
  /* okay, create argument array */
  {
    jvalue unwrapped[nparams];
    for (nparams=0, sigptr=desc+1; sigptr!=NULL && *sigptr!=')';
	 sigptr=REFLECT_advanceDescriptor(sigptr), nparams++) {
      jobject onearg = (*env)->GetObjectArrayElement(env, args, nparams);
      if (*sigptr=='L' || *sigptr=='[') {
	jclass argtype = REFLECT_parseDescriptor(env, sigptr);
	if ((*env)->ExceptionOccurred(env)) return NULL; /* bail on error */
	if (!(*env)->IsInstanceOf(env, onearg, argtype)) {
	  jclass excls=(*env)->FindClass(env,
					 "java/lang/IllegalArgumentException");
	  (*env)->ThrowNew(env, excls,
			   "couldn't widen object to parameter type");
	  return NULL;
	}
	unwrapped[nparams].l = onearg;
      } else {
	unwrapped[nparams]=REFLECT_unwrapPrimitive(env, onearg, *sigptr);
	if ((*env)->ExceptionOccurred(env)) return NULL; /* bail on error */
      }
    }
    /* determine if this method is a constructor */
    {
      jclass constrclazz =
	(*env)->FindClass(env, "java/lang/reflect/Constructor");
      if ((*env)->ExceptionOccurred(env)) (*env)->ExceptionClear(env);
      else isConstructor = (*env)->IsInstanceOf(env, methodobj, constrclazz);
    }
    /* find return type */
    retdesc = *(strchr(desc, ')')+1);
    /* invoke! */
    assert(!(*env)->ExceptionOccurred(env));
    if (method->modifiers & java_lang_reflect_Modifier_STATIC) {
      /* according to spec, must initialize declaring class here */
#ifdef WITH_INIT_CHECK
      if (with_init_check)
	if (!REFLECT_staticinit(env, methodclazz))
	  return NULL; /* ExceptionInInitializerError thrown */
#endif /* WITH_INIT_CHECK */
      /* ----------------- static methods --------------------- */
      switch (retdesc) {
      case 'V':
	(*env)->CallStaticVoidMethodA
	  (env, methodclazz, method->methodID, unwrapped);
	break;
#define STATICCASE(typename,shortname,longname,descchar,fieldname)\
      case descchar:\
	retval.fieldname = (*env)->CallStatic##shortname##MethodA\
	  (env, methodclazz, method->methodID, unwrapped);\
	break;
      FORPRIMITIVETYPESX(STATICCASE);
#undef STATICCASE
      default:
	/* object type */
	retval.l = (*env)->CallStaticObjectMethodA
	  (env, methodclazz, method->methodID, unwrapped);
	break;
      }
    } else if (isConstructor ||
	       method->modifiers & java_lang_reflect_Modifier_PRIVATE) {
      /* ----------------- non-virtual methods --------------------- */
      switch (retdesc) {
      case 'V':
	(*env)->CallNonvirtualVoidMethodA
	  (env, receiverobj, methodclazz, method->methodID, unwrapped);
	break;
#define NONVIRTUALCASE(typename,shortname,longname,descchar,fieldname)\
      case descchar:\
	retval.fieldname = (*env)->CallNonvirtual##shortname##MethodA\
	  (env, receiverobj, methodclazz, method->methodID, unwrapped);\
	break;
      FORPRIMITIVETYPESX(NONVIRTUALCASE);
#undef NONVIRTUALCASE
      default:
	/* object type */
	retval.l = (*env)->CallNonvirtualObjectMethodA
	  (env, receiverobj, methodclazz, method->methodID, unwrapped);
	break;
      }
    } else {
      /* ----------------- virtual methods --------------------- */
      switch (retdesc) {
      case 'V':
	(*env)->CallVoidMethodA
	  (env, receiverobj, method->methodID, unwrapped);
	break;
#define VIRTUALCASE(typename,shortname,longname,descchar,fieldname)\
      case descchar:\
	retval.fieldname = (*env)->Call##shortname##MethodA\
	  (env, receiverobj, method->methodID, unwrapped);\
	break;
      FORPRIMITIVETYPESX(VIRTUALCASE);
#undef VIRTUALCASE
      default:
	/* object type */
	retval.l = (*env)->CallObjectMethodA
	  (env, receiverobj, method->methodID, unwrapped);
	break;
      }
    }
  } /* call is done! (don't need the unwrapped args anymore) */
  if ((*env)->ExceptionOccurred(env)) { /* oops, exception occurred */
    /* wrap the exception in an InvocationTargetException and throw it */
    jthrowable exception, wrapped_exception;
    jclass wrapcls; jmethodID mid;
    exception = (*env)->ExceptionOccurred(env);
    (*env)->ExceptionClear(env);
    wrapcls = (*env)->FindClass
      (env, "java/lang/reflect/InvocationTargetException");
    if ((*env)->ExceptionOccurred(env)) return NULL; /* bail */
    mid = (*env)->GetMethodID
      (env, wrapcls, "<init>", "(Ljava/lang/Throwable;)V");
    if ((*env)->ExceptionOccurred(env)) return NULL; /* bail */
    wrapped_exception = (*env)->NewObject(env, wrapcls, mid, exception);
    if ((*env)->ExceptionOccurred(env)) return NULL; /* bail */
    (*env)->Throw(env, wrapped_exception);
    return NULL;
  }
  /* no exceptions!  wrap possibly-primitive retval and return */
  switch(retdesc) {
  case 'V': return NULL;
  case 'L': case '[': return retval.l;
  default:
    return REFLECT_wrapPrimitive(env, retval, retdesc); /* wrap! */
  }
  /* done */
}
/* actual 'no init check' implementation. */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Method_invoke
  (JNIEnv *env, jobject methodobj, jobject receiverobj, jobjectArray args) {
  return
    Flex_java_lang_reflect_Method_invoke(env, methodobj, receiverobj, args,
					 JNI_FALSE/* NO INIT CHECK */);
}
#ifdef WITH_INIT_CHECK /* 'with init check' implementation. */
JNIEXPORT jobject JNICALL Java_java_lang_reflect_Method_invoke_00024_00024initcheck
  (JNIEnv *env, jobject methodobj, jobject receiverobj, jobjectArray args) {
  return
    Flex_java_lang_reflect_Method_invoke(env, methodobj, receiverobj, args,
					 JNI_TRUE /* WITH INIT CHECK */);
}
#endif /* WITH_INIT_CHECK */

#if !defined(WITHOUT_HACKED_REFLECTION) /* this is our hacked implementation */
/*
 * Class:     java_lang_reflect_Method
 * Method:    getDeclaringClass
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Method_getDeclaringClass
  (JNIEnv *env, jobject _this) {
    return FNI_WRAP(FNI_GetMethodInfo(_this)->declaring_class_object);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_reflect_Method_getName
  (JNIEnv *env, jobject _this) {
    return (*env)->NewStringUTF(env, FNI_GetMethodInfo(_this)->methodID->name);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getReturnType
 * Signature: ()Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_reflect_Method_getReturnType
  (JNIEnv *env, jobject _this) {
    char *desc = FNI_GetMethodInfo(_this)->methodID->desc;
    assert(*desc=='('); /* method descriptors start with lparen */
    desc = strchr(desc, ')');
    assert(desc!=NULL); /* all method descriptors have a matched paren set */
    return REFLECT_parseDescriptor(env, desc+1);
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getParameterTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Method_getParameterTypes
  (JNIEnv *env, jobject _this) {
    jclass clscls = (*env)->FindClass(env, "java/lang/Class");
    jobjectArray result;
    char *desc = FNI_GetMethodInfo(_this)->methodID->desc;
    char *sigptr;
    int nparams=0;
    assert(*desc=='(');
    /* count number of parameters */
    for (sigptr=desc+1; sigptr!=NULL && *sigptr!=')';
	 sigptr=REFLECT_advanceDescriptor(sigptr))
	nparams++;
    /* create array of proper length */
    result = (*env)->NewObjectArray(env, nparams, clscls, NULL);
    /* fill array with parameters */
    nparams=0;
    for (sigptr=desc+1; sigptr!=NULL && *sigptr!=')';
	 sigptr=REFLECT_advanceDescriptor(sigptr))
	(*env)->SetObjectArrayElement
	    (env, result, nparams++, REFLECT_parseDescriptor(env, sigptr));
    /* done */
    return result;
}

/*
 * Class:     java_lang_reflect_Method
 * Method:    getExceptionTypes
 * Signature: ()[Ljava/lang/Class;
 */
JNIEXPORT jobjectArray JNICALL Java_java_lang_reflect_Method_getExceptionTypes
  (JNIEnv *env, jobject _this) {
    printf("WARNING: Java_java_lang_reflect_Method_getExceptionTypes unimplemented\n");
    return (*env)->NewObjectArray(env, (jsize)0, 
				  (*env)->FindClass(env, "java/lang/Exception"), NULL);
//  assert(0);
}

#endif /* !WITHOUT_HACKED_REFLECTION */
