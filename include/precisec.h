#ifndef INCLUDED_PRECISEC_H
#define INCLUDED_PRECISEC_H

#include "jni.h"

typedef void * jptr;
#define SHR(x,y) (((int32_t)(x))>>((y)&0x1f))
#define USHR(x,y) (((u_int32_t)(x))>>((y)&0x1f))
#define LSHR(x,y) (((int64_t)(x))>>((y)&0x3f))
#define LUSHR(x,y) (((u_int64_t)(x))>>((y)&0x3f))

/* select which calling convention you'd like to use in the generated code */
#define USE_GLOBAL_SETJMP

#ifdef USE_PAIR_RETURN /* ----------------------------------------------- */
#define FIRST_DECL_ARG(x)
#define DECLAREFUNC(rettype, funcname, args, segment) \
rettype ## _and_ex funcname args __attribute__ ((section (segment)))
#define DEFINEFUNC(rettype, funcname, args, segment) \
rettype ## _and_ex funcname args
#define DECLAREFUNCV(funcname, args, segment) \
void * funcname args __attribute__ ((section (segment)))
#define DEFINEFUNCV(funcname, args, segment) \
void * funcname args
#define RETURN(rettype, val)	return ((rettype ## _and_ex) { NULL, (val) })
#define RETURNV()		return NULL
#define THROW(rettype, val)	return ((rettype ## _and_ex) { (val), 0 })
#define THROWV(val)		return (val)

#define FIRST_PROTO_ARG(x)
#define FUNCPROTO(rettype, argtypes)\
rettype ## _and_ex (*) argtypes
#define FUNCPROTOV(argtypes)\
void * (*) argtypes

#define FIRST_CALL_ARG(x)
#define CALL(rettype, retval, funcref, args, exv, handler)\
{ rettype ## _and_ex __r = (funcref) args;\
  if (__r.ex) { exv = __r.ex; goto handler; }\
  else retval = __r.value;\
}
#define CALLV(funcref, args, exv, handler)\
{ void * __r = (funcref) args;\
  if (__r) { exv = __r; goto handler; }\
}
/* no-handler case is same as handler case */
#define CALL_NH(rettype, retval, funcref, args, exv, handler)\
CALL(rettype, retval, funcref, args, exv, handler)
#define CALLV_NH(funcref, args, exv, handler)\
CALLV(funcref, args, exv, handler)

/* <foo> and exception pairs */
typedef struct {
  void *ex;
  jdouble value;
} jdouble_and_ex;
typedef struct {
  void *ex;
  jfloat value;
} jfloat_and_ex;
typedef struct {
  void *ex;
  jint value;
} jint_and_ex;
typedef struct {
  void *ex;
  jlong value;
} jlong_and_ex;
typedef struct {
  void *ex;
  jptr value;
} jptr_and_ex;
#endif /* USE_PAIR_RETURN ----------------------------------------- */

#ifdef USE_GLOBAL_SETJMP /* ----------------------------------------------- */
#include "fni-threadstate.h" /* for struct FNI_Thread_State */
#include <setjmp.h>
extern void *memcpy(void *dst, const void *src, size_t n);

#define FIRST_DECL_ARG(x)
#define DECLAREFUNC(rettype, funcname, args, segment) \
rettype funcname args __attribute__ ((section (segment)))
#define DEFINEFUNC(rettype, funcname, args, segment) \
rettype funcname args
#define DECLAREFUNCV(funcname, args, segment) \
void funcname args __attribute__ ((section (segment)))
#define DEFINEFUNCV(funcname, args, segment) \
void funcname args
#define RETURN(rettype, val)	return (val)
#define RETURNV()		return
#define THROW(rettype, val)	THROWV(val)
#define THROWV(val)\
longjmp(((struct FNI_Thread_State *)FNI_GetJNIEnv())->handler, (int)(val))

#define FIRST_PROTO_ARG(x)
#define FUNCPROTO(rettype, argtypes)\
rettype (*) argtypes
#define FUNCPROTOV(argtypes)\
void (*) argtypes

#define FIRST_CALL_ARG(x)
#define SETUP_HANDLER(exv, hlabel)\
{ struct FNI_Thread_State *fts=(struct FNI_Thread_State *)FNI_GetJNIEnv();\
  jptr _ex_;\
  jmp_buf _jb_; memcpy(_jb_, fts->handler, sizeof(_jb_));\
  if ((_ex_=(jptr)setjmp(fts->handler))!=NULL) {\
    exv=_ex_; memcpy(fts->handler, _jb_, sizeof(_jb_)); goto hlabel;\
  }
#define RESTORE_HANDLER\
  memcpy(fts->handler, _jb_, sizeof(_jb_));\
}
/* no-handler versions of calls */
#define CALL_NH(rettype, retval, funcref, args, exv, handler)\
retval = (funcref) args;
#define CALLV_NH(funcref, args, exv, handler)\
(funcref) args

#define CALL(rettype, retval, funcref, args, exv, handler)\
SETUP_HANDLER(exv, handler)\
CALL_NH(rettype, retval, funcref, args, exv, handler);\
RESTORE_HANDLER
#define CALLV(funcref, args, exv, handler)\
SETUP_HANDLER(exv, handler)\
CALLV_NH(funcref, args, exv, handler);\
RESTORE_HANDLER

#endif /* USE_GLOBAL_SETJMP ----------------------------------------- */

#endif /* INCLUDED_PRECISEC_H */
