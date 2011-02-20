#include <assert.h>
#include "RealtimeThreadData.h"

void RealtimeThread_init(JNIEnv *env) {
  RealtimeThreadClaz = (*env)->FindClass(env, "javax/realtime/RealtimeThread");
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThreadClaz = (*env)->NewGlobalRef(env, RealtimeThreadClaz);
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThread_getScheduler = (*env)->GetMethodID(env, RealtimeThreadClaz, "getScheduler",
						    "()Ljavax/realtime/Scheduler;");
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThread_initScheduler = (*env)->GetMethodID(env, RealtimeThreadClaz, "initScheduler", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThread_schedule = (*env)->GetMethodID(env, RealtimeThreadClaz, "schedule", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  RealtimeThread_unschedule = (*env)->GetMethodID(env, RealtimeThreadClaz, "unschedule", "()V");
  assert(!((*env)->ExceptionOccurred(env)));

  RealtimeThread_handler_mask = (*env)->GetMethodID(env, RealtimeThreadClaz, "handler_mask", "()J");
  assert(!((*env)->ExceptionOccurred(env)));
}

jlong handlerMask = 0;

void RealtimeThread_setHandlerMask(JNIEnv* env, jobject thread) {
  handlerMask = (*env)->CallLongMethod(env, thread, RealtimeThread_handler_mask);
  assert(!((*env)->ExceptionOccurred(env)));
}
