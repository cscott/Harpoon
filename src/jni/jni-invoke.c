#include <stdarg.h>
#include <assert.h>
#include "jni.h"
#include "jni-private.h"
  
#define FNI_DISPATCH_PROTO(name, type) \
   extern type FNI_Dispatch_##name(ptroff_t method_pointer, int narg_words, \
				   void * argptr, jthrowable * exception) \
   __attribute__ ((/*weak,*/ alias ("FNI_Dispatch")));
FORALLTYPES(FNI_DISPATCH_PROTO);
extern jthrowable FNI_dispatch_exception;

/* OK, wrap & move arguments based on signature. */
static void move_and_wrapA(jmethodID methodID, ptroff_t *argtable,
			   jvalue * args) {
  char *sigptr = methodID->desc+1;
  int i, j;
  for (i=0, j=0; *sigptr != ')'; sigptr++)
    switch (*sigptr) {
    case 'B': argtable[i++] = (ptroff_t) args[j++].b; break;
    case 'C': argtable[i++] = (ptroff_t) args[j++].c; break;
    case 'F': argtable[i++] = (ptroff_t) args[j++].f; break;
    case 'I': argtable[i++] = (ptroff_t) args[j++].i; break;
    case 'S': argtable[i++] = (ptroff_t) args[j++].s; break;
    case 'Z': argtable[i++] = (ptroff_t) args[j++].z; break;
    case 'D': case 'J':
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
      /* XXX: wrap object here. */
      argtable[i++] = (ptroff_t) args[j++].l;
      break;
    default: assert(0); /* illegal signature */
    }
  assert(methodID->nargs==i);
}
static void move_and_wrapV(jmethodID methodID, ptroff_t *argtable,
			   va_list args) {
  char *sigptr = methodID->desc+1;
  int i, j;
  for (i=0, j=0; *sigptr != ')'; sigptr++)
    switch (*sigptr) {
    case 'B': argtable[i++] = (ptroff_t) va_arg(args, jbyte); break;
    case 'C': argtable[i++] = (ptroff_t) va_arg(args, jchar); break;
    case 'F': argtable[i++] = (ptroff_t) va_arg(args, jfloat); break;
    case 'I': argtable[i++] = (ptroff_t) va_arg(args, jint); break;
    case 'S': argtable[i++] = (ptroff_t) va_arg(args, jshort); break;
    case 'Z': argtable[i++] = (ptroff_t) va_arg(args, jboolean); break;
    case 'D': case 'J':
      if (sizeof(ptroff_t) >= sizeof(jlong))
	argtable[i++] = (ptroff_t) va_arg(args, jlong);
      else {
	int i;
	for (i=0; i<sizeof(jlong); i+=sizeof(ptroff_t))
	  argtable[i++] = (ptroff_t) va_arg(args, ptroff_t);
      }
      break;
    case '[':
      while (*sigptr=='[') sigptr++;
    case 'L':
      if (*sigptr=='L') while (*sigptr!=';') sigptr++;
      /* XXX: wrap object here. */
      argtable[i++] = (ptroff_t) va_arg(args, jobject);
      break;
    default: assert(0); /* illegal signature */
    }
  assert(methodID->nargs==i);
}

/* Static methods. */
void FNI_CallStaticVoidMethod(JNIEnv *env,
			      jclass clazz, jmethodID methodID, ...)
{
    va_list varargs;
    va_start(varargs, methodID);
    (*env)->CallStaticVoidMethodV(env, clazz, methodID, varargs);
    va_end(varargs);
}
void FNI_CallStaticVoidMethodA(JNIEnv *env,
			       jclass clazz, jmethodID methodID,
			       jvalue * args) {
  ptroff_t argtable[methodID->nargs];
  jthrowable exception = NULL;
  move_and_wrapA(methodID, argtable, args);
  FNI_Dispatch_Void(methodID->offset, methodID->nargs, argtable,
		    &exception);
  assert(exception==NULL); /* can't handle exceptions yet. */
}
void FNI_CallStaticVoidMethodV(JNIEnv *env,
			       jclass clazz, jmethodID methodID,
			       va_list args) {
  ptroff_t argtable[methodID->nargs];
  jthrowable exception = NULL;
  move_and_wrapV(methodID, argtable, args);
  FNI_Dispatch_Void(methodID->offset, methodID->nargs, argtable,
		    &exception);
  assert(exception==NULL); /* can't handle exceptions yet. */
}
#define FNI_CALL_STATIC(name, type) \
type FNI_CallStatic##name##Method(JNIEnv *env, \
				  jclass clazz, jmethodID methodID, ...) \
{ \
    va_list varargs; \
    type result; \
    va_start(varargs, methodID); \
    result=FNI_CallStatic##name##MethodV(env, clazz, methodID, varargs); \
    va_end(varargs); \
    return result; \
} \
type FNI_CallStatic##name##MethodA(JNIEnv *env, \
				   jclass clazz, jmethodID methodID, \
				   jvalue * args) { \
  ptroff_t argtable[methodID->nargs]; \
  type result; \
  jthrowable exception = NULL; \
  move_and_wrapA(methodID, argtable, args); \
  result = FNI_Dispatch_##name(methodID->offset, methodID->nargs, \
			       argtable, &exception); \
  assert(exception==NULL); \
  return result; \
} \
type FNI_CallStatic##name##MethodV(JNIEnv *env, \
				   jclass clazz, jmethodID methodID, \
				   va_list args) { \
  ptroff_t argtable[methodID->nargs]; \
  type result; \
  jthrowable exception = NULL; \
  move_and_wrapV(methodID, argtable, args); \
  result = FNI_Dispatch_##name(methodID->offset, methodID->nargs, \
			       argtable, &exception); \
  assert(exception==NULL); \
  return result; \
}
FORNONVOIDTYPES(FNI_CALL_STATIC)
