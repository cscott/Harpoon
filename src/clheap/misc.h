/* Miscellaneous configuration. */

#ifndef INCLUDED_NIFTY_MISC_H
#define INCLUDED_NIFTY_MISC_H

#define HEAPSIZE 65536
#define ALIGNMENT 4
#define ALIGN(x) (((x)+(ALIGNMENT-1)) & ~(ALIGNMENT-1))

/* undefine to disable statistics logging */
#define MAKE_STATS
/* #define REALLY_DO_ALLOC */

#endif /* INCLUDED_NIFTY_MISC_H */
