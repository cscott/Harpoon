/* Miscellaneous configuration. */

#ifndef INCLUDED_NIFTY_MISC_H
#define INCLUDED_NIFTY_MISC_H

#define POOLSIZE 1 /* how many threads share a heap */
#define HEAPSIZE (80*1024*POOLSIZE)
#define ALIGNMENT 4
#define ALIGN(x) (((x)+(ALIGNMENT-1)) & ~(ALIGNMENT-1))

/* undefine to replace the fancy allocation types with simple malloc */
#define REALLY_DO_STK_ALLOC
#define REALLY_DO_THR_ALLOC
/* undefine to explicitly malloc() and free() each thread heap */
#define RECYCLE_HEAPS

/* do allocation without updating statistics: this is used
 * by various allocators when REALLY_DO_ALLOC is not defined. */
#include <stdlib.h>	/* for size_t */
void *NGBL_malloc_noupdate(size_t size);

#endif /* INCLUDED_NIFTY_MISC_H */
