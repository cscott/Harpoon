#include "RealtimeThread.h"

jclass RealtimeThreadClaz;
jmethodID RealtimeThread_getScheduler;
jmethodID RealtimeThread_initScheduler;
jmethodID RealtimeThread_schedule;
jmethodID RealtimeThread_unschedule;

jmethodID RealtimeThread_handler_mask;

jlong handlerMask;

void RealtimeThread_init(JNIEnv* env);

void RealtimeThread_setHandlerMask(JNIEnv* env, jobject thread);
