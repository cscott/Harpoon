#include "java_io_ObjectInputStream.h"

#ifdef WITH_INIT_CHECK
JNIEXPORT jobject JNICALL
Java_java_io_ObjectInputStream_allocateObject_00024_00024initcheck( JNIEnv * env,
								    jobject self,
								    jclass clazz )
{
  if (!REFLECT_staticinit(env, clazz)) return NULL; // static init failed
  return Java_java_io_ObjectInputStream_allocateObject(env, self, clazz);
}
#endif
