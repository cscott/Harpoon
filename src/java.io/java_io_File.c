/* Implementations for class java_io_File */
#include "java_io_File.h"
#include "config.h"
#include <assert.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <unistd.h>
#include <stdlib.h> /* for malloc */
#include "flexthread.h" /* for mutex ops */
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif

static jfieldID pathID = 0; /* The field ID of File.path */
static int inited = 0; /* whether the above variables have been initialized */
FLEX_MUTEX_DECLARE_STATIC(init_mutex);

static int initializeFI(JNIEnv *env) {
  jclass FIcls;

  FLEX_MUTEX_LOCK(&init_mutex);
  // other thread may win race to lock and init before we do.
  if (inited) goto done;
  
  FIcls = (*env)->FindClass(env, "java/io/File");
  if ((*env)->ExceptionOccurred(env)) goto done;
  pathID = (*env)->GetFieldID(env,FIcls,"path","Ljava/lang/String;");
  if ((*env)->ExceptionOccurred(env)) goto done;

  inited = 1;
 done:
  FLEX_MUTEX_UNLOCK(&init_mutex);
  return inited;
}


/*
 * Class:     java_io_File
 * Method:    exists0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_exists0(JNIEnv *env, jobject this) {
  struct stat buf;
  int r;
  jobject jstr;
  const char * cstr;    
  
  if (!inited && !initializeFI(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  cstr=(*env)->GetStringUTFChars(env,jstr,0);
  r = stat(cstr, &buf);	
  (*env)->ReleaseStringUTFChars(env,jstr,cstr);
	
  if (r != 0) 
    return ((jboolean)0);
  else
    return ((jboolean)1);
}

#if 0
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

#endif
/*
 * Class:     java_io_File
 * Method:    isFile0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_isFile0(JNIEnv * env, jobject this){
  struct stat buf;
  int r;
  jobject jstr;
  const char * cstr;    
  
  if (!inited && !initializeFI(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  cstr=(*env)->GetStringUTFChars(env,jstr,0);
  r = stat(cstr, &buf);	
  (*env)->ReleaseStringUTFChars(env,jstr,cstr);
	
  if ((r == 0) && S_ISREG(buf.st_mode))
    return ((jboolean)1);
  else
    return ((jboolean)0);
}


/*
 * Class:     java_io_File
 * Method:    isDirectory0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_isDirectory0(JNIEnv *env, jobject this) {
  struct stat buf;
  int r;
  jobject jstr;
  const char * cstr;    
  
  if (!inited && !initializeFI(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  cstr=(*env)->GetStringUTFChars(env,jstr,0);
  r = stat(cstr, &buf);	
  (*env)->ReleaseStringUTFChars(env,jstr,cstr);
	
  if ((r == 0) && S_ISDIR(buf.st_mode))
    return ((jboolean)1);
  else
    return ((jboolean)0);

}

/*
 * Class:     java_io_File
 * Method:    lastModified0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_io_File_lastModified0(JNIEnv *env, jobject this) {
  struct stat buf;
  int r;
  jobject jstr;
  const char * cstr;    
  
  if (!inited && !initializeFI(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  cstr=(*env)->GetStringUTFChars(env,jstr,0);
  r = stat(cstr, &buf);	
  (*env)->ReleaseStringUTFChars(env,jstr,cstr);
	
  return ((jlong)buf.st_mtime*(jlong)1000);
}


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


/*
 * Class:     java_io_File
 * Method:    mkdir0
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_mkdir0(JNIEnv *env, jobject this) {

  int r;
  jobject jstr;
  const char * cstr;    
  
  if (!inited && !initializeFI(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  cstr=(*env)->GetStringUTFChars(env,jstr,0);
  r = mkdir(cstr, 0777);	
  (*env)->ReleaseStringUTFChars(env,jstr,cstr);
  if (r != 0)
    return JNI_FALSE;
  else
    return JNI_TRUE;
}


#if 0
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
#endif

/*
 * Class:     java_io_File
 * Method:    list0
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_java_io_File_list0(JNIEnv *env, jobject this) {
  jobject jstr;
  const char * path;    
  DIR* dir;
  struct dirent* entry;
  struct dentry {
    struct dentry* next;
    char name[1];
  };
  struct dentry* dirlist;
  struct dentry* mentry;
  jobjectArray array;
  jclass clscls;
  int count;
  int i;
  

  if (!inited && !initializeFI(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  path=(*env)->GetStringUTFChars(env,jstr,NULL);

  
  /* XXX make part of jsyscall interface !? */
  dir = opendir(path);
  if (dir == NULL) { /* if path not a directory, return NULL. */
    (*env)->ReleaseStringUTFChars(env,jstr,path);
    return NULL;
  }
  
  dirlist = NULL;
  count = 0;
  /* XXX make part of jsyscall interface !? */
  while ((entry = readdir(dir)) != 0) {
    /* We skip '.' and '..' */
    if (strcmp(".", entry->d_name) == 0 ||
	strcmp("..", entry->d_name) == 0) {
      continue;
    }
    /** no pointers to garbage-collected memory; safe to use malloc. */
    mentry = malloc(sizeof(struct dentry) + _D_EXACT_NAMLEN(entry));
    if (!mentry) {  /* free memory and throw exception */
      (*env)->Throw(env, (*env)->FindClass(env, "java/lang/OutOfMemoryError"));
      goto error;
    }
    strcpy(mentry->name, entry->d_name);
    mentry->next = dirlist;
    dirlist = mentry;
    count++;
  }
  /* XXX make part of jsyscall interface !? */
  closedir(dir);
  clscls=(*env)->FindClass(env, "java/lang/String");
  if (!clscls) goto error; /* free memory and return */

  array = (*env)->NewObjectArray(env, count, clscls, NULL);
  if (!array) goto error; /* exception already set. */

  for (i = 0; i < count; i++) {
    jstring jstr;
    mentry = dirlist;
    dirlist = mentry->next;
    jstr=(*env)->NewStringUTF(env, mentry->name);
    (*env)->SetObjectArrayElement(env, array, i, jstr);
    free(mentry);
  }
  (*env)->ReleaseStringUTFChars(env,jstr,path);
  return (array);

 error:
  {
    jthrowable ex = (*env)->ExceptionOccurred(env);
    assert(ex!=NULL);

    while (dirlist) {
      mentry = dirlist;
      dirlist = dirlist->next;
      free(mentry);
    }

    (*env)->ExceptionClear(env);
    (*env)->ReleaseStringUTFChars(env,jstr,path);
    (*env)->Throw(env, ex);
    return NULL;
  }
}

/*
 * Class:     java_io_File
 * Method:    canonPath
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_io_File_canonPath(JNIEnv *env, jobject this, jstring jstr) {
  return jstr;
}

/*
 * Class:     java_io_File
 * Method:    isAbsolute
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_java_io_File_isAbsolute(JNIEnv *env, jobject this) {
  jobject jstr;
  const char * cstr;    
  jboolean jbool;
  
  if (!inited && !initializeFI(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  cstr=(*env)->GetStringUTFChars(env,jstr,0);
  jbool= (cstr[0]=='/') ? JNI_TRUE : JNI_FALSE;

  (*env)->ReleaseStringUTFChars(env,jstr,cstr);
  
  return jbool;
}

#ifdef WITH_TRANSACTIONS
#include "../transact/transact.h" /* for FNI_StrTrans2Str */
JNIEXPORT jlong JNICALL Java_java_io_File_length0_00024_00024withtrans
	(JNIEnv * env, jobject this, jobject commitrec)
{
  struct stat buf;
  int r;
  jobject jstr;
  const char * cstr;    
  
  if (!inited && !initializeFI(env)) return 0; /* exception occurred; bail */
  
  jstr=(*env)->GetObjectField(env, this, pathID);
  cstr=(*env)->GetStringUTFChars(env,FNI_StrTrans2Str(env, commitrec, jstr),0);
  r = stat(cstr, &buf);	
  (*env)->ReleaseStringUTFChars(env,jstr,cstr);
	

  if (r != 0) {
    return ((jlong)0);
  }
  return ((jlong)buf.st_size);
}
#endif /* WITH_TRANSACTIONS */
