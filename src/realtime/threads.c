#include <jni.h>
#include <assert.h>

void Start_QuantaThread(JNIEnv *env)
{
  jclass cls;
  jmethodID qtConstruct;
  jobject quantaThread;


  cls = (*env)->FindClass(env, "javax/realtime/QuantaThread");
  assert(!((*env)->ExceptionOccurred(env)));
  qtConstruct = (*env)->GetMethodID(env, cls, "<init>", "()V");
  assert(!((*env)->ExceptionOccurred(env)));
  quantaThread = (*env)->NewObject(env, cls, qtConstruct);
  assert(!((*env)->ExceptionOccurred(env)));
}

void Stop_QuantaThread(JNIEnv *env)
{
  jclass cls;
  jfieldID doneField;

  cls = (*env)->FindClass(env, "javax/realtime/QuantaThread");
  assert(!((*env)->ExceptionOccurred(env)));

  doneField = (*env)->GetStaticFieldID(env, cls, "done", "Z");
  assert(!((*env)->ExceptionOccurred(env)));

  (*env)->SetStaticBooleanField(env, cls, doneField, JNI_TRUE);
  assert(!((*env)->ExceptionOccurred(env)));
}

void HandleQuantaFlag()
{
  jclass cls;
  jfieldID flagField;
  JNIEnv *env;

  printf("Quanta Flag is Set.\n");

  env = FNI_GetJNIEnv();

  cls = (*env)->FindClass(env, "javax/realtime/QuantaThread");
  assert(!((*env)->ExceptionOccurred(env)));
  
  flagField = (*env)->GetStaticFieldID(env, cls, "timerFlag", "I");
  assert(!((*env)->ExceptionOccurred(env)));

  (*env)->SetStaticIntField(env, cls, flagField, 0);
  assert(!((*env)->ExceptionOccurred(env)));
}
