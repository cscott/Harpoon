/* multi-granular SC/LL */
#ifndef INCLUDED_TRANSACT_ATOMIC_H
#define INCLUDED_TRANSACT_ATOMIC_H

#define VALUETYPE jboolean
#define VALUENAME Boolean
#include "transact/atomic-impl.h"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jbyte
#define VALUENAME Byte
#include "transact/atomic-impl.h"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jchar
#define VALUENAME Char
#include "transact/atomic-impl.h"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jshort
#define VALUENAME Short
#include "transact/atomic-impl.h"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jint
#define VALUENAME Int
#include "transact/atomic-impl.h"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jlong
#define VALUENAME Long
#include "transact/atomic-impl.h"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jfloat
#define VALUENAME Float
#include "transact/atomic-impl.h"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jdouble
#define VALUENAME Double
#include "transact/atomic-impl.h"
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE struct oobj *
#define VALUENAME Object
#include "transact/atomic-impl.h"
#undef VALUENAME
#undef VALUETYPE

#endif /* INCLUDED_TRANSACT_ATOMIC_H */
