/* Stack allocation function. */

/* we use alloca directly when compiling w/ precisec */
#if !defined(WITH_PRECISE_C_BACKEND)

#include "alloc.h"	/* for declaration of NSTK_malloc */
#include "asm/stack.h"	/* for get_stackptr/set_stackptr */
#include "misc.h"	/* for ALIGN, REALLY_DO_ALLOC, NGBL_malloc_noupdate. */
#include "stats.h"	/* for UPDATE_STATS */

/** XXX THIS BREAKS IF THE STACK DOESN'T GROW DOWN */
void *NSTK_malloc(size_t size) {
  register char *result;
  UPDATE_STATS(stk, size);
#ifdef REALLY_DO_STK_ALLOC
  result = get_stackptr()-ALIGN(size);
#if 0
  printf("STACK ALLOCATING %ld bytes from %p (at %p)\n",
	 (long) size, __builtin_return_address(0), result);
#endif
  set_stackptr(result);
  return result; /* stack pointer points to last full location */
#else
  return NGBL_malloc_noupdate(size);
#endif
}

#endif /* !WITH_PRECISE_C_BACKEND */
