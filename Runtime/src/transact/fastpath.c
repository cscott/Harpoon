/* Fast-path implementations of read and write functions, intended to be
 * inlined. */
#if !defined(IN_FASTPATH_HEADER)
# include "config.h"
# include <jni.h>
# include "transact/transact.h"
# include "transact/proto.h" /* keep us honest */
#else
// just enough definitions to get by; too many and we'll fight with precisec
# include "transact/transact-config.h"
# include "transact/transact-ty.h"
#endif
#include "transact/flags.h"
#include "transact/objinfo.h"
#include "fni-stats.h" /* sometimes we keep statistics */
#include "fni-objsize.h" /* FLEX statistics sometimes need object size */
#include "transact/atomic.h"

DECLARE_STATS_EXTERN(transact_readnt)
DECLARE_STATS_EXTERN(transact_writent)
DECLARE_STATS_EXTERN(transact_false_flag_read)
DECLARE_STATS_EXTERN(transact_false_flag_write)
DECLARE_STATS_EXTERN(transact_long_write)

#if !defined(DONT_REALLY_DO_TRANSACTIONS)

#define NO_VALUETYPE
#include "transact/fastpath-impl.c"
#undef NO_VALUETYPE

#define VALUETYPE jboolean
#define VALUENAME Boolean
#include "transact/fastpath-impl.c"
#define ARRAY
#include "transact/fastpath-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jbyte
#define VALUENAME Byte
#include "transact/fastpath-impl.c"
#define ARRAY
#include "transact/fastpath-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jchar
#define VALUENAME Char
#include "transact/fastpath-impl.c"
#define ARRAY
#include "transact/fastpath-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jshort
#define VALUENAME Short
#include "transact/fastpath-impl.c"
#define ARRAY
#include "transact/fastpath-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jint
#define VALUENAME Int
#include "transact/fastpath-impl.c"
#define ARRAY
#include "transact/fastpath-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jlong
#define VALUENAME Long
#include "transact/fastpath-impl.c"
#define ARRAY
#include "transact/fastpath-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jfloat
#define VALUENAME Float
#include "transact/fastpath-impl.c"
#define ARRAY
#include "transact/fastpath-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jdouble
#define VALUENAME Double
#include "transact/fastpath-impl.c"
#define ARRAY
#include "transact/fastpath-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE struct oobj *
#define VALUENAME Object
#include "transact/fastpath-impl.c"
#define ARRAY
#include "transact/fastpath-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#endif /* !DONT_REALLY_DO_TRANSACTIONS */
