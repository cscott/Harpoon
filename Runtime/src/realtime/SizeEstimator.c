#include "SizeEstimator.h"
#include "jni-private.h"

JNIEXPORT jlong JNICALL Java_javax_realtime_SizeEstimator_objSize
(JNIEnv *env, jclass claz, jclass objClaz) {
  jlong result = (jlong)FNI_ClassSize(objClaz);
  result += ((result % 16) == 0) ? 0 : (16 - (result % 16));
  return result;
}
