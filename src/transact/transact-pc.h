/** Support functions for preciseC-compiled code */

#ifndef INCLUDED_TRANSACT_PC_H
#define INCLUDED_TRANSACT_PC_H

#if BDW_CONSERVATIVE_GC
#define GC_malloc_atomic GC_malloc_atomic_trans
#endif /* BDW_CONSERVATIVE_GC */

# include "fni-stats.h" /* sometimes we're going to keep statistics */
DECLARE_STATS_EXTERN(transact_readnt)
DECLARE_STATS_EXTERN(transact_writent)
DECLARE_STATS_EXTERN(transact_false_flag_read)
DECLARE_STATS_EXTERN(transact_false_flag_write)
DECLARE_STATS_EXTERN(transact_long_write)

/* ------------------------------------------------------ */
#ifdef DONT_REALLY_DO_TRANSACTIONS
/* testing stubs */
# include "transact/stubs.h"
#else
/* the real deal! */
# include "transact/fastpath.h"
#endif

#endif /* INCLUDED_TRANSACT_PC_H */
