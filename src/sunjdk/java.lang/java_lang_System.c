#include "config.h"
#ifdef WITH_HASHLOCK_SHRINK
# define GC_I_HIDE_POINTERS /* we need HIDE_POINTER from gc.h */
#endif /* WITH_HASHLOCK_SHRINK */

#include <jni.h>
#include <jni-private.h>
#include "java_lang_System.h"
#include "flexthread.h" /* for arrcls lock in arraycopy */
#include <time.h> /* time,localtime for time zone information */
#include <sys/time.h> /* gettimeofday */
#ifdef HAVE_UNAME
# include <sys/utsname.h>
#endif
#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif

#include "../../java.lang/system.h" /* useful library-indep implementations */

/* utility method */
static void _putProperty(JNIEnv *env, jobject propobj, jmethodID methodID,
			 const char *ckey, const char *cvalue);
/*
 * Class:     java_lang_System
 * Method:    setIn0
 * Signature: (Ljava/io/InputStream;)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_setIn0
  (JNIEnv *env, jclass syscls, jobject in) {
    jfieldID fid = (*env)->GetStaticFieldID(env, syscls, "in",
					    "Ljava/io/InputStream;");
    (*env)->SetStaticObjectField(env, syscls, fid, in);
}

/*
 * Class:     java_lang_System
 * Method:    setOut0
 * Signature: (Ljava/io/PrintStream;)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_setOut0
  (JNIEnv *env, jclass syscls, jobject out) {
    jfieldID fid = (*env)->GetStaticFieldID(env, syscls, "out",
					    "Ljava/io/PrintStream;");
    (*env)->SetStaticObjectField(env, syscls, fid, out);
}

/*
 * Class:     java_lang_System
 * Method:    setErr0
 * Signature: (Ljava/io/PrintStream;)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_setErr0
  (JNIEnv *env, jclass syscls, jobject err) {
    jfieldID fid = (*env)->GetStaticFieldID(env, syscls, "err",
					    "Ljava/io/PrintStream;");
    (*env)->SetStaticObjectField(env, syscls, fid, err);
}

/*
 * Class:     java_lang_System
 * Method:    currentTimeMillis
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_java_lang_System_currentTimeMillis
(JNIEnv *env, jclass clazz) {
  struct timeval tv; jlong retval;
  gettimeofday(&tv, NULL);
  retval = tv.tv_sec; /* seconds */
  retval*=1000; /* milliseconds */
  retval+= (tv.tv_usec/1000); /* adjust milliseconds & add them in */
  return retval;
}


#ifdef WITH_TRANSACTIONS
 /* transactions has its own versions of arraycopy */
#else /* !WITH_TRANSACTIONS */
/*
 * Class:     java_lang_System
 * Method:    arraycopy
 * Signature: (Ljava/lang/Object;ILjava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_arraycopy
  (JNIEnv *env, jclass syscls,
   jobject src, jint srcpos, jobject dst, jint dstpos,
   jint length) {
  fni_system_arraycopy(env, syscls, src, srcpos, dst, dstpos, length
}
#endif /* !WITH_TRANSACTIONS */

/*
 * Class:     java_lang_System
 * Method:    identityHashCode
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_System_identityHashCode
  (JNIEnv *env, jclass cls, jobject obj) {
  return fni_system_identityHashCode(env, cls, obj);
}

/*
 * Class:     java_lang_System
 * Method:    initProperties
 * Signature: (Ljava/util/Properties;)Ljava/util/Properties;
 */
JNIEXPORT jobject JNICALL Java_java_lang_System_initProperties
  (JNIEnv *env, jclass syscls, jobject propobj) {
    char *properties[] = {
      "java.version", "1.1.7",
      "java.vendor", "FLEX compiler group",
      "java.vendor.url", "http://flexc.lcs.mit.edu/",
#ifdef JAVA_HOME
      "java.home", JAVA_HOME,
#endif
      "java.class.version", "45.3",
      "file.separator", "/",
      "path.separator", ":",
      "line.separator", "\n",
      NULL, NULL
    };
    jclass propcls = (*env)->FindClass(env, "java/util/Properties");
#if 0 /* JDK 1.2 and up only */
    jmethodID methodID = (*env)->GetMethodID(env, propcls, "setProperty",
		   "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
#else /* JDK 1.1 and up */
    jmethodID methodID = (*env)->GetMethodID(env, propcls, "put",
		   "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
#endif
    char *ckey, *cvalue;
    int i;
    /* do all the easy (constant) properties first */
    for (i=0; properties[i]!=NULL; i+=2)
      _putProperty(env, propobj, methodID, properties[i], properties[i+1]);
    /* okay, now the tricky stuff... */
    ckey = "java.class.path";
    cvalue = getenv("CLASSPATH");
    if (cvalue==NULL) cvalue="."; /* default class path */
    _putProperty(env, propobj, methodID, ckey, cvalue);
    ckey = "java.home";
    cvalue = getenv("JAVA_HOME");
    if (cvalue!=NULL) _putProperty(env, propobj, methodID, ckey, cvalue);
    /* uname stuff... */
#ifdef HAVE_UNAME
    {
    struct utsname buf;
    if (uname(&buf)==0) {
      ckey = "os.name";
      cvalue = buf.sysname;
      _putProperty(env, propobj, methodID, ckey, cvalue);
      ckey = "os.arch";
      cvalue = buf.machine;
      _putProperty(env, propobj, methodID, ckey, cvalue);
      ckey = "os.version";
      cvalue = buf.release;
      _putProperty(env, propobj, methodID, ckey, cvalue);
    }
    }
#endif /* HAVE_UNAME */
    /* user info */
    ckey = "user.name";
    cvalue = getenv("USER");
    _putProperty(env, propobj, methodID, ckey, cvalue);
    ckey = "user.home";
    cvalue = getenv("HOME");
    _putProperty(env, propobj, methodID, ckey, cvalue);
#ifdef HAVE_GETCWD
    {
      /* note, as this is a (temporary) char array, it is safe to use
       * malloc instead of GC_malloc. (ie, there are no pointers in here) */
      int size=10;
      char *buf = malloc(size), *cwd;
      while ((cwd=getcwd(buf, size))==NULL) {
	free(buf); size*=2; buf=malloc(size);
      }
      ckey = "user.dir";
      cvalue = buf;
      _putProperty(env, propobj, methodID, ckey, cvalue);
      free(buf);
    }
#endif /* HAVE_GETCWD */
#ifdef HAVE_LOCALTIME
    { /* borrowed from Japhar */
      time_t t = time(NULL);
      struct tm *tminfo = localtime(&t);
      ckey = "user.timezone";
      /* XXX: on some unix systems (ie, mine) *both* time zone methods
       * below return 'EDT' during daylight savings time, which the java
       * libraries apparently don't recognize as a time zone. */
# ifdef HAVE_TM_ZONE
      _putProperty(env, propobj, methodID, ckey, tminfo->tm_zone);
# elif defined(HAVE_TZNAME)
      _putProperty(env, propobj, methodID, ckey,tzname[tminfo->tm_isdst]);
# endif
    }
#endif /* HAVE_LOCALTIME */
    /* done */
    return propobj;
}

static void _putProperty(JNIEnv *env, jobject propobj, jmethodID methodID,
			 const char *ckey, const char *cvalue) {
  jstring key, value;
  if (cvalue==NULL) cvalue="";
  key = (*env)->NewStringUTF(env, ckey); assert(key);
  value = (*env)->NewStringUTF(env, cvalue); assert(value);
  (*env)->CallObjectMethod(env, propobj, methodID, key, value);
  assert(!(*env)->ExceptionOccurred(env));
}
