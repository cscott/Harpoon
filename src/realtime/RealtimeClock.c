#include "RealtimeClock.h"
#include "threads.h"
#include <sys/time.h>

JNIEXPORT jlong JNICALL Java_javax_realtime_RealtimeClock_getTimeInC
(JNIEnv* env, jobject _this) {
  struct timeval time;
  gettimeofday(&time, NULL);
#ifdef RTJ_DEBUG_THREADS
  printf("\n  (%lld s,%lld us) = RealtimeClock.getTimeInC(%p, %p)", 
	 (long long int)time.tv_sec, (long long int)time.tv_usec, env, _this);
#endif
  return time.tv_sec * 1000000 + time.tv_usec;
}
