/* Miscellaneous configuration. */

#ifndef INCLUDED_NIFTY_MISC_H
#define INCLUDED_NIFTY_MISC_H

#define POOLSIZE 1 /* how many threads share a heap */
#define HEAPSIZE 65536
#define ALIGNMENT 4
#define ALIGN(x) (((x)+(ALIGNMENT-1)) & ~(ALIGNMENT-1))

/* undefine to disable statistics logging */
#define MAKE_STATS
#define REALLY_DO_ALLOC

/* do allocation without updating statistics: this is used
 * by various allocators when REALLY_DO_ALLOC is not defined. */
#include <stdlib.h>	/* for size_t */
void *NGBL_malloc_noupdate(size_t size);

#endif /* INCLUDED_NIFTY_MISC_H */
