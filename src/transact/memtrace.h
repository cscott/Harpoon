#ifndef INCLUDED_TRANSACT_MEMTRACE_H
#define INCLUDED_TRANSACT_MEMTRACE_H

/* IN_MEMTRACE_HEADER indicates that we want prototypes and
 * function bodies only for inlined methods. */
#define IN_MEMTRACE_HEADER
#include "transact/memtrace.c"
#undef IN_MEMTRACE_HEADER

#endif /* INCLUDED_TRANSACT_MEMTRACE_H */
