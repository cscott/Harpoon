/* multi-granular SC/LL */

#include "transact/preproc.h" /* Defines 'T()' and 'TA'() macros. */
#include "asm/llsc.h" /* defines LL/SC and friends */

/////////////////////////////////////////////////////////////////////
// hacked multi-granular SC.
// XXX  this gets *nasty* when value size
// is not word or doubleword!  In particular, we can inadventently
// corrupt adjacent subword fields if they are being concurrently
// written.
// XXX we'll assume this doesn't matter.  The "correct" solution is
// to put the read flags for the subword field *adjacent* to the
// field (i.e. one word may contain one byte of read flags and
// three bytes of fields -- say, three byte-sized fields).
// for arrays, this would mean an expansion of 25% for byte arrays
// (okay) and 50% for short arrays (err, the same we'd get if we
// just allocated one word for every element).  This is a lot
// of work unless we can show it's worth it.
static inline int T(store_conditional)(void *base, unsigned offset,
				       VALUETYPE value) {
  return __builtin_choose_expr
    (sizeof(VALUETYPE) < sizeof(jint),
     ({
       int32_t *ptr = (int32_t*)(base+(offset&~3));
       int32_t mask = __builtin_choose_expr(sizeof(VALUETYPE)==1,0xFF,0xFFFF);
       int shift = (offset & 3) << 3;
#ifdef WORDS_BIGENDIAN
       shift = __builtin_choose_expr(sizeof(VALUETYPE)==1,24,16) - shift;
#endif
       int32_t nval = ((((int32_t)value) & mask) << shift) |
	 ( (*ptr) & ~(mask << shift) );
       SC(ptr, nval);
     }),
     __builtin_choose_expr
     (sizeof(VALUETYPE) == sizeof(jint),
      ({
	jint nval = __builtin_choose_expr
	  (__builtin_types_compatible_p(VALUETYPE,jint), value,
	   ({ union { jint i; VALUETYPE v; } u={ .v=value }; u.i; }));
	SC((jint*)(base + offset), nval);
      }),
      ({
	jlong nval = __builtin_choose_expr
	  (__builtin_types_compatible_p(VALUETYPE,jlong), value,
	   ({ union { jlong j; VALUETYPE v; } u={ .v=value }; u.j; }));
	SC_D((jlong*)(base + offset), nval);
      })
      ));
}
// not-so-hacked multi-granular LL
static inline VALUETYPE T(load_linked)(void *base, unsigned offset) {
  return __builtin_choose_expr
    (sizeof(VALUETYPE) < sizeof(jint),
     ({
       int32_t *ptr = (int32_t*)(base+(offset&~3));
       int32_t mask = __builtin_choose_expr(sizeof(VALUETYPE)==1,0xFF,0xFFFF);
       int shift = (offset & 3) << 3;
#ifdef WORDS_BIGENDIAN
       shift = __builtin_choose_expr(sizeof(VALUETYPE)==1,24,16) - shift;
#endif
       (VALUETYPE) (mask & (LL(ptr) >> shift));
     }),
     __builtin_choose_expr
     (sizeof(VALUETYPE) == sizeof(jint),
      ({ jint value = load_linked((jint*)(base+offset));
         __builtin_choose_expr
	 (__builtin_types_compatible_p(VALUETYPE,jint), (VALUETYPE) value,
	  ({ union { jint i; VALUETYPE v; } u = { .i=value }; u.v; }));
      }),
      ({ jlong value = load_linked((jlong*)(base+offset));
         __builtin_choose_expr
	 (__builtin_types_compatible_p(VALUETYPE,jlong), (VALUETYPE) value,
	  ({ union { jlong j; VALUETYPE v; } u = { .j=value }; u.v; }));
      })));
}

/* clean up after ourselves */
#include "transact/preproc.h"
