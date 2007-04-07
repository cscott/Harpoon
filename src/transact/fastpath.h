/* Fast-path implementations of read and write functions, intended to be
 * inlined. */
#ifndef INCLUDED_TRANSACT_FASTPATH_H
#define INCLUDED_TRANSACT_FASTPATH_H

/* IN_FASTPATH_HEADER indicates that we want 'extern inline'
 * versions of methods. */
#define IN_FASTPATH_HEADER
#include "transact/fastpath.c"
#undef IN_FASTPATH_HEADER

#endif /* INCLUDED_TRANSACT_FASTPATH_H */
