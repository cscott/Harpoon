/* Statistics stubs */
#include <stdio.h>
#include <signal.h>
#include "config.h"
#include "fni-stats.h"

#ifndef WITH_STATISTICS /* consistency checking... */
#error The src/stats module should only be compiled #ifdef WITH_STATISTICS
#endif /* WITH_STATISTICS */

/* allocate and initialize the statistics counters */
DECLARE_STATS_LOCAL(monitor_enter)
DECLARE_STATS_LOCAL(monitor_contention)
#ifdef WITH_CLUSTERED_HEAPS
#include "../clheap/misc.h" /* for POOLSIZE / HEAPSIZE */
DECLARE_STATS_LOCAL(stk_bytes_alloc)
DECLARE_STATS_LOCAL(stk_objs_alloc)
DECLARE_STATS_LOCAL(gbl_bytes_alloc)
DECLARE_STATS_LOCAL(gbl_objs_alloc)
DECLARE_STATS_LOCAL(thr_bytes_alloc)
DECLARE_STATS_LOCAL(thr_objs_alloc)
DECLARE_STATS_LOCAL(thr_bytes_overflow)
DECLARE_STATS_LOCAL(thread_heaps_created)
DECLARE_STATS_LOCAL(threads_created)
#endif /* WITH_CLUSTERED_HEAPS */

#undef EXTRA_STATS
#ifdef EXTRA_STATS
#include <jni.h>
#include <jni-private.h>
#endif

/* counter for total garbage collection time. */
#ifdef BDW_CONSERVATIVE_GC
extern double ttl_gc_time;
#endif

void print_statistics(void);
static void stat_signal_handler(int sig) { print_statistics(); }

#define FS(x) (unsigned long long) FETCH_STATS(x) /* convenience macro */
void print_statistics(void) {
  /* lead off with a new-line in case we're using SIGALRM */
#ifdef EXTRA_STATS
  JNIEnv *env;
  jclass cls;
  jmethodID mid;
  env=FNI_GetJNIEnv();
  cls=(*env)->FindClass(env,"harpoon/Analysis/ContBuilder/Scheduler");
  mid=(*env)->GetStaticMethodID(env,cls,"printresults","()V");
  (*env)->CallStaticVoidMethod(env, cls, mid);
  CHECK_EXCEPTIONS(env);
  (*env)->DeleteLocalRef(env, cls);
#endif
  printf("\n");
#ifdef BDW_CONSERVATIVE_GC
  printf("Total gc time at this point: %f ms\n", (float) ttl_gc_time);
#endif
  printf("MonitorEnter operations: %8llu (contention on %llu ops)\n",
	 FS(monitor_enter), FS(monitor_contention));
#ifdef WITH_CLUSTERED_HEAPS
  printf("HEAPSIZE: %ld  POOLSIZE: %ld  THRALLOC: %d  STKALLOC: %d\n"
	 "Stack allocation:        %8llu bytes %8llu objects\n"
	 "Thread-local allocation: %8llu bytes %8llu objects\n"
	 "Global allocation:       %8llu bytes %8llu objects\n"
	 "Threads created:         %8llu (in %llu heaps)\n"
	 "Average heap size/thread:%8llu bytes\n"
	 "Thread heap overflow:    %8llu bytes\n",
	 (long) HEAPSIZE, (long) POOLSIZE,
#ifdef REALLY_DO_THR_ALLOC
	 1,
#else
	 0,
#endif
#ifdef REALLY_DO_STK_ALLOC
	 1,
#else
	 0,
#endif
	 FS(stk_bytes_alloc), FS(stk_objs_alloc),
	 FS(thr_bytes_alloc), FS(thr_objs_alloc),
	 FS(gbl_bytes_alloc), FS(gbl_objs_alloc),
	 FS(threads_created), FS(thread_heaps_created),
	 FS(threads_created) ? FS(thr_bytes_alloc)/FS(threads_created) : 0LL,
	 FS(thr_bytes_overflow));
#endif /* WITH_CLUSTERED_HEAPS */
  fflush(stdout);
  

  /* print out statistics on demand */
  signal(SIGALRM, stat_signal_handler);
}

