/* Implementations for class java_io_File */
#include "java_io_File.h"
#include "config.h"
#include <assert.h>
#include <errno.h>
#include <sys/stat.h>
#include <unistd.h>
#ifdef WITH_HEAVY_THREADS
#include <pthread.h>    /* for mutex ops */
#endif

static jfieldID pathID = 0; /* The field ID of File.path */
static int inited = 0; /* whether the above variables have been initialized */
#ifdef WITH_HEAVY_THREADS
static pthread_mutex_t init_mutex = PTHREAD_MUTEX_INITIALIZER;
#endif

static int initializeFI(JNIEnv *env) {
  jclass FIcls;

#ifdef WITH_HEAVY_THREADS
  pthread_mutex_lock(&init_mutex);
  // other thread may win race to lock and init before we do.
  if (inited) goto done;
#endif
  
  FIcls = (*env)->FindClass(env, "java/io/File");
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

#if 0 /* unimplemented */
/*
 * Class:     java_io_File
 * Method:    exists0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_exists0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    canWrite0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_canWrite0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    canRead0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_canRead0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    isFile0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_isFile0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    isDirectory0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_isDirectory0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    lastModified0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_io_File_lastModified0
  (JNIEnv *, jobject);
#endif /* unimplemented */

/*
 * Class:     java_io_File
 * Method:    length0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_io_File_length0(JNIEnv * env, jobject this)
{
  struct stat buf;
  int r;
  jobject jstr;
  const char * cstr;    
  
  if (!inited && !initializeFI(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  cstr=(*env)->GetStringUTFChars(env,jstr,0);
  r = stat(cstr, &buf);	
  (*env)->ReleaseStringUTFChars(env,jstr,cstr);
	

  if (r != 0) {
    return ((jlong)0);
  }
  return ((jlong)buf.st_size);
}

#if 0 /* unimplemented */
/*
 * Class:     java_io_File
 * Method:    mkdir0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_mkdir0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    renameTo0
 * Signature: (Ljava/io/File;)Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_renameTo0
  (JNIEnv *, jobject, jobject);

/*
 * Class:     java_io_File
 * Method:    delete0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_delete0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    rmdir0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_rmdir0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    list0
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_java_io_File_list0
  (JNIEnv *, jobject);

/*
 * Class:     java_io_File
 * Method:    canonPath
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_io_File_canonPath
  (JNIEnv *, jobject, jstring);

/*
 * Class:     java_io_File
 * Method:    isAbsolute
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_isAbsolute
  (JNIEnv *, jobject);

#endif /* unimplemented */
