#include "config.h"
#include "jni.h"
#include "jni-private.h"

#include <assert.h>
#include "transact.h"
#include "flags.h"
#include "objinfo.h"

/* deal with allocation variations */
#ifdef BDW_CONSERVATIVE_GC
# define MALLOC GC_malloc
#else
# define MALLOC malloc
#endif

#include "allproto.h"

#include "allversions.c"
