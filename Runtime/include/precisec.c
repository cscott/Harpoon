#include <assert.h>
#include "precisec.h"
#include <jni-private.h>
/* XXX: we only handle up to 15 arguments. */
#define REPEAT0(x) 
#define REPEAT1(x) x(0)
#define REPEAT2(x) REPEAT1(x), x(1)
#define REPEAT3(x) REPEAT2(x), x(2)
#define REPEAT4(x) REPEAT3(x), x(3)
#define REPEAT5(x) REPEAT4(x), x(4)
#define REPEAT6(x) REPEAT5(x), x(5)
#define REPEAT7(x) REPEAT6(x), x(6)
#define REPEAT8(x) REPEAT7(x), x(7)
#define REPEAT9(x) REPEAT8(x), x(8)
#define REPEAT10(x) REPEAT9(x), x(9)
#define REPEAT11(x) REPEAT10(x), x(10)
#define REPEAT12(x) REPEAT11(x), x(11)
#define REPEAT13(x) REPEAT12(x), x(12)
#define REPEAT14(x) REPEAT13(x), x(13)
#define REPEAT15(x) REPEAT14(x), x(14)

#define ARG(x) argptr[x]
#define JPTR(x) jptr

#ifdef USE_PAIR_RETURN /* --------------------------------------------- */
/* FNI_Dispatch_<foo> definitions for PAIR_RETURN */
void FNI_Dispatch_Void(ptroff_t method_pointer, int narg_words,
		       void *_argptr, jobject_unwrapped *exception) {
    register jptr *argptr = (jptr *) _argptr;
    switch (narg_words) {
#define CASE(x) \
case x: \
*exception=((jptr(*)(REPEAT##x(JPTR)))method_pointer)(REPEAT##x(ARG)); break;
	CASE( 0) CASE( 1) CASE( 2) CASE( 3) CASE( 4) CASE( 5) CASE( 6) CASE( 7)
	CASE( 8) CASE( 9) CASE(10) CASE(11) CASE(12) CASE(13) CASE(14) CASE(15)
#undef CASE
    default: assert(0); break;
    }
    return;
}
#define CASE(ctype,x) \
case x: \
_r=((ctype##_and_ex(*)(REPEAT##x(JPTR)))method_pointer)(REPEAT##x(ARG)); break;
#define FNI_DISPATCH(Type, ctype, rettype)\
rettype FNI_Dispatch_##Type(ptroff_t method_pointer, int narg_words,\
			    void *_argptr, jobject_unwrapped *exception) {\
    register jptr *argptr = (jptr *) _argptr;\
    ctype##_and_ex _r;\
    switch (narg_words) {\
	CASE(ctype, 0) CASE(ctype, 1) CASE(ctype, 2) CASE(ctype, 3)\
        CASE(ctype, 4) CASE(ctype, 5) CASE(ctype, 6) CASE(ctype, 7)\
	CASE(ctype, 8) CASE(ctype, 9) CASE(ctype,10) CASE(ctype,11)\
        CASE(ctype,12) CASE(ctype,13) CASE(ctype,14) CASE(ctype,15)\
    default: assert(0); break;\
    }\
    if (_r.ex) *exception = _r.ex;\
    return (rettype) _r.value;\
}
#endif /* USE_PAIR_RETURN ------------------------------------------- */

#ifdef USE_GLOBAL_SETJMP /* --------------------------------------------- */
/* FNI_Dispatch_<foo> definitions for GLOBAL_SETJMP */
void FNI_Dispatch_Void(ptroff_t method_pointer, int narg_words,
		       void *_argptr, jobject_unwrapped *exception) {
    register jptr *argptr = (jptr *) _argptr;
    JNIEnv *env = FNI_GetJNIEnv(); /* for FNI_WRAP */
    struct FNI_Thread_State *fts = (struct FNI_Thread_State *)env;
    jobject_unwrapped ex;
    jmp_buf jb, *oldhandler;
    oldhandler=fts->handler; fts->handler=&jb;
    if ((ex=(jobject_unwrapped)setjmp(jb))!=NULL)
      fts->exception = FNI_WRAP(ex);
    else switch (narg_words) {
#define CASE(x) \
case x: \
((void(*)(REPEAT##x(JPTR)))method_pointer)(REPEAT##x(ARG)); break;
	CASE( 0) CASE( 1) CASE( 2) CASE( 3) CASE( 4) CASE( 5) CASE( 6) CASE( 7)
	CASE( 8) CASE( 9) CASE(10) CASE(11) CASE(12) CASE(13) CASE(14) CASE(15)
#undef CASE
    default: assert(0); break;
    }
    fts->handler=oldhandler; /* restore handler */
    return;
}
#define CASE(ctype,x) \
case x: \
_r = ((ctype(*)(REPEAT##x(JPTR)))method_pointer)(REPEAT##x(ARG)); break;
#define FNI_DISPATCH(Type, ctype, rtype)\
rtype FNI_Dispatch_##Type(ptroff_t method_pointer, int narg_words,\
			    void *_argptr, jobject_unwrapped *exception) {\
    register jptr *argptr = (jptr *) _argptr;\
    JNIEnv *env = FNI_GetJNIEnv(); /* for FNI_WRAP */\
    struct FNI_Thread_State *fts = (struct FNI_Thread_State *)env;\
    jobject_unwrapped ex;\
    ctype _r = 0;\
    jmp_buf jb, *oldhandler;\
    oldhandler=fts->handler; fts->handler=&jb;\
    if ((ex=(jobject_unwrapped)setjmp(jb))!=NULL)\
      fts->exception = FNI_WRAP(ex);\
    else switch (narg_words) {\
        CASE(ctype, 0) CASE(ctype, 1) CASE(ctype, 2) CASE(ctype, 3)\
        CASE(ctype, 4) CASE(ctype, 5) CASE(ctype, 6) CASE(ctype, 7)\
        CASE(ctype, 8) CASE(ctype, 9) CASE(ctype,10) CASE(ctype,11)\
        CASE(ctype,12) CASE(ctype,13) CASE(ctype,14) CASE(ctype,15)\
    default: assert(0); break;\
    }\
    fts->handler=oldhandler; /* restore handler */\
    return (rtype) _r;\
}
#endif /* USE_GLOBAL_SETJMP ------------------------------------------- */

FNI_DISPATCH(Object,  jptr,    jobject_unwrapped)
FNI_DISPATCH(Boolean, jint,    jboolean)
FNI_DISPATCH(Byte,    jint,    jbyte)
FNI_DISPATCH(Char,    jint,    jchar)
FNI_DISPATCH(Short,   jint,    jshort)
FNI_DISPATCH(Int,     jint,    jint)
FNI_DISPATCH(Long,    jlong,   jlong)
FNI_DISPATCH(Float,   jfloat,  jfloat)
FNI_DISPATCH(Double,  jdouble, jdouble)
#undef FNI_DISPATCH
#undef CASE
