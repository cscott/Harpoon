#include "config.h"

#ifdef WITH_INIT_CHECK
#include "../../java.lang.reflect/reflect-util.h"

/* From classpath sources */
JNIEXPORT jobject JNICALL
Java_java_io_ObjectInputStream_allocateObject(JNIEnv *env,
					      jobject self,
					      jclass clazz);

JNIEXPORT jobject JNICALL
Java_java_io_ObjectInputStream_allocateObject_00024_00024initcheck(JNIEnv *env,
								   jobject self,
								   jclass clazz);
#endif

