#ifndef INCLUDED_FNI_PROPERTIES_H
#define INCLUDED_FNI_PROPERTIES_H

#include "config.h"
#include <assert.h>
#include <stdlib.h> /* getenv, malloc, free */
#include <time.h> /* time,localtime for time zone information */
#include <unistd.h> /* getcwd */
#ifdef HAVE_UNAME
# include <sys/utsname.h>
#endif

/* prototype */
static void _putProperty(JNIEnv *env, jobject propobj, jmethodID methodID,
			 const char *ckey, const char *cvalue);

static inline
void fni_properties_init(JNIEnv *env, jobject propobj,
			 jboolean use_setProperty) {
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
    jmethodID methodID;
    char *ckey, *cvalue;
    int i;
    if (use_setProperty) /* JDK 1.2 and up only */
      methodID = (*env)->GetMethodID(env, propcls, "setProperty",
		   "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;");
    else /* JDK 1.1 and up */
      methodID = (*env)->GetMethodID(env, propcls, "put",
		   "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
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

#endif /* INCLUDED_FNI_PROPERTIES_H */
