#include <assert.h>
#include <sys/time.h>
#include <unistd.h>
#include "omit_gc_timer.h"

static double start_time = 0;
static double total_time = 0;

double current_seconds()
{
  struct timeval tv;
  //struct timezone tz;
  
  gettimeofday(&tv,0);
  //gettimeofday(&tv,&tz);
  // gets time
  return ((double) tv.tv_sec + ((double) tv.tv_usec)/1000000.);
}

double get_total_time()
{
  if (start_time == 0) return total_time;
  return total_time + (current_seconds() - start_time);
}

void init_timer()
{
  atexit(print_timer_output);
  start_timer();
}

void pause_timer()
{
  total_time += current_seconds() - start_time;
  assert(total_time > 0);
  start_time = 0;
}

void print_timer_output()
{
  printf("Time without gc is %lf seconds.\n", get_total_time());
}

void start_timer()
{
  start_time = current_seconds();
}
