/* Stack allocation function. */

#include "alloc.h"	/* for declaration of NSTK_malloc */
#include "asm/stack.h"	/* for get_stackptr/set_stackptr */
#include "misc.h"	/* for ALIGN */
#include "stats.h"	/* for UPDATE_STATS */

/** XXX THIS BREAKS IF THE STACK DOESN'T GROW DOWN */
void *NSTK_malloc(size_t size) {
  register char *result;
  UPDATE_STATS(stk, size);
  result = get_stackptr()-ALIGN(size);
  set_stackptr(result);
  return result; /* stack pointer points to last full location */
}
