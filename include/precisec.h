#ifndef INCLUDED_PRECISEC_H
#define INCLUDED_PRECISEC_H

#include "jni.h"

typedef void * jptr;

#define USE_PAIR_RETURN

#ifdef USE_PAIR_RETURN
#define FIRST_DECL_ARG(x)
#define DECLAREFUNC(rettype, funcname, args, segment) \
rettype ## _and_ex funcname args __attribute__ ((section (segment)))
#define DEFINEFUNC(rettype, funcname, args, segment) \
rettype ## _and_ex funcname args
#define DECLAREFUNCV(funcname, args, segment) \
void * funcname args __attribute__ ((section (segment)))
#define DEFINEFUNCV(funcname, args, segment) \
void * funcname args
#define RETURN(rettype, val)	return ((rettype ## _and_ex) { (val), NULL })
#define RETURNV()		return NULL
#define THROW(rettype, val)	return ((rettype ## _and_ex) { 0, (val)})
#define THROWV(val)		return (val)

#define FIRST_PROTO_ARG(x)
#define FUNCPROTO(rettype, argtypes)\
rettype ## _and_ex (*) argtypes
#define FUNCPROTOV(argtypes)\
void * (*) argtypes

#define FIRST_CALL_ARG(x)
#define CALL(rettype, retval, funcref, args, ex, handler)\
{ rettype ## _and_ex __r = (funcref) args;\
  if (__r.ex) { ex = __r.ex; goto handler; }\
  else retval = __r.value;\
}
#define CALLV(funcref, args, ex, handler)\
{ void * __r = (funcref) args;\
  if (__r) { ex = __r; goto handler; }\
}

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
#endif

#endif /* INCLUDED_PRECISEC_H */
