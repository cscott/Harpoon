#include "config.h"

#ifdef WITH_MEMORYSTATISTICS

extern long memorystat;
extern flex_mutex_t memstat_mutex;
extern long peakusage;
void update_stats();

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




