/* Stack allocation function. */

#include "config.h"

#include "alloc.h"	/* for declaration of NSTK_malloc */
#include "misc.h"	/* for ALIGN, REALLY_DO_ALLOC, NGBL_malloc_noupdate. */
#include "stats.h"	/* for UPDATE_STATS */
#if !defined(WITH_PRECISE_C_BACKEND)
# include "asm/stack.h"	/* for get_stackptr/set_stackptr */
#else
/* we use alloca directly when compiling w/ precisec; if NSTK_malloc is
 * ever called, we're only trying to get accurate statistics and don't
 * care that we can't actually do the stack allocation. */
# undef REALLY_DO_STK_ALLOC
#endif

/** XXX THIS BREAKS IF THE STACK DOESN'T GROW DOWN */
void *NSTK_malloc(size_t size) {
  register char *result;
  UPDATE_NIFTY_STATS(stk, size);
#ifdef REALLY_DO_STK_ALLOC
  result = get_stackptr()-ALIGN(size);
#if 0
  printf("STACK ALLOCATING %ld bytes from %p (at %p)\n",
	 (long) size, __builtin_return_address(0), result);
#endif
  set_stackptr(result);
  /* stack pointer points to last full location */
#else
  result = NGBL_malloc_noupdate(size);
#endif
  return result;
}

#ifdef WITH_PRECISE_C_BACKEND
/* Update statistics for stack allocation.  The actual allocation
 * in precise-c is done via a call to alloca() one level up. */
void NSTK_update_stats(size_t size) {
  UPDATE_NIFTY_STATS(stk, size);
}
#endif /* WITH_PRECISE_C_BACKEND */
