#include "edu_uci_ece_ac_time_HighResClock.h"
#include <sys/time.h>
#include "SysInfo.h"

//#define RTJPERF_ESTIMATE_RDTSC_OVERHEAD 1
/*
 * Class:     edu_uci_ece_doc_rtjperf_sys_HighResClock
 * Method:    getTime
 * Signature: (Ledu/uci/ece/doc/rtjperf/sys/HighResTime;)V
 */
JNIEXPORT void JNICALL
Java_edu_uci_ece_ac_time_HighResClock_getTime(JNIEnv *env, jclass clazz, jobject object)
{
  // Convert clock number into nano seconds
  unsigned long long int rdtsc = 0;
  long double timeNS;
  long cMilliSec, cRoundNanoSec, cMicroSec, cNanoSec;
  jclass cls;
  jmethodID setTimeMethod;

  __asm__ volatile (".byte 0x0f, 0x31" : "=A" (rdtsc));
  
  timeNS = rdtsc * CLOCK_PERIOD_NS;
  cMilliSec = (long)(timeNS/POW_6_TEN);
  cRoundNanoSec = (long)(cMilliSec * POW_6_TEN);
  cMicroSec = (long)((timeNS - cRoundNanoSec)/POW_3_TEN);
  cNanoSec = (long)(timeNS - cRoundNanoSec - (cMicroSec*POW_3_TEN));

  cls = (*env)->GetObjectClass(env, object);
  setTimeMethod = (*env)->GetMethodID(env, cls, "setTime", "(JJJ)V");

  (*env)->CallVoidMethod(env,
			 object,
			 setTimeMethod,
			 (jlong)(cMilliSec),
			 (jlong)(cMicroSec),
			 (jlong)(cNanoSec));
}


/*
 * Class:     edu_uci_ece_doc_rtjperf_sys_HighResClock
 * Method:    getClockTickCount
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_edu_uci_ece_ac_time_HighResClock_getClockTickCount(JNIEnv *env, jclass cls)
{
  unsigned long long int rdtsc = 0;
  __asm__ volatile (".byte 0x0f, 0x31" : "=A" (rdtsc));
  
  return (jlong)(rdtsc);
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

  long cMilliSec = (long)(timeNS/POW_6_TEN);
  long cRoundNanoSec = (long)(cMilliSec * POW_6_TEN);
  long cMicroSec = (long)((timeNS - cRoundNanoSec)/POW_3_TEN);
  long cNanoSec = (long)(timeNS - cRoundNanoSec - (cMicroSec*POW_3_TEN));

  jclass cls = (*env)->GetObjectClass(env,
				      object);
  jmethodID setTimeMethod = (*env)->GetMethodID(env, cls, "setTime", "(JJJ)V");

  (*env)->CallVoidMethod(env,
			 object,
			 setTimeMethod,
			 (jlong)(cMilliSec),
			 (jlong)(cMicroSec),
			 (jlong)(cNanoSec));
  
}

/*
 * Class:     edu_uci_ece_doc_rtjperf_sys_HighResClock
 * Method:    getClockFrequency
 * Signature: ()F
 */
JNIEXPORT jfloat JNICALL
Java_edu_uci_ece_ac_time_HighResClock_getClockFrequency(JNIEnv *env, jclass cls)
{
  return (jfloat)(CLOCK_FREQUENCY);
}


/*
 * Class:     edu_uci_ece_doc_rtjperf_sys_HighResClock
 * Method:    getClockPeriod
 * Signature: ()D
 */
JNIEXPORT jdouble JNICALL
Java_edu_uci_ece_ac_time_HighResClock_getClockPeriod(JNIEnv *env, jclass cls)
{
  return (jdouble)(CLOCK_PERIOD_NS);
}
