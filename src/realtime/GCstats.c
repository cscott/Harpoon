#include "GCstats.h"

#define MALLOC_STATS(name, name_str) \
void* name##_stats(size_t size_in_bytes) { \
  struct timeval begin, end; \
  void* result; \
  gettimeofday(&begin, NULL); \
  result = (void*)name(size_in_bytes); \
  gettimeofday(&end, NULL); \
  printf("%s_stats: %ds %dus\n", name_str, \
	 end.tv_sec-begin.tv_sec, end.tv_usec-begin.tv_usec); \
  return result; \
} 

#define FREE_STATS(name, name_str) \
void name##_stats(void* object_addr) { \
  struct timeval begin, end; \
  gettimeofday(&begin, NULL); \
  name(object_addr); \
  gettimeofday(&end, NULL); \
  printf("%s_stats: %ds %dus\n", name_str, \
	 end.tv_sec-begin.tv_sec, end.tv_usec-begin.tv_usec); \
}

MALLOC_STATS(GC_malloc, "GC_malloc");
MALLOC_STATS(GC_malloc_atomic, "GC_malloc_atomic");
MALLOC_STATS(GC_malloc_uncollectable, "GC_malloc_uncollectable");
MALLOC_STATS(precise_malloc, "precise_malloc");
MALLOC_STATS(malloc, "malloc");

FREE_STATS(GC_free, "GC_free");
FREE_STATS(free, "free");

#undef MALLOC_STATS
#undef FREE_STATS
