#include "GCstats.h"

static struct timespec total_GC_time;
static struct timespec max_GC_time;

#define MALLOC_STATS_VARARG(name, args, arg_call) \
void* name##_stats##args { \
  struct timespec begin, end, elapsed; \
  void* result; \
  clock_gettime(CLOCK, &begin); \
  result = (void*)name##arg_call; \
  clock_gettime(CLOCK, &end); \
  elapsed.tv_sec = (end.tv_sec-begin.tv_sec)+((end.tv_nsec<begin.tv_nsec)?(-1):0); \
  elapsed.tv_nsec = (end.tv_nsec-begin.tv_nsec)+((end.tv_nsec<begin.tv_nsec)?1000000000:0); \
  total_GC_time.tv_sec += elapsed.tv_sec + (total_GC_time.tv_nsec + elapsed.tv_nsec)/1000000000; \
  total_GC_time.tv_nsec = (total_GC_time.tv_nsec + elapsed.tv_nsec)%1000000000; \
  if (elapsed.tv_sec > max_GC_time.tv_sec) { \
    max_GC_time.tv_sec = elapsed.tv_sec; \
    max_GC_time.tv_nsec = elapsed.tv_nsec; \
  } \
  if ((elapsed.tv_sec == max_GC_time.tv_sec)&&(elapsed.tv_nsec > max_GC_time.tv_nsec)) { \
    max_GC_time.tv_nsec = elapsed.tv_nsec; \
  } \
  return result; \
}

#define MALLOC_STATS(name) MALLOC_STATS_VARARG(name, (size_t size), (size))

#define FREE_STATS(name) \
void name##_stats(void* object_addr) { \
  struct timespec begin, end, elapsed; \
  clock_gettime(CLOCK, &begin); \
  name(object_addr); \
  clock_gettime(CLOCK, &end); \
  elapsed.tv_sec = (end.tv_sec-begin.tv_sec)+((end.tv_nsec<begin.tv_nsec)?(-1):0); \
  elapsed.tv_nsec = (end.tv_nsec-begin.tv_nsec)+((end.tv_nsec<begin.tv_nsec)?1000000000:0); \
}

void setup_GC_stats() {
  memset(&total_GC_time, 0, sizeof(total_GC_time));
  memset(&max_GC_time, 0, sizeof(max_GC_time));
#ifdef WITH_REALTIME_JAVA
  memset(&total_finalize_time, 0, sizeof(total_finalize_time));
#endif
}

void print_GC_stats() {
  printf("Total GC time: %ds %dus\n", total_GC_time.tv_sec, total_GC_time.tv_nsec/1000);
  printf("Max GC pause: %ds %dus\n", max_GC_time.tv_sec, max_GC_time.tv_nsec/1000);
#ifdef WITH_REALTIME_JAVA
  printf("Total CT finalizer time: %ds %dus\n", total_finalize_time.tv_sec,
	 total_finalize_time.tv_nsec/1000);
#endif
}

MALLOC_STATS(malloc);
MALLOC_STATS_VARARG(calloc, (size_t nmemb, size_t size), (nmemb, size));
FREE_STATS(free);

#ifdef BDW_CONSERVATIVE_GC
MALLOC_STATS(GC_malloc);
MALLOC_STATS(GC_malloc_atomic);
MALLOC_STATS(GC_malloc_uncollectable);
FREE_STATS(GC_free);
#endif

#ifdef WITH_PRECISE_GC
MALLOC_STATS(precise_malloc);
#endif

#undef MALLOC_STATS_VARARG
#undef MALLOC_STATS
#undef FREE_STATS

