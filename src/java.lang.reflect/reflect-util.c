/* utility methods for reflection */
#include "config.h" /* for WITH_INIT_CHECK */
#include <assert.h>
#include <string.h>
#include <jni.h>
#include "jni-private.h"
#include "reflect-util.h"
#include "../java.lang/class.h" /*library-independent java.lang.Class methods*/

/* Return the (first character of the) descriptor corresponding to the
 * given jclass */
char REFLECT_getDescriptor(JNIEnv *env, jclass clazz) {
  struct FNI_classinfo *info;
  if (fni_class_isArray(env, clazz)) return '[';
  if (!fni_class_isPrimitive(env, clazz)) return 'L';
  info = FNI_GetClassInfo(clazz);
  switch (info->name[0]) {
  case 'b':
    if (strcmp(info->name, "boolean")==0) return 'Z';
    if (strcmp(info->name, "byte")==0) return 'B';
  case 'c': if (strcmp(info->name, "char")==0) return 'C';
  case 'd': if (strcmp(info->name, "double")==0) return 'D';
  case 'f': if (strcmp(info->name, "float")==0) return 'F';
  case 'i': if (strcmp(info->name, "int")==0) return 'I';
  case 'l': if (strcmp(info->name, "long")==0) return 'J';
  case 's': if (strcmp(info->name, "short")==0) return 'S';
  default: assert(0); /* not a valid primitive type name */
  }
}

/* Return the class object corresponding to the first component of the
 * given descriptor. */
jclass REFLECT_parseDescriptor(JNIEnv *env, const char *desc) {
    const char *name, *sigptr=desc;
    switch (desc[0]) {
    case 'Z': name="boolean"; break;
    case 'B': name="byte"; break;
    case 'C': name="char"; break;
    case 'S': name="short"; break;
    case 'I': name="int"; break;
    case 'J': name="long"; break;
    case 'F': name="float"; break;
    case 'D': name="double"; break;
    case '[':
      while (*sigptr=='[') sigptr++;
    case 'L':
      if (*sigptr=='L') while (*sigptr!=';') sigptr++;
      {
	  char buf[2+sigptr-desc];
	  strncpy(buf, desc+((desc[0]=='L')?1:0), 2+sigptr-desc);
	  buf[1+sigptr-desc]='\0';
	  if (desc[0]=='L') buf[sigptr-desc-1]='\0';
	  return (*env)->FindClass(env, buf);
      }
    default: assert(0); /* illegal signature */
    }
    /* it's a primitive class */
    return fni_class_getPrimitiveClass
	(env, (*env)->NewStringUTF(env, name));
}

/* Advance the given descriptor to the next component; returns NULL
 * if there are no more components. */
char *REFLECT_advanceDescriptor(char *sigptr) {
    switch (*sigptr) {
    case '[':
      while (*sigptr=='[') sigptr++;
    case 'L':
      if (*sigptr=='L') while (*sigptr!=';') sigptr++;
    default:;
    }
    assert(*sigptr!='[' && *sigptr!='L');
    sigptr++; /* one step forward */
    if (*sigptr && *sigptr!=')') return sigptr;
    else return NULL;
}

/* Return an object-wrapped version of the given primitive value.
 * The value ought to be in the proper field of the jvalue 'unwrapped'
 * and the type of the primitive (expressed as the single-character
 * type signature) in the 'type' argument. */
jobject REFLECT_wrapPrimitive(JNIEnv *env, jvalue unwrapped, char type) {
  char *classname, constructsig[] = { "(X)V" }; /* for wrapper creation */
  jclass wrapclaz; /* class object of the wrapper */
  jmethodID mid; /* methodID of the wrapper's constructor */

  /* determine proper wrapper class name */
  switch(constructsig[1]=type) {
  case 'Z': classname="java/lang/Boolean"; break;
  case 'B': classname="java/lang/Byte"; break;
  case 'C': classname="java/lang/Character"; break;
  case 'S': classname="java/lang/Short"; break;
  case 'I': classname="java/lang/Integer"; break;
  case 'J': classname="java/lang/Long"; break;
  case 'F': classname="java/lang/Float"; break;
  case 'D': classname="java/lang/Double"; break;
  default: assert(0); /* not the signature of a primitive type! */
  }
  /* look up wrapper class */
  wrapclaz = (*env)->FindClass(env, classname);
  assert(wrapclaz);
  /* fetch proper constructor */
  mid = (*env)->GetMethodID(env, wrapclaz, "<init>", constructsig);
  assert(mid);
  /* create the wrapper with the proper value! */
  return (*env)->NewObjectA(env, wrapclaz, mid, &unwrapped);
}

/* Unwrap the given object to the type requested by 'desired sig'.
 * May throw an 'IllegalArgumentException' if this cannot be done via
 * a valid widening conversion. */
jvalue REFLECT_unwrapPrimitive(JNIEnv *env, jobject wrapped, char desiredsig) {
  jclass cls; jmethodID mid; jvalue result;
  assert(env); assert(!(*env)->ExceptionOccurred(env));
  switch(desiredsig) {
  case 'D':
    cls = (*env)->FindClass(env, "java/lang/Double");
    if ((*env)->ExceptionOccurred(env)) (*env)->ExceptionClear(env);
    else if ((*env)->IsInstanceOf(env, wrapped, cls)) goto number;
  case 'F':
    cls = (*env)->FindClass(env, "java/lang/Float");
    if ((*env)->ExceptionOccurred(env)) (*env)->ExceptionClear(env);
    else if ((*env)->IsInstanceOf(env, wrapped, cls)) goto number;
    /* i don't know why long->float is a valid widening, but it is. */
  case 'J':
    cls = (*env)->FindClass(env, "java/lang/Long");
    if ((*env)->ExceptionOccurred(env)) (*env)->ExceptionClear(env);
    else if ((*env)->IsInstanceOf(env, wrapped, cls)) goto number;
  case 'I':
    cls = (*env)->FindClass(env, "java/lang/Integer");
    if ((*env)->ExceptionOccurred(env)) (*env)->ExceptionClear(env);
    else if ((*env)->IsInstanceOf(env, wrapped, cls)) goto number;
  case 'C':
    cls = (*env)->FindClass(env, "java/lang/Character");
    if ((*env)->ExceptionOccurred(env)) (*env)->ExceptionClear(env);
    else if ((*env)->IsInstanceOf(env, wrapped, cls)) goto character;
    if (desiredsig=='C') goto error; /* byte,short->char not valid widening */
  case 'S':
    cls = (*env)->FindClass(env, "java/lang/Short");
    if ((*env)->ExceptionOccurred(env)) (*env)->ExceptionClear(env);
    else if ((*env)->IsInstanceOf(env, wrapped, cls)) goto number;
  case 'B':
    cls = (*env)->FindClass(env, "java/lang/Byte");
    if ((*env)->ExceptionOccurred(env)) (*env)->ExceptionClear(env);
    else if ((*env)->IsInstanceOf(env, wrapped, cls)) goto number;
    goto error;
    /* boolean can't get widened. */
  case 'Z':
    cls = (*env)->FindClass(env, "java/lang/Boolean");
    if ((*env)->ExceptionOccurred(env)) (*env)->ExceptionClear(env);
    else if ((*env)->IsInstanceOf(env, wrapped, cls)) {
      mid = (*env)->GetMethodID(env, cls, "booleanValue", "()Z");
      result.z = (*env)->CallBooleanMethod(env, wrapped, mid);
      return result;
    }
    goto error;
  default:
    assert(0); /* not a valid signature */
  }

 character:
  /* this is a Character.  Extract the charValue()... */
  mid = (*env)->GetMethodID(env, cls, "charValue", "()C");
  result.c = (*env)->CallCharMethod(env, wrapped, mid);
  /* ...then widen */
  switch(desiredsig) {
  case 'C': return result;
  case 'I': result.i = result.c; return result;
  case 'J': result.j = result.c; return result;
  case 'F': result.f = result.c; return result;
  case 'D': result.d = result.c; return result;
  default: assert(0); /* not a valid widening from character! */
  }

 number:
  /* this is a Number. Extract directly and return */
  switch(desiredsig) { /* cls still has class of wrapper */
#define NUMBERCASE(sigchar, methodname, methodsig, jvalfield, shortname)\
  case sigchar:\
    mid = (*env)->GetMethodID(env, cls, methodname, methodsig);\
    result.jvalfield = (*env)->Call##shortname##Method(env, wrapped, mid);\
    return result
  NUMBERCASE('B', "byteValue",  "()B", b, Byte);
  NUMBERCASE('S', "shortValue", "()S", s, Short);
  NUMBERCASE('I', "intValue",   "()I", i, Int);
  NUMBERCASE('J', "longValue",  "()J", j, Long);
  NUMBERCASE('F', "floatValue", "()F", f, Float);
  NUMBERCASE('D', "doubleValue","()D", d, Double);
#undef NUMBERCASE
  default: assert(0); /* not a valid subtype of Number! */
  }

 error:
  /* Not a valid widening.  Throw an exception. */
  cls = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
  if (!(*env)->ExceptionOccurred(env))
    (*env)->ThrowNew(env, cls, "can't unwrap: not a valid widening.");
  return result; /* result will be bogus: we're throwing an exception */
}


#ifdef WITH_INIT_CHECK
/* run the static initializer for the given class. */
/* this method also copied to java_lang_reflect_Field.c */
int REFLECT_staticinit(JNIEnv *env, jclass c) {
  jmethodID methodID; jclass sc;
  /* XXX: Doesn't initialize interfaces */
  if (fni_class_isArray(env, c) &&
      !REFLECT_staticinit(env, fni_class_getComponentType(env, c)))
    return 0; /* fail if component type init fails */
  sc = (*env)->GetSuperclass(env, c);
  if (sc && !REFLECT_staticinit(env, sc))
    return 0; /* fail if superclass init fails */
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
