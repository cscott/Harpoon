#include <jni.h>

jclass SchedulerClaz;
jmethodID Scheduler_jAddCThread;
jmethodID Scheduler_jChooseThread;
jmethodID Scheduler_jDisableThread;
jmethodID Scheduler_jEnableThread;
jmethodID Scheduler_jNumThreads;
jmethodID Scheduler_jRemoveCThread;
jmethodID Scheduler_print;

/* Event handlers */
jmethodID Scheduler_handle_mutex_init;
jmethodID Scheduler_handle_mutex_destroy;
jmethodID Scheduler_handle_mutex_trylock;
jmethodID Scheduler_handle_mutex_lock;
jmethodID Scheduler_handle_mutex_unlock;
jmethodID Scheduler_handle_cond_init;
jmethodID Scheduler_handle_cond_destroy;
jmethodID Scheduler_handle_cond_broadcast;
jmethodID Scheduler_handle_cond_signal;
jmethodID Scheduler_handle_cond_timedwait;
jmethodID Scheduler_handle_cond_wait;

jmethodID Scheduler_getScheduler;

void Scheduler_init(JNIEnv* env);
