#include <stdarg.h>
#include <assert.h>
#include "jni.h"
#include "jni-private.h"
#ifdef WITH_PRECISE_C_BACKEND
#include "precisec.c" /* drag in implementations of FNI_Dispatch_<foo> */
#endif
  
extern void FNI_Dispatch_Void(ptroff_t method_pointer, int narg_words,
			      void * argptr, jobject_unwrapped * exception);
extern jobject_unwrapped FNI_Dispatch_Object(ptroff_t method_pointer,
					     int narg_words, void * argptr,
					     jobject_unwrapped * exception);
#define FNI_DISPATCH_PROTO(name, type) \
   extern type FNI_Dispatch_##name(ptroff_t method_pointer, int narg_words, \
				   void *argptr, jobject_unwrapped *exception);
FORPRIMITIVETYPES(FNI_DISPATCH_PROTO);

/* OK, wrap & move arguments based on signature. */
static void move_and_unwrapA(jmethodID methodID,
			     ptroff_t *argtable, int offset,
			     jvalue * args) {
  char *sigptr = methodID->desc+1;
  int i, j;
  for (i=offset, j=0; *sigptr != ')'; sigptr++)
    switch (*sigptr) {
    case 'B': argtable[i++] = (ptroff_t) ((jint) args[j++].b); break;
    case 'C': argtable[i++] = (ptroff_t) ((jint) args[j++].c); break;
    case 'F': /* floats are same size as ints. */
    case 'I': argtable[i++] = (ptroff_t) ((jint) args[j++].i); break;
    case 'S': argtable[i++] = (ptroff_t) ((jint) args[j++].s); break;
    case 'Z': argtable[i++] = (ptroff_t) ((jint) args[j++].z); break;
    case 'D': /* doubles are same size as longs. */
    case 'J':
      if (sizeof(ptroff_t) >= sizeof(jlong))
	argtable[i++] = (ptroff_t) args[j++].j;
      else {
	union { jlong l; struct { ptroff_t p1; ptroff_t p2; } p; } u;
	u.l = args[j++].j;
	argtable[i++] = u.p.p1;
	argtable[i++] = u.p.p2;
      }
      break;
    case '[':
      while (*sigptr=='[') sigptr++;
    case 'L':
      if (*sigptr=='L') while (*sigptr!=';') sigptr++;
      argtable[i++] = (ptroff_t) FNI_UNWRAP(args[j++].l);
      break;
    default: assert(0); /* illegal signature */
    }
  assert(methodID->nargs==i);
}

/* weird stuff for default argument promotions. */
/* XXX: this breaks if sizeof(int) < sizeof(jint) */
typedef int jboolean_promoted;
typedef int jbyte_promoted;
typedef int jchar_promoted;
typedef int jshort_promoted;
typedef int jint_promoted;
typedef jlong jlong_promoted;
typedef double jfloat_promoted;
typedef double jdouble_promoted;

static void move_and_unwrapV(jmethodID methodID,
			     ptroff_t *argtable, int offset,
			     va_list args) {
  char *sigptr = methodID->desc+1;
  int i;
  assert(sizeof(int) >= sizeof(jint)); /* see defs of _promoted types above */
  for (i=offset; *sigptr != ')'; sigptr++)
    switch (*sigptr) {
    case 'B': argtable[i++] = (ptroff_t) va_arg(args, jbyte_promoted); break;
    case 'C': argtable[i++] = (ptroff_t) va_arg(args, jchar_promoted); break;
    case 'I': argtable[i++] = (ptroff_t) va_arg(args, jint_promoted); break;
    case 'S': argtable[i++] = (ptroff_t) va_arg(args, jshort_promoted); break;
    case 'Z': argtable[i++] = (ptroff_t) va_arg(args, jboolean_promoted);break;
    case 'F':
      {
	union { jfloat f; ptroff_t p; } u;
	u.f = (jfloat) va_arg(args, jfloat_promoted);
	argtable[i++] = u.p;
      }
      break;
    case 'J': case 'D':
      {
	union{ jlong l; jdouble d; struct { ptroff_t p1; ptroff_t p2; } p; } u;
	if (*sigptr=='J')
	  u.l = (jlong) va_arg(args, jlong_promoted);
	else
	  u.d = (jdouble) va_arg(args, jdouble_promoted);
	argtable[i++] = u.p.p1;
	if (sizeof(ptroff_t) < sizeof(jlong)) /*long and double are same size*/
	  argtable[i++] = u.p.p2;
	assert( (2*sizeof(ptroff_t)) >= sizeof(jlong) );
      }
      break;
    case '[':
      while (*sigptr=='[') sigptr++;
    case 'L':
      if (*sigptr=='L') while (*sigptr!=';') sigptr++;
      argtable[i++] = (ptroff_t) FNI_UNWRAP(va_arg(args, jobject));
      break;
    default: assert(0); /* illegal signature */
    }
  assert(methodID->nargs==i);
}

/* get the ... cases out of the way */
void FNI_CallStaticVoidMethod(JNIEnv *env,
			      jclass clazz, jmethodID methodID, ...)
{
    va_list varargs;
    assert(FNI_NO_EXCEPTIONS(env));
    va_start(varargs, methodID);
    (*env)->CallStaticVoidMethodV(env, clazz, methodID, varargs);
    va_end(varargs);
}
void FNI_CallNonvirtualVoidMethod(JNIEnv *env, jobject obj,
				  jclass clazz, jmethodID methodID, ...)
{
    va_list varargs;
    assert(FNI_NO_EXCEPTIONS(env));
    va_start(varargs, methodID);
    (*env)->CallNonvirtualVoidMethodV(env, obj, clazz, methodID, varargs);
    va_end(varargs);
}
void FNI_CallVoidMethod(JNIEnv *env, jobject obj,
			jmethodID methodID, ...)
{
    va_list varargs;
    assert(FNI_NO_EXCEPTIONS(env));
    va_start(varargs, methodID);
    (*env)->CallVoidMethodV(env, obj, methodID, varargs);
    va_end(varargs);
}
#define FNI_CALL_DOTDOTDOT(name,type) \
type FNI_CallStatic##name##Method(JNIEnv *env, \
				  jclass clazz, jmethodID methodID, ...) \
{ \
    va_list varargs; \
    type result; \
    assert(FNI_NO_EXCEPTIONS(env)); \
    va_start(varargs, methodID); \
    result=FNI_CallStatic##name##MethodV(env, clazz, methodID, varargs); \
    va_end(varargs); \
    return result; \
} \
type FNI_CallNonvirtual##name##Method(JNIEnv *env, jobject obj, \
				      jclass clazz, jmethodID methodID, ...) \
{ \
    va_list varargs; \
    type result; \
    assert(FNI_NO_EXCEPTIONS(env)); \
    va_start(varargs, methodID); \
    result=FNI_CallNonvirtual##name##MethodV(env, obj, clazz, methodID, varargs); \
    va_end(varargs); \
    return result; \
} \
type FNI_Call##name##Method(JNIEnv *env, jobject obj, \
			    jmethodID methodID, ...) \
{ \
    va_list varargs; \
    type result; \
    assert(FNI_NO_EXCEPTIONS(env)); \
    va_start(varargs, methodID); \
    result=FNI_Call##name##MethodV(env, obj, methodID, varargs); \
    va_end(varargs); \
    return result; \
}
FORNONVOIDTYPES(FNI_CALL_DOTDOTDOT);

/* -------- Static methods. ----------- */

void FNI_CallStaticVoidMethodA(JNIEnv *env,
			       jclass clazz, jmethodID methodID,
			       jvalue * args) {
  ptroff_t argtable[methodID->nargs];
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  move_and_unwrapA(methodID, argtable, 0, args);
  FNI_Dispatch_Void(methodID->offset, methodID->nargs, argtable,
		    &exception);
  if (exception!=NULL) FNI_Throw(env, FNI_WRAP(exception));
}
void FNI_CallStaticVoidMethodV(JNIEnv *env,
			       jclass clazz, jmethodID methodID,
			       va_list args) {
  ptroff_t argtable[methodID->nargs];
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  move_and_unwrapV(methodID, argtable, 0, args);
  FNI_Dispatch_Void(methodID->offset, methodID->nargs, argtable,
		    &exception);
  if (exception!=NULL) FNI_Throw(env, FNI_WRAP(exception));
}
jobject FNI_CallStaticObjectMethodA(JNIEnv *env,
				   jclass clazz, jmethodID methodID,
				   jvalue * args) {
  ptroff_t argtable[methodID->nargs];
  jobject_unwrapped result;
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  move_and_unwrapA(methodID, argtable, 0, args);
  result = FNI_Dispatch_Object(methodID->offset, methodID->nargs,
			       argtable, &exception);
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return NULL; }
  return FNI_WRAP(result);
}
jobject FNI_CallStaticObjectMethodV(JNIEnv *env,
				   jclass clazz, jmethodID methodID,
				   va_list args) {
  ptroff_t argtable[methodID->nargs];
  jobject_unwrapped result;
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  move_and_unwrapV(methodID, argtable, 0, args);
  result = FNI_Dispatch_Object(methodID->offset, methodID->nargs,
			       argtable, &exception);
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return NULL; }
  return FNI_WRAP(result);
}
#define FNI_CALL_STATIC(name, type) \
type FNI_CallStatic##name##MethodA(JNIEnv *env, \
				   jclass clazz, jmethodID methodID, \
				   jvalue * args) { \
  ptroff_t argtable[methodID->nargs]; \
  type result; \
  jobject_unwrapped exception = NULL; \
  assert(FNI_NO_EXCEPTIONS(env)); \
  move_and_unwrapA(methodID, argtable, 0, args); \
  result = FNI_Dispatch_##name(methodID->offset, methodID->nargs, \
			       argtable, &exception); \
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return 0; }\
  return result; \
} \
type FNI_CallStatic##name##MethodV(JNIEnv *env, \
				   jclass clazz, jmethodID methodID, \
				   va_list args) { \
  ptroff_t argtable[methodID->nargs]; \
  type result; \
  jobject_unwrapped exception = NULL; \
  assert(FNI_NO_EXCEPTIONS(env)); \
  move_and_unwrapV(methodID, argtable, 0, args); \
  result = FNI_Dispatch_##name(methodID->offset, methodID->nargs, \
			       argtable, &exception); \
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return 0; }\
  return result; \
}
FORPRIMITIVETYPES(FNI_CALL_STATIC)

/* Nonvirtual methods. */

void FNI_CallNonvirtualVoidMethodA(JNIEnv *env, jobject obj,
				   jclass clazz, jmethodID methodID,
				   jvalue * args) {
  ptroff_t argtable[methodID->nargs+1];
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj);
  move_and_unwrapA(methodID, argtable, 1, args);
  FNI_Dispatch_Void(methodID->offset, methodID->nargs, argtable,
		    &exception);
  if (exception!=NULL) FNI_Throw(env, FNI_WRAP(exception));
}
void FNI_CallNonvirtualVoidMethodV(JNIEnv *env, jobject obj,
				   jclass clazz, jmethodID methodID,
				   va_list args) {
  ptroff_t argtable[methodID->nargs+1];
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj);
  move_and_unwrapV(methodID, argtable, 1, args);
  FNI_Dispatch_Void(methodID->offset, methodID->nargs, argtable,
		    &exception);
  if (exception!=NULL) FNI_Throw(env, FNI_WRAP(exception));
}
jobject FNI_CallNonvirtualObjectMethodA(JNIEnv *env, jobject obj,
					jclass clazz, jmethodID methodID,
					jvalue * args) {
  ptroff_t argtable[methodID->nargs+1];
  jobject_unwrapped result;
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj);
  move_and_unwrapA(methodID, argtable, 1, args);
  result = FNI_Dispatch_Object(methodID->offset, methodID->nargs,
			       argtable, &exception);
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return NULL; }
  return FNI_WRAP(result);
}
jobject FNI_CallNonvirtualObjectMethodV(JNIEnv *env, jobject obj,
					jclass clazz, jmethodID methodID,
					va_list args) {
  ptroff_t argtable[methodID->nargs+1];
  jobject_unwrapped result;
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj);
  move_and_unwrapV(methodID, argtable, 1, args);
  result = FNI_Dispatch_Object(methodID->offset, methodID->nargs,
			       argtable, &exception);
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return NULL; }
  return FNI_WRAP(result);
}
#define FNI_CALL_NONVIRTUAL(name, type) \
type FNI_CallNonvirtual##name##MethodA(JNIEnv *env, jobject obj, \
				       jclass clazz, jmethodID methodID, \
				       jvalue * args) { \
  ptroff_t argtable[methodID->nargs+1]; \
  type result; \
  jobject_unwrapped exception = NULL; \
  assert(FNI_NO_EXCEPTIONS(env)); \
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj); \
  move_and_unwrapA(methodID, argtable, 1, args); \
  result = FNI_Dispatch_##name(methodID->offset, methodID->nargs, \
			       argtable, &exception); \
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return 0; }\
  return result; \
} \
type FNI_CallNonvirtual##name##MethodV(JNIEnv *env, jobject obj, \
				       jclass clazz, jmethodID methodID, \
				       va_list args) { \
  ptroff_t argtable[methodID->nargs+1]; \
  type result; \
  jobject_unwrapped exception = NULL; \
  assert(FNI_NO_EXCEPTIONS(env)); \
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj); \
  move_and_unwrapV(methodID, argtable, 1, args); \
  result = FNI_Dispatch_##name(methodID->offset, methodID->nargs, \
			       argtable, &exception); \
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return 0; }\
  return result; \
}
FORPRIMITIVETYPES(FNI_CALL_NONVIRTUAL)

/* Virtual methods. */

#define VIRTUAL(obj, offset) \
     (*((ptroff_t *)(((ptroff_t)FNI_UNWRAP_MASKED(obj)->claz)+(offset))))
void FNI_CallVoidMethodA(JNIEnv *env, jobject obj, jmethodID methodID, 
			 jvalue * args) {
  ptroff_t argtable[methodID->nargs+1];
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj);
  move_and_unwrapA(methodID, argtable, 1, args);
  FNI_Dispatch_Void(VIRTUAL(obj,methodID->offset), methodID->nargs, argtable,
		    &exception);
  if (exception!=NULL) FNI_Throw(env, FNI_WRAP(exception));
}
void FNI_CallVoidMethodV(JNIEnv *env, jobject obj, jmethodID methodID,
			 va_list args) {
  ptroff_t argtable[methodID->nargs+1];
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj);
  move_and_unwrapV(methodID, argtable, 1, args);
  FNI_Dispatch_Void(VIRTUAL(obj,methodID->offset), methodID->nargs, argtable,
		    &exception);
  if (exception!=NULL) FNI_Throw(env, FNI_WRAP(exception));
}
jobject FNI_CallObjectMethodA(JNIEnv *env, jobject obj, jmethodID methodID,
			      jvalue * args) {
  ptroff_t argtable[methodID->nargs+1];
  jobject_unwrapped result;
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj);
  move_and_unwrapA(methodID, argtable, 1, args);
  result = FNI_Dispatch_Object(VIRTUAL(obj,methodID->offset), methodID->nargs,
			       argtable, &exception);
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return NULL; }
  return FNI_WRAP(result);
}
jobject FNI_CallObjectMethodV(JNIEnv *env, jobject obj, jmethodID methodID,
			      va_list args) {
  ptroff_t argtable[methodID->nargs+1];
  jobject_unwrapped result;
  jobject_unwrapped exception = NULL;
  assert(FNI_NO_EXCEPTIONS(env));
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj);
  move_and_unwrapV(methodID, argtable, 1, args);
  result = FNI_Dispatch_Object(VIRTUAL(obj,methodID->offset), methodID->nargs,
			       argtable, &exception);
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return NULL; }
  return FNI_WRAP(result);
}
#define FNI_CALL_VIRTUAL(name, type) \
type FNI_Call##name##MethodA(JNIEnv *env, jobject obj, jmethodID methodID, \
			     jvalue * args) { \
  ptroff_t argtable[methodID->nargs+1]; \
  type result; \
  jobject_unwrapped exception = NULL; \
  assert(FNI_NO_EXCEPTIONS(env)); \
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj); \
  move_and_unwrapA(methodID, argtable, 1, args); \
  result = FNI_Dispatch_##name(VIRTUAL(obj,methodID->offset), methodID->nargs,\
			       argtable, &exception); \
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return 0; }\
  return result; \
} \
type FNI_Call##name##MethodV(JNIEnv *env, jobject obj, jmethodID methodID, \
			     va_list args) { \
  ptroff_t argtable[methodID->nargs+1]; \
  type result; \
  jobject_unwrapped exception = NULL; \
  assert(FNI_NO_EXCEPTIONS(env)); \
  argtable[0] = (ptroff_t) FNI_UNWRAP(obj); \
  move_and_unwrapV(methodID, argtable, 1, args); \
  result = FNI_Dispatch_##name(VIRTUAL(obj,methodID->offset), methodID->nargs,\
			       argtable, &exception); \
  if (exception!=NULL) { FNI_Throw(env, FNI_WRAP(exception)); return 0; }\
  return result; \
}
FORPRIMITIVETYPES(FNI_CALL_VIRTUAL)
