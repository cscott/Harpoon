#ifndef __GCstats_H__
#define __GCstats_H__

#define __need_timeval
#include <bits/time.h>
#include <time.h>
#include "gc.h"

void* GC_malloc_stats(size_t size_in_bytes);
void* GC_malloc_atomic_stats(size_t size_in_bytes);
void* GC_malloc_uncollectable_stats(size_t size_in_bytes);
void GC_free_stats(void* object_addr);

#endif
