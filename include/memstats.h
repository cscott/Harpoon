#include "config.h"

#ifdef WITH_MEMORYSTATISTICS
#include "flexthread.h"

extern long memorystat;

#if WITH_THREADS
extern flex_mutex_t memstat_mutex;
#endif

extern long peakusage;
extern long peakusagea;
void update_stats();
void update_stacksize(long);

#define INCREMENT_MALLOC(x) \
FLEX_MUTEX_LOCK(&memstat_mutex);\
memorystat+=x;\
update_stats();\
FLEX_MUTEX_UNLOCK(&memstat_mutex);

#define DECREMENT_MALLOC(x) \
FLEX_MUTEX_LOCK(&memstat_mutex);\
memorystat-=x;\
update_stats();\
FLEX_MUTEX_UNLOCK(&memstat_mutex);
#else
#define INCREMENT_MALLOC(x)
#define DECREMENT_MALLOC(x)
#endif






