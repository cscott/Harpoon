#include "config.h"
#include "flexthread.h"
#include "gc.h"
#include <time.h> /* time,localtime for time zone information */
#include <sys/time.h> /* gettimeofday */


#ifdef WITH_MEMORYSTATISTICS
long memorystat;
flex_mutex_t memstat_mutex=FLEX_MUTEX_INITIALIZER;
long peakusage;
long lastpeak;
long startclock;
long initialclock;

void update_stats() {
  long memstat=memorystat;
  long heapsize=GC_get_heap_size();
  long t;
  struct timeval tv; struct timezone tz;

  if ((memstat+heapsize)>peakusage)
    peakusage=heapsize+memstat;
  

  gettimeofday(&tv, &tz);
  t = tv.tv_sec; /* seconds */
  t*=1000; /* milliseconds */
  t+= (tv.tv_usec/1000); /* adjust milliseconds & add them in */
  if (t>(startclock+100)) {
    /* log the current peak value and zero it */
    if (initialclock==0)
      initialclock=t;
    printf("time = %ld peak = %ld\n",t-initialclock,lastpeak);
    lastpeak=0;
    startclock=t;
  }
  if ((memstat+heapsize)>lastpeak)
    lastpeak=heapsize+memstat;
}
#endif

