#ifndef __GCstats_H__
#define __GCstats_H__

#define __need_timeval
#include <bits/time.h>
#include <time.h>
#include "config.h"
#include "gc.h"

void* malloc_stats(size_t size_in_bytes);
void free_stats(void* object_addr);

#ifdef BDW_CONSERVATIVE_GC
void* GC_malloc_stats(size_t size_in_bytes);
void* GC_malloc_atomic_stats(size_t size_in_bytes);
void* GC_malloc_uncollectable_stats(size_t size_in_bytes);
void GC_free_stats(void* object_addr);
#endif

#ifdef WITH_PRECISE_GC
void* precise_malloc_stats(size_t size_in_bytes);
#endif

#endif
