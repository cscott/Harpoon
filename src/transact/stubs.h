/** Testing prototypes for the various transactions-related functions. */
#ifndef INCLUDED_TRANSACT_STUBS_H
#define INCLUDED_TRANSACT_STUBS_H

#include <assert.h>
#include "transact/proto.h" /* double-check */
#ifdef STUB_LLSC
# include "transact/atomic.h"
#endif

struct commitrec;

#define NO_VALUETYPE
#include "transact/stubs-impl.h"
#undef NO_VALUETYPE

#define VALUETYPE jboolean
#define VALUENAME Boolean
#include "transact/stubs-impl.h"
#define ARRAY
#include "transact/stubs-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jbyte
#define VALUENAME Byte
#include "transact/stubs-impl.h"
#define ARRAY
#include "transact/stubs-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jchar
#define VALUENAME Char
#include "transact/stubs-impl.h"
#define ARRAY
#include "transact/stubs-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jshort
#define VALUENAME Short
#include "transact/stubs-impl.h"
#define ARRAY
#include "transact/stubs-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jint
#define VALUENAME Int
#include "transact/stubs-impl.h"
#define ARRAY
#include "transact/stubs-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jlong
#define VALUENAME Long
#include "transact/stubs-impl.h"
#define ARRAY
#include "transact/stubs-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jfloat
#define VALUENAME Float
#include "transact/stubs-impl.h"
#define ARRAY
#include "transact/stubs-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jdouble
#define VALUENAME Double
#include "transact/stubs-impl.h"
#define ARRAY
#include "transact/stubs-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE struct oobj *
#define VALUENAME Object
#include "transact/stubs-impl.h"
#define ARRAY
#include "transact/stubs-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#endif /* INCLUDED_TRANSACT_STUBS_H */
