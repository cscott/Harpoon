#include "java_io_File.h"
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
#ifdef WITH_HEAVY_THREADS
#include <pthread.h>    /* for mutex ops */
#endif

static jfieldID pathID = 0;
static int inited = 0;

#ifdef WITH_HEAVY_THREADS
static pthread_mutex_t init_mutex = PTHREAD_MUTEX_INITIALIZER;
#endif

JNIEXPORT jlong java_io_File_length0(JNIEnv * env, jobject this)
{
  struct stat buf;
  int r;
  jobject jstr;
  const char * cstr;    
  
  if (!inited && !initializeFD(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  cstr=(*env)->GetStringUTFChars(env,jstr,0);
  r = stat(cstr, &buf);	
  (*env)->ReleaseStringUTFChars(env,jstr,cstr);
	

  if (r != 0) {
    return ((jlong)0);
  }
  return ((jlong)buf.st_size);
}

static int initializeFI(JNIEnv *env) {
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_lock(&init_mutex);
  // other thread may win race to lock and init before we do.
  if (inited) goto done;
#endif
  
  jclass FIcls = (*env)->FindClass(env, "java/io/File");
  if ((*env)->ExceptionOccurred(env)) goto done;
  pathID = (*env)->GetFieldID(env,FIcls,"path","Ljava/lang/String;");
  if ((*env)->ExceptionOccurred(env)) goto done;
  
  inited = 1;
 done:
#ifdef WITH_HEAVY_THREADS
  pthread_mutex_unlock(&init_mutex);
#endif
  return inited;
}
