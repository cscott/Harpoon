/* Memory logging routines. */
#if !defined(IN_MEMTRACE_HEADER)
# include "config.h"
# include <jni.h>
# include "jni-private.h"
# include <assert.h>
# include <stdio.h>
# include <stdlib.h>
# include <string.h>
# include "transact/memtrace.h" /* keep us honest */
#endif

#define NO_VALUETYPE
#include "transact/memtrace-impl.c"
#undef NO_VALUETYPE

#define VALUETYPE jboolean
#define VALUENAME Boolean
#include "transact/memtrace-impl.c"
#define ARRAY
#include "transact/memtrace-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jbyte
#define VALUENAME Byte
#include "transact/memtrace-impl.c"
#define ARRAY
#include "transact/memtrace-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jchar
#define VALUENAME Char
#include "transact/memtrace-impl.c"
#define ARRAY
#include "transact/memtrace-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jshort
#define VALUENAME Short
#include "transact/memtrace-impl.c"
#define ARRAY
#include "transact/memtrace-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jint
#define VALUENAME Int
#include "transact/memtrace-impl.c"
#define ARRAY
#include "transact/memtrace-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jlong
#define VALUENAME Long
#include "transact/memtrace-impl.c"
#define ARRAY
#include "transact/memtrace-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jfloat
#define VALUENAME Float
#include "transact/memtrace-impl.c"
#define ARRAY
#include "transact/memtrace-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jdouble
#define VALUENAME Double
#include "transact/memtrace-impl.c"
#define ARRAY
#include "transact/memtrace-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE struct oobj *
#define VALUENAME Object
#include "transact/memtrace-impl.c"
#define ARRAY
#include "transact/memtrace-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE
