#ifndef INCLUDED_FNI_OBJECT_H
#define INCLUDED_FNI_OBJECT_H

#include "config.h"
#include <sys/time.h> /* for struct timeval */
#include <time.h> /* for struct timespec */
#include <string.h> /* for memcpy */

/* helper for clone functions. */
static inline
jobject fni_object_cloneHelper(JNIEnv *env, jobject obj, jsize len) {
  jobject clone = FNI_Alloc(env, NULL, FNI_CLAZ(FNI_UNWRAP_MASKED(obj)),
			    NULL/*default alloc func*/, len);
#ifdef WITH_GENERATIONAL_GC
  add_to_curr_obj_list(FNI_UNWRAP_MASKED(clone));
#endif /* WITH_GENERATIONAL_GC */
  memcpy(FNI_UNWRAP_MASKED(clone)->field_start,
	 FNI_UNWRAP_MASKED(obj  )->field_start,
	 len - sizeof(struct oobj));
#ifdef WITH_CLAZ_SHRINK
  /* need to copy the non-hashcode header space, too */
  memcpy(FNI_UNWRAP_MASKED(clone), FNI_UNWRAP_MASKED(obj), 4);
#endif
#ifdef WITH_ROLE_INFER
  NativeassignUID(env, clone, FNI_WRAP(FNI_CLAZ(FNI_UNWRAP(obj))->class_object));
  RoleInference_clone(env, obj, clone);
#endif
  return clone;
}

static inline
void fni_object_notify(JNIEnv *env, jobject _this) {
  FNI_MonitorNotify(env, _this, JNI_FALSE);
}

static inline
void fni_object_notifyAll(JNIEnv *env, jobject _this) {
  FNI_MonitorNotify(env, _this, JNI_TRUE);
}

static inline
void fni_object_wait(JNIEnv *env, jobject _this, jlong millis, jint nanos) {
  struct timeval tp; struct timespec ts;
  int rc;

  /* make val into an absolute timespec */
  rc =  gettimeofday(&tp, NULL); assert(rc==0);
  /* Convert from timeval to timespec */
  ts.tv_sec  = tp.tv_sec;
  ts.tv_nsec = tp.tv_usec * 1000;
  ts.tv_sec += millis/1000;
  ts.tv_nsec+= 1000*(millis%1000) + nanos;
  while (ts.tv_nsec > 1000000000) { ts.tv_nsec-=1000000000; ts.tv_sec++; }

  /* okay, do the wait */
  FNI_MonitorWait(env, _this, (millis==0&&nanos==0)?NULL:&ts);
}

#endif /* INCLUDED_FNI_OBJECT_H */
