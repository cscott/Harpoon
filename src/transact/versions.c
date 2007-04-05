/** Additional transaction version routines */
#if !defined(IN_VERSIONS_HEADER)
# include "config.h"
# include "jni.h"
# include "jni-private.h"
# include <assert.h>
# include "transact/proto.h" /* keep us honest */
# include "transact/atomic.h"
# include "transact/versions.h"
# include "transact/vhash.h"
# include <string.h> /* for memset */
#endif
#include "transact/transact.h"
#include "transact/objinfo.h" /* OBJ_READERS_PTR/OBJ_VERSION_PTR */
#include "transact/readwrite.h"
#include "transact/versions.h"

/* deal with allocation variations */
#ifdef BDW_CONSERVATIVE_GC
GC_PTR GC_malloc_atomic_trans(size_t size);
# define MALLOC GC_malloc
# define MALLOC_ATOMIC GC_malloc_atomic_trans
#else
void *malloc(size_t size);
# define MALLOC malloc
# define MALLOC_ATOMIC malloc
#endif

#define NO_VALUETYPE
#include "transact/versions-impl.c"
#undef NO_VALUETYPE

#define VALUETYPE jboolean
#define VALUENAME Boolean
#include "transact/versions-impl.c"
#define ARRAY
#include "transact/versions-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jbyte
#define VALUENAME Byte
#include "transact/versions-impl.c"
#define ARRAY
#include "transact/versions-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jchar
#define VALUENAME Char
#include "transact/versions-impl.c"
#define ARRAY
#include "transact/versions-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jshort
#define VALUENAME Short
#include "transact/versions-impl.c"
#define ARRAY
#include "transact/versions-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jint
#define VALUENAME Int
#include "transact/versions-impl.c"
#define ARRAY
#include "transact/versions-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jlong
#define VALUENAME Long
#include "transact/versions-impl.c"
#define ARRAY
#include "transact/versions-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jfloat
#define VALUENAME Float
#include "transact/versions-impl.c"
#define ARRAY
#include "transact/versions-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define VALUETYPE jdouble
#define VALUENAME Double
#include "transact/versions-impl.c"
#define ARRAY
#include "transact/versions-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE

#define NONPRIMITIVE
#define VALUETYPE struct oobj *
#define VALUENAME Object
#include "transact/versions-impl.c"
#define ARRAY
#include "transact/versions-impl.c"
#undef ARRAY
#undef VALUENAME
#undef VALUETYPE
#undef NONPRIMITIVE
