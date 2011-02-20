/* Read and Write functions for Transaction and Non-Transaction contexts */
#ifndef INCLUDED_TRANSACT_READWRITE_H
#define INCLUDED_TRANSACT_READWRITE_H

/* IN_READWRITE_HEADER indicates that we want prototypes,
 * and function bodies only for inlined methods. */
#define IN_READWRITE_HEADER
#include "transact/readwrite.c"
#undef IN_READWRITE_HEADER

#endif /* INCLUDED_TRANSACT_READWRITE_H */
