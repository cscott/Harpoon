/** Prototypes for the various transactions-related functions. */
#ifndef INCLUDED_TRANSACT_PROTO_H
#define INCLUDED_TRANSACT_PROTO_H

#define NO_VALUETYPE
#include "transact/proto-impl.h"
#undef NO_VALUETYPE

#define VALUETYPE jboolean
#define VALUENAME Boolean
#include "transact/proto-impl.h"
#define ARRAY
#include "transact/proto-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jbyte
#define VALUENAME Byte
#include "transact/proto-impl.h"
#define ARRAY
#include "transact/proto-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jchar
#define VALUENAME Char
#include "transact/proto-impl.h"
#define ARRAY
#include "transact/proto-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jshort
#define VALUENAME Short
#include "transact/proto-impl.h"
#define ARRAY
#include "transact/proto-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jint
#define VALUENAME Int
#include "transact/proto-impl.h"
#define ARRAY
#include "transact/proto-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jlong
#define VALUENAME Long
#include "transact/proto-impl.h"
#define ARRAY
#include "transact/proto-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jfloat
#define VALUENAME Float
#include "transact/proto-impl.h"
#define ARRAY
#include "transact/proto-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jdouble
#define VALUENAME Double
#include "transact/proto-impl.h"
#define ARRAY
#include "transact/proto-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE struct oobj *
#define VALUENAME Object
#include "transact/proto-impl.h"
#define ARRAY
#include "transact/proto-impl.h"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#endif /* INCLUDED_TRANSACT_PROTO_H */
