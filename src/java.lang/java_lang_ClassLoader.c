#include <jni.h>
#include "java_lang_Class.h" /* for Java_java_lang_Class_forName() */
#include "java_lang_ClassLoader.h"
#include <sys/stat.h> /* lstat */
#include <unistd.h> /* lstat, S_ISREG */
#include <string.h> /* strdup, strstr */
#include <assert.h>

/*
 * Class:     java_lang_ClassLoader
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_java_lang_ClassLoader_init
  (JNIEnv *env, jobject _this) {
  /* do we need to do anything here? */
}

#if 0
/*
 * Class:     java_lang_ClassLoader
 * Method:    defineClass0
 * Signature: (Ljava/lang/String;[BII)Ljava/lang/Class;
 */
/* should be able to use (*env)->DefineClass for this function */
JNIEXPORT jclass JNICALL Java_java_lang_ClassLoader_defineClass0
  (JNIEnv *, jobject _this, jstring, jbyteArray, jint, jint);

/*
 * Class:     java_lang_ClassLoader
 * Method:    resolveClass0
 * Signature: (Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_java_lang_ClassLoader_resolveClass0
  (JNIEnv *, jobject, jclass);
#endif

/*
 * Class:     java_lang_ClassLoader
 * Method:    findSystemClass0
 * Signature: (Ljava/lang/String;)Ljava/lang/Class;
 */
JNIEXPORT jclass JNICALL Java_java_lang_ClassLoader_findSystemClass0
  (JNIEnv *env, jobject _this, jstring name) {
  /* XXX: not sure if this is technically correct, but it will work. */
  return Java_java_lang_Class_forName(env, NULL, name);
}

static const char *find_system_resource(JNIEnv *env, jstring j_resname) {
  jstring j_clspath, j_filesep, j_pathsep, jstr;
  const char *c_resname, *c_clspath, *c_filesep, *c_pathsep, *c_result, *start;
  jclass syscls; jmethodID mid;
  c_result=NULL;
  /* initialize syscls and mid */
  syscls = (*env)->FindClass(env, "java/lang/System");
  if ((*env)->ExceptionOccurred(env)) goto bail0;
  mid = (*env)->GetStaticMethodID(env, syscls, "getProperty",
				  "(Ljava/lang/String;)Ljava/lang/String;");
  if ((*env)->ExceptionOccurred(env)) goto bail0;
  /* form a c-string from the resource name */
  c_resname = (*env)->GetStringUTFChars(env, j_resname, NULL);
  if (c_resname==NULL) goto bail0;
  /* get c-string version of java.class.path property */
  jstr = (*env)->NewStringUTF(env, "java.class.path");
  j_clspath = (*env)->CallStaticObjectMethod(env, syscls, mid, jstr);
  if (j_clspath==NULL) goto bail1;
  c_clspath = (*env)->GetStringUTFChars(env, j_clspath, NULL);
  /* get c-string version of path.separator property */
  jstr = (*env)->NewStringUTF(env, "path.separator");
  j_pathsep = (*env)->CallStaticObjectMethod(env, syscls, mid, jstr);
  if (j_pathsep==NULL) goto bail2;
  c_pathsep = (*env)->GetStringUTFChars(env, j_pathsep, NULL);
  /* get c-string version of file.separator property */
  jstr = (*env)->NewStringUTF(env, "file.separator");
  j_filesep = (*env)->CallStaticObjectMethod(env, syscls, mid, jstr);
  if (j_filesep==NULL) goto bail3;
  c_filesep = (*env)->GetStringUTFChars(env, j_filesep, NULL);

  /* OKAY! c_resname, c_clspath, c_filesep, and c_pathsep are all set. */
  /* iterate through path components */
  /* XXX: hackity hackity; doesn't handle ZIP/JARs in classpath */
  for (start = c_clspath; ; ) {
    struct stat st;
    char *end = strstr(start, c_pathsep);
    char buf[((end?end:start)-start)+strlen(c_filesep)+strlen(c_resname)+1];
    if (end==NULL) break;
    strncpy(buf, start, end-start);
    strcpy(buf+(end-start), c_filesep);
    strcpy(buf+(end-start)+strlen(c_filesep), c_resname);
    /* okay, try the path now in buf */
    if (lstat(buf, &st)==0 && S_ISREG(st.st_mode)) {
      /* this is it! */
      c_result = strdup(buf);
      break;
    }
    /* move start, try again. */
    start = end+strlen(c_pathsep);
  }

 bail4:
  (*env)->ReleaseStringUTFChars(env, j_filesep, c_filesep);
 bail3:
  (*env)->ReleaseStringUTFChars(env, j_pathsep, c_pathsep);
 bail2:
  (*env)->ReleaseStringUTFChars(env, j_clspath, c_clspath);
 bail1:
  (*env)->ReleaseStringUTFChars(env, j_resname, c_resname);
 bail0:
  (*env)->ExceptionClear(env);
  return c_result;
}

/*
 * Class:     java_lang_ClassLoader
 * Method:    getSystemResourceAsStream0
 * Signature: (Ljava/lang/String;)Ljava/io/InputStream;
 */
JNIEXPORT jobject JNICALL Java_java_lang_ClassLoader_getSystemResourceAsStream0
  (JNIEnv *env, jclass cls, jstring name) {
  const char *filename = find_system_resource(env, name);
  if (filename!=NULL) { /* create a new FileInputStream and return that */
    jclass fiscls = (*env)->FindClass(env, "java/io/FileInputStream");
    jmethodID mid = (*env)->GetMethodID(env, fiscls, "<init>",
					"(Ljava/lang/String;)V");
    jstring jfilename = (*env)->NewStringUTF(env, filename);
    return (*env)->NewObject(env, fiscls, mid, jfilename);
  }
  return NULL;
}

/*
 * Class:     java_lang_ClassLoader
 * Method:    getSystemResourceAsName0
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_java_lang_ClassLoader_getSystemResourceAsName0
  (JNIEnv *env, jclass cls, jstring name) {
  /* XXX cheat: claim resource is never found.
   * in reality, we should search the CLASSPATH for the given
   * resource, and return a URL string that will enable the
   * user to open it. */
  assert(0); /* should call find_system_resource and construct a URL */
  return NULL;
}
