/* JNI transactions support functions */
#if !defined(IN_TRANSJNI_HEADER)
# include "config.h"
# include "jni.h"
# include "jni-private.h"
# include "transact/transact.h"
# ifdef DONT_REALLY_DO_TRANSACTIONS
#  include "transact/stubs.h"
# else
#  include "transact/fastpath.h"
# endif
#endif

/* Helper 'macros' */
static inline struct commitrec *currTrans(JNIEnv *env) {
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  return fts->current_transaction;
}
static inline struct commitrec *setCurrTrans(JNIEnv *env, struct commitrec *cr)
{
  struct FNI_Thread_State *fts = (struct FNI_Thread_State *) env;
  struct commitrec *oldcr = fts->current_transaction;
  fts->current_transaction = cr;
  return oldcr;
}

#define NO_VALUETYPE
#include "transact/transjni-impl.c"
#undef NO_VALUETYPE

#define VALUETYPE jboolean
#define VALUENAME Boolean
#include "transact/transjni-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jbyte
#define VALUENAME Byte
#include "transact/transjni-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jchar
#define VALUENAME Char
#include "transact/transjni-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jshort
#define VALUENAME Short
#include "transact/transjni-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jint
#define VALUENAME Int
#include "transact/transjni-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jlong
#define VALUENAME Long
#include "transact/transjni-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jfloat
#define VALUENAME Float
#include "transact/transjni-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jdouble
#define VALUENAME Double
#include "transact/transjni-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE struct oobj *
#define VALUENAME Object
#include "transact/transjni-impl.c"
#undef VALUENAME
#undef VALUETYPE
