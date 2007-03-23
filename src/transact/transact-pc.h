/** Support functions for preciseC-compiled code */

#ifndef INCLUDED_TRANSACT_PC_H
#define INCLUDED_TRANSACT_PC_H

#if BDW_CONSERVATIVE_GC
#define GC_malloc_atomic GC_malloc_atomic_trans
#endif /* BDW_CONSERVATIVE_GC */

/* ------------------------------------------------------ */
#ifdef DONT_REALLY_DO_TRANSACTIONS
/* testing stubs */
# include "transact/stubs.h"
#else
/* the real deal! */
#if 0 /* hmm, declaration problems. */
# include "transact/readwrite.h"
# include "transact/versions.h"
#else
# include "transact/proto.h"
#endif
#endif

#endif /* INCLUDED_TRANSACT_PC_H */
