#include <jni.h>
#include <jni-private.h>
#include "java_lang_System.h"
#include "config.h"
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
  struct timeval tv; struct timezone tz; jlong retval;
  gettimeofday(&tv, &tz);
  retval = tv.tv_sec; /* seconds */
  retval*=1000; /* milliseconds */
  retval+= (tv.tv_usec/1000); /* adjust milliseconds & add them in */
  return retval;
}

/* factor out arraycopy checks to make variant impls easier. */
int do_arraycopy_checks(JNIEnv *env, jobject src, jint srcpos,
			jobject dst, jint dstpos, jint length) {
    jsize srclen, dstlen;
    int isPrimitive=0;
    static jclass arrcls=NULL;
    FLEX_MUTEX_DECLARE_STATIC(arrcls_lock);

    /* initialize arrcls */
    if (!arrcls) {
      FLEX_MUTEX_LOCK(&arrcls_lock);
      if (!arrcls) { /* double-check after aquiring lock */
	arrcls = (*env)->NewGlobalRef
	  (env, (*env)->FindClass(env, "[Ljava/lang/Object;"));
      }
      FLEX_MUTEX_UNLOCK(&arrcls_lock);
    }

    /* do checks */
    if (src==NULL || dst==NULL) {
      /* throw NullPointerException */
      jclass nulcls = (*env)->FindClass(env, "java/lang/NullPointerException");
      jmethodID methodID=(*env)->GetMethodID(env, nulcls, "<init>", "()V");
      (*env)->Throw(env, (*env)->NewObject(env, nulcls, methodID));
      return -1;
    }
    if (FNI_UNWRAP(src)->claz->component_claz==NULL) {
      jclass asecls = (*env)->FindClass
	(env, "java/lang/ArrayStoreException");
      (*env)->ThrowNew(env, asecls, "src not an array");
      return -1;
    }
    if (FNI_UNWRAP(dst)->claz->component_claz==NULL) {
      jclass asecls = (*env)->FindClass
	(env, "java/lang/ArrayStoreException");
      (*env)->ThrowNew(env, asecls, "dst not an array");
      return -1;
    }
    if ((*env)->IsInstanceOf(env, src, arrcls)==JNI_FALSE ||
	(*env)->IsInstanceOf(env, dst, arrcls)==JNI_FALSE ) {
      /* one or both is an array of primitive type... */
      if (FNI_UNWRAP(src)->claz !=
	  FNI_UNWRAP(dst)->claz ) {
	jclass asecls = (*env)->FindClass
	  (env, "java/lang/ArrayStoreException");
	(*env)->ThrowNew(env, asecls, "primitive array types don't match");
	return -1;
      }
      isPrimitive = 1;
    }
    /* length checks */
    srclen = (*env)->GetArrayLength(env, (jarray) src);
    dstlen = (*env)->GetArrayLength(env, (jarray) dst);
    if ((srcpos < 0) || (dstpos < 0) || (length < 0) ||
	(srcpos+length > srclen) || (dstpos+length > dstlen)) {
      jclass oobcls = (*env)->FindClass
	(env,"java/lang/ArrayIndexOutOfBoundsException");
      (*env)->ThrowNew(env, oobcls, "index out of bounds");
      return -1;
    }
    return isPrimitive;
}

#ifndef WITH_TRANSACTIONS /* transactions has its own versions of arraycopy */
/*
 * Class:     java_lang_System
 * Method:    arraycopy
 * Signature: (Ljava/lang/Object;ILjava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_arraycopy
  (JNIEnv *env, jclass syscls,
   jobject src, jint srcpos, jobject dst, jint dstpos,
   jint length) {

    int isPrimitive=do_arraycopy_checks(env, src, srcpos, dst, dstpos, length);
    if (isPrimitive<0) return; /* exception occurred. */

    /* for primitive array, we're all set: */
    if (isPrimitive) {
      struct aarray *_src, *_dst;
      int size=0;
      assert(FNI_GetClassInfo(FNI_GetObjectClass(env, src))->name[0]=='[');
      switch(FNI_GetClassInfo(FNI_GetObjectClass(env, src))->name[1]) {
      case 'Z': size = sizeof(jboolean); break;
      case 'B': size = sizeof(jbyte); break;
      case 'C': size = sizeof(jchar); break;
      case 'S': size = sizeof(jshort); break;
      case 'I': size = sizeof(jint); break;
      case 'J': size = sizeof(jlong); break;
      case 'F': size = sizeof(jfloat); break;
      case 'D': size = sizeof(jdouble); break;
      default: assert(0); /* what kind of primitive array is this? */
      }
      _src=(struct aarray*) FNI_UNWRAP(src);
      _dst=(struct aarray*) FNI_UNWRAP(dst);
      /* note: we use memmove to allow the areas to overlap. */
      memmove(((char *)&(_dst->element_start))+(dstpos*size),
	     ((char *)&(_src->element_start))+(srcpos*size),
	     size*length);
      return;
    } else {
      /* check for overlap */
      int backward = ( FNI_IsSameObject(env, src, dst) && srcpos < dstpos );
      int i = backward ? (length-1) : 0;
      while (backward ? (i >= 0) : (i < length)) {
	jobject o = (*env)->GetObjectArrayElement(env, src, srcpos+i);
	(*env)->SetObjectArrayElement(env, dst, dstpos+i, o);
	if ((*env)->ExceptionOccurred(env)!=NULL) return;
	if (backward) i--; else i++;
      }
      return;
    }
}
#endif /* !WITH_TRANSACTIONS */

/*
 * Class:     java_lang_System
 * Method:    identityHashCode
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_System_identityHashCode
  (JNIEnv *env, jclass cls, jobject obj) {
    jobject_unwrapped oobj = FNI_UNWRAP(obj);
    ptroff_t hashcode = oobj->hashunion.hashcode;
    if ((hashcode & 1) == 0) hashcode = oobj->hashunion.inflated->hashcode;
    return (jint) (hashcode>>2);
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
    _putProperty(env, propobj, methodID, ckey, cvalue);
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
    { /* borrowed from Japahar */
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
  key = (*env)->NewStringUTF(env, ckey);
  value = (*env)->NewStringUTF(env, cvalue);
  (*env)->CallObjectMethod(env, propobj, methodID, key, value);
}
