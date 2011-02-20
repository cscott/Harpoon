#include "edu_uci_ece_ac_time_HighResClock.h"
#include <sys/time.h>
#include "SysInfo.h"
#include <iostream>

//#define RTJPERF_ESTIMATE_RDTSC_OVERHEAD 1
/*
 * Class:     edu_uci_ece_doc_rtjperf_sys_HighResClock
 * Method:    getTime
 * Signature: (Ledu/uci/ece/doc/rtjperf/sys/HighResTime;)V
 */
JNIEXPORT void JNICALL
Java_edu_uci_ece_ac_time_HighResClock_getTime(JNIEnv *env, jclass clazz, jobject object)
{
  unsigned long long int rdtsc = 0;
  __asm__ volatile (".byte 0x0f, 0x31" : "=A" (rdtsc));
  
  // Convert clock number into nano seconds
  long double timeNS = rdtsc * CLOCK_PERIOD_NS;
  long cMilliSec = static_cast<long>(timeNS/POW_6_TEN);
  long cRoundNanoSec = static_cast<long>(cMilliSec * POW_6_TEN);
  long cMicroSec = static_cast<long>((timeNS - cRoundNanoSec)/POW_3_TEN);
  long cNanoSec = static_cast<long>(timeNS - cRoundNanoSec - (cMicroSec*POW_3_TEN));
  
  jclass cls = env->GetObjectClass(object);
  jmethodID setTimeMethod = env->GetMethodID(cls, "setTime", "(JJJ)V");

  env->CallVoidMethod(object,
                      setTimeMethod,
                      static_cast<jlong>(cMilliSec),
                      static_cast<jlong>(cMicroSec),
                      static_cast<jlong>(cNanoSec));
}


/*
 * Class:     edu_uci_ece_doc_rtjperf_sys_HighResClock
 * Method:    getClockTickCount
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_edu_uci_ece_ac_time_HighResClock_getClockTickCount(JNIEnv *, jclass)
{
  unsigned long long int rdtsc = 0;
  __asm__ volatile (".byte 0x0f, 0x31" : "=A" (rdtsc));
  
  return static_cast<jlong>(rdtsc);
}


/*
 * Class:     edu_uci_ece_doc_rtjperf_sys_HighResClock
 * Method:    clockTick2HighResTime
 * Signature: (JLedu/uci/ece/doc/rtjperf/sys/HighResTime;)V
 */
JNIEXPORT void JNICALL
Java_edu_uci_ece_ac_time_HighResClock_clockTick2HighResTime(JNIEnv *env, jclass clazz, jlong rdtsc, jobject object)
{
  long double timeNS = rdtsc * CLOCK_PERIOD_NS;

  long cMilliSec = static_cast<long>(timeNS/POW_6_TEN);
  long cRoundNanoSec = static_cast<long>(cMilliSec * POW_6_TEN);
  long cMicroSec = static_cast<long>((timeNS - cRoundNanoSec)/POW_3_TEN);
  long cNanoSec = static_cast<long>(timeNS - cRoundNanoSec - (cMicroSec*POW_3_TEN));

  jclass cls = env->GetObjectClass(object);
  jmethodID setTimeMethod = env->GetMethodID(cls, "setTime", "(JJJ)V");

  env->CallVoidMethod(object,
                      setTimeMethod,
                      static_cast<jlong>(cMilliSec),
                      static_cast<jlong>(cMicroSec),
                      static_cast<jlong>(cNanoSec));
  
}

/*
 * Class:     edu_uci_ece_doc_rtjperf_sys_HighResClock
 * Method:    getClockFrequency
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL
Java_edu_uci_ece_ac_time_HighResClock_getClockFrequency(JNIEnv *, jclass)
{
  return static_cast<jfloat>(CLOCK_FREQUENCY);
}


/*
 * Class:     edu_uci_ece_doc_rtjperf_sys_HighResClock
 * Method:    getClockPeriod
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL
Java_edu_uci_ece_ac_time_HighResClock_getClockPeriod(JNIEnv *, jclass)
{
  return static_cast<jdouble>(CLOCK_PERIOD_NS);
}
