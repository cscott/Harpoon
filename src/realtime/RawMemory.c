#include "RawMemory.h"

#define get(claz, type, name) \
JNIEXPORT type JNICALL Java_javax_realtime_##claz##_get##name \
(JNIEnv* env, jobject rawMemory, jlong offset) { \
\
} 

#define getArray(claz, type, name) \
JNIEXPORT void JNICALL Java_javax_realtime_##claz##_get##name##s \
(JNIEnv* env, jobject rawMemory, jlong offset, type##Array array, \
 jint low, jint n) { \
\
} 

#define set(claz, type, name) \
JNIEXPORT type JNICALL Java_javax_realtime_##claz##_set##name \
(JNIEnv* env, jobject rawMemory, jlong offset, type item) { \
\
}

#define setArray(claz, type, name) \
JNIEXPORT void JNICALL Java_javax_realtime_##claz##_set##name##s \
(JNIEnv* env, jobject rawMemory, jlong offset, type##Array array, \
 jint low, jint n) { \
\
}

#define rawAccessFuncs(claz, type, name) \
get(claz, type, name); \
getArray(claz, type, name); \
set(claz, type, name); \
setArray(claz, type, name);

rawAccessFuncs(RawMemoryAccess, jbyte, Byte);
rawAccessFuncs(RawMemoryAccess, jint, Int);
rawAccessFuncs(RawMemoryAccess, jlong, Long);
rawAccessFuncs(RawMemoryAccess, jshort, Short);
rawAccessFuncs(RawMemoryFloatAccess, jdouble, Double);
rawAccessFuncs(RawMemoryFloatAccess, jfloat, Float);

#undef get
#undef getArray
#undef set
#undef setArray
#undef rawAccessFuncs
