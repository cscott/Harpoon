#include "config.h"

#ifdef WITH_MEMORY_STATISTICS
#include "flexthread.h"

extern long memorystat;

#if WITH_THREADS
extern flex_mutex_t memstat_mutex;
#endif

extern long peakusage;
extern long peakusagea;
void update_stats();
void update_stacksize(long);

#define INCREMENT_MEM_STATS(x) \
FLEX_MUTEX_LOCK(&memstat_mutex);\
memorystat+=x;\
update_stats();\
FLEX_MUTEX_UNLOCK(&memstat_mutex);

#define DECREMENT_MEM_STATS(x) \
FLEX_MUTEX_LOCK(&memstat_mutex);\
memorystat-=x;\
update_stats();\
FLEX_MUTEX_UNLOCK(&memstat_mutex);
#else
#define INCREMENT_MEM_STATS(x)
#define DECREMENT_MEM_STATS(x)
#endif






