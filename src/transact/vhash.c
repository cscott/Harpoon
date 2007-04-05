/* Read and Write functions for Transaction and Non-Transaction contexts */
#if !defined(IN_HEADER)
# include "config.h"
# include "jni.h"
# include <assert.h>
# include <string.h> /* for memset */
# include "compiler.h" /* for likely/unlikely */
# include "asm/llsc.h" /* for LL/SC */
# include "transact/transact.h" /* for DO_HASH */
# include "transact/flags.h"
# include "transact/vhash.h"
#endif

// int needs to be defined first, since others reference it.
#define VALUETYPE jint
#define VALUENAME Int
#include "transact/vhash-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jboolean
#define VALUENAME Boolean
#include "transact/vhash-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jbyte
#define VALUENAME Byte
#include "transact/vhash-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jchar
#define VALUENAME Char
#include "transact/vhash-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jshort
#define VALUENAME Short
#include "transact/vhash-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jlong
#define VALUENAME Long
#include "transact/vhash-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jfloat
#define VALUENAME Float
#include "transact/vhash-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jdouble
#define VALUENAME Double
#include "transact/vhash-impl.c"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE struct oobj *
#define VALUENAME Object
#include "transact/vhash-impl.c"
#undef VALUENAME
#undef VALUETYPE
