#include <jni.h>
#include <jni-private.h>
#include "java_lang_System.h"
#include <sys/time.h>
#include <sys/utsname.h>
#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include "config.h"
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif


/* utility method */
static void _putProperty(JNIEnv *env, jobject propobj, jmethodID methodID,
			 char *ckey, char *cvalue);
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

/*
 * Class:     java_lang_System
 * Method:    arraycopy
 * Signature: (Ljava/lang/Object;ILjava/lang/Object;II)V
 */
JNIEXPORT void JNICALL Java_java_lang_System_arraycopy
  (JNIEnv *env, jclass syscls,
   jobject src, jint srcpos, jobject dst, jint dstpos,
   jint length) {
    jclass arrcls = (*env)->FindClass(env, "[Ljava/lang/Object;");
    jclass asecls = (*env)->FindClass(env, "java/lang/ArrayStoreException");
    jclass oobcls = (*env)->FindClass(env, "java/lang/ArrayIndexOutOfBoundsException");
    jsize srclen, dstlen;
    int isPrimitive=0;
    /* do checks */
    if (FNI_UNWRAP(src)->claz->component_claz==NULL) {
      (*env)->ThrowNew(env, asecls, "src not an array");
      return;
    }
    if (FNI_UNWRAP(dst)->claz->component_claz==NULL) {
      (*env)->ThrowNew(env, asecls, "dst not an array");
      return;
    }
    if ((*env)->IsInstanceOf(env, src, arrcls)==JNI_FALSE ||
	(*env)->IsInstanceOf(env, dst, arrcls)==JNI_FALSE ) {
      /* one or both is an array of primitive type... */
      if (FNI_UNWRAP(src)->claz !=
	  FNI_UNWRAP(dst)->claz ) {
	(*env)->ThrowNew(env, asecls, "primitive array types don't match");
	return;
      }
      isPrimitive = 1;
    }
    /* length checks */
    srclen = (*env)->GetArrayLength(env, (jarray) src);
    dstlen = (*env)->GetArrayLength(env, (jarray) dst);
    if ((srcpos < 0) || (dstpos < 0) || (length < 0) ||
	(srcpos+length > srclen) || (dstpos+length > dstlen)) {
      (*env)->ThrowNew(env, oobcls, "index out of bounds");
      return;
    }
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
      memcpy(((char *)&(_dst->element_start))+(dstpos*size),
	     ((char *)&(_src->element_start))+(srcpos*size),
	     size*length);
      return;
    } else {
      int i;
      for (i=0; i<length; i++) {
	jobject o = (*env)->GetObjectArrayElement(env, src, srcpos+i);
	(*env)->SetObjectArrayElement(env, dst, dstpos+i, o);
	if ((*env)->ExceptionOccurred(env)!=NULL) return;
      }
      return;
    }
}

/*
 * Class:     java_lang_System
 * Method:    identityHashCode
 * Signature: (Ljava/lang/Object;)I
 */
JNIEXPORT jint JNICALL Java_java_lang_System_identityHashCode
  (JNIEnv *env, jclass cls, jobject obj) {
    jobject_unwrapped oobj = FNI_UNWRAP(obj);
    return (jint) oobj->hashcode;
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
    jmethodID methodID = (*env)->GetMethodID(env, propcls, "setProperty",
		   "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
    struct utsname buf;
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
    /* user info */
    ckey = "user.name";
    cvalue = getenv("USER");
    _putProperty(env, propobj, methodID, ckey, cvalue);
    ckey = "user.home";
    cvalue = getenv("HOME");
    _putProperty(env, propobj, methodID, ckey, cvalue);
    {
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
    /* done */
    return propobj;
}

static void _putProperty(JNIEnv *env, jobject propobj, jmethodID methodID,
			 char *ckey, char *cvalue) {
  jstring key, value;
  if (cvalue==NULL) cvalue="";
  key = (*env)->NewStringUTF(env, ckey);
  value = (*env)->NewStringUTF(env, cvalue);
  (*env)->CallObjectMethod(env, propobj, methodID, key, value);
}
