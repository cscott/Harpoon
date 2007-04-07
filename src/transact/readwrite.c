/* Read and Write functions for Transaction and Non-Transaction contexts */
#if !defined(IN_READWRITE_HEADER)
# include "config.h"
# include "jni.h"
# include "jni-private.h"
# include "fni-stats.h" /* sometimes we're going to keep statistics */
DECLARE_STATS_EXTERN(transact_readnt)
DECLARE_STATS_EXTERN(transact_writent)
DECLARE_STATS_EXTERN(transact_false_flag_read)
DECLARE_STATS_EXTERN(transact_false_flag_write)
DECLARE_STATS_EXTERN(transact_long_write)

# include "transact/atomic.h"
# include "transact/versions.h"
# include "transact/vhash.h"
# include "transact/proto.h" /* keep us honest */
# include "transact/fastpath.h" // fast path protos
#endif
#include "transact/transact.h"
#include "transact/flags.h" /* XXX should go in asm? */

#define VALUETYPE jboolean
#define VALUENAME Boolean
#include "transact/readwrite-impl.c"
#define ARRAY
#include "transact/readwrite-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jbyte
#define VALUENAME Byte
#include "transact/readwrite-impl.c"
#define ARRAY
#include "transact/readwrite-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jchar
#define VALUENAME Char
#include "transact/readwrite-impl.c"
#define ARRAY
#include "transact/readwrite-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jshort
#define VALUENAME Short
#include "transact/readwrite-impl.c"
#define ARRAY
#include "transact/readwrite-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jint
#define VALUENAME Int
#include "transact/readwrite-impl.c"
#define ARRAY
#include "transact/readwrite-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jlong
#define VALUENAME Long
#include "transact/readwrite-impl.c"
#define ARRAY
#include "transact/readwrite-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jfloat
#define VALUENAME Float
#include "transact/readwrite-impl.c"
#define ARRAY
#include "transact/readwrite-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jdouble
#define VALUENAME Double
#include "transact/readwrite-impl.c"
#define ARRAY
#include "transact/readwrite-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE struct oobj *
#define VALUENAME Object
#include "transact/readwrite-impl.c"
#define ARRAY
#include "transact/readwrite-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE
