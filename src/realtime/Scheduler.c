#include <time.h>
#include <assert.h>
#include "Scheduler.h"
#include "SchedulerData.h"
#include "threads.h"

void Scheduler_init(JNIEnv* env) {
  SchedulerClaz = (*env)->FindClass(env, "javax/realtime/Scheduler");
  assert(!((*env)->ExceptionOccurred(env)));
  SchedulerClaz = (*env)->NewGlobalRef(env, SchedulerClaz);
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jAddCThread = (*env)->GetStaticMethodID(env, SchedulerClaz, "jAddCThread", "(J)V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jChooseThread = (*env)->GetMethodID(env, SchedulerClaz, "jChooseThread", "(J)J");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jDisableThread = (*env)->GetStaticMethodID(env, SchedulerClaz, "jDisableThread", 
						       "(Ljavax/realtime/RealtimeThread;J)V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jEnableThread = (*env)->GetStaticMethodID(env, SchedulerClaz, "jEnableThread", 
						      "(Ljavax/realtime/RealtimeThread;J)V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jNumThreads = (*env)->GetStaticMethodID(env, SchedulerClaz, "jNumThreads", "()J");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_jRemoveCThread = (*env)->GetStaticMethodID(env, SchedulerClaz, "jRemoveCThread", "(J)V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_print = (*env)->GetStaticMethodID(env, SchedulerClaz, "print", "()V");
  assert(!((*env)->ExceptionOccurred(env)));

  Scheduler_handle_mutex_init = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_mutex_init", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_mutex_destroy = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_mutex_destroy", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_mutex_trylock = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_mutex_trylock", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_mutex_lock = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_mutex_lock", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_mutex_unlock = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_mutex_unlock", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_cond_init = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_cond_init", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_cond_destroy = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_cond_destroy", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_cond_broadcast = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_cond_broadcast", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_cond_signal = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_cond_signal", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_cond_timedwait = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_cond_timedwait", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  Scheduler_handle_cond_wait = (*env)->GetStaticMethodID(env, SchedulerClaz, "jhandle_cond_wait", "()V");
  assert(!((*env)->ExceptionOccurred(env)));

  Scheduler_getScheduler = (*env)->GetStaticMethodID(env, SchedulerClaz, "getScheduler", "()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));
}

#ifndef WITH_REALTIME_THREADS
JNIEXPORT jint JNICALL Java_javax_realtime_Scheduler_beginAtomic
  (JNIEnv *env, jobject scheduler) {
  assert(0);
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_endAtomic
  (JNIEnv *env, jobject scheduler, jint state) {
  assert(0);
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_addThreadInC
(JNIEnv* env, jobject _this, jobject thread, jlong threadID) {
  assert(0);
}

JNIEXPORT jlong JNICALL Java_javax_realtime_Scheduler_removeThreadInC
(JNIEnv* env, jobject _this, jobject thread) {
  assert(0);
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_setQuanta
(JNIEnv* env, jobject _this, jlong microsecs) {
  assert(0);
}
#else 
JNIEXPORT jint JNICALL Java_javax_realtime_Scheduler_beginAtomic
  (JNIEnv *env, jobject scheduler) {
  return StopSwitching();
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_endAtomic
  (JNIEnv *env, jobject scheduler, jint state) {
  RestoreSwitching(state);
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_setQuanta
(JNIEnv* env, jobject _this, jlong microsecs) {
#ifdef RTJ_DEBUG_THREADS
  printf("\n  Scheduler.setQuanta(%p, %p, %lld)", env, 
	 _this, (long long)microsecs);
#endif
  setQuanta(microsecs);
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_addThreadInC
(JNIEnv* env, jobject _this, jobject thread, jlong threadID) {
  addThreadInC(env, thread, threadID);
}

JNIEXPORT jlong JNICALL Java_javax_realtime_Scheduler_removeThreadInC
(JNIEnv* env, jobject _this, jobject thread) {
  return removeThreadInC(env, thread);
}

JNIEXPORT void JNICALL Java_javax_realtime_Scheduler_sleep
(JNIEnv* env, jobject _this, jlong microsecs) {
  struct timespec req;
  req.tv_sec = ((long long int)microsecs)/1000000;
  req.tv_nsec = ((long long int)microsecs)*1000;
  nanosleep(&req, NULL);
}

#endif

