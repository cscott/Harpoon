#include "config.h"

#ifdef WITH_MEMORYSTATISTICS
#include "flexthread.h"
#include "gc.h"
#include <time.h> /* time,localtime for time zone information */
#include <sys/time.h> /* gettimeofday */
#include <stdio.h>
extern FILE *stderr;

long memorystat;
#if WITH_THREADS
flex_mutex_t memstat_mutex=FLEX_MUTEX_INITIALIZER;
#endif
long peakusage;
long lastpeak;
long peakusagea;
long lastpeaka;
long startclock;
long initialclock;

void update_stats() {
  long memstat=memorystat;
  long heapsize=GC_get_heap_size();
  long freesize=GC_get_free_bytes();
  long t;
  struct timeval tv; struct timezone tz;

  if ((memstat+heapsize)>peakusage)
    peakusage=heapsize+memstat;

  if ((memstat+heapsize-freesize)>peakusagea)
    peakusagea=heapsize+memstat-freesize;
  

  gettimeofday(&tv, &tz);
  t = tv.tv_sec; /* seconds */
  t*=1000; /* milliseconds */
  t+= (tv.tv_usec/1000); /* adjust milliseconds & add them in */
  if (t>(startclock+100)) {
    /* log the current peak value and zero it */
    if (initialclock==0)
      initialclock=t;
    fprintf(stderr,"\n time= %ld peak= %ld\n",t-initialclock,lastpeaka);
    lastpeak=0;
    lastpeaka=0;
    startclock=t;
  }
  if ((memstat+heapsize)>lastpeak)
    lastpeak=heapsize+memstat;

  if ((memstat+heapsize-freesize)>lastpeaka)
    lastpeaka=heapsize+memstat-freesize;

}
#endif

