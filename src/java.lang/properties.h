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

/* extra properties defined on the command-line. */
extern struct property_list {
  char *key, *value;
  struct property_list *next;
} *extra_properties;

/* for stringifying preprocessor #defines */
#define stringify(x) stringify2(x)
#define stringify2(x) #x

/* prototype */
static void _putProperty(JNIEnv *env, jobject propobj, jmethodID methodID,
			 const char *ckey, const char *cvalue);

static inline
void fni_properties_init(JNIEnv *env, jobject propobj,
			 jboolean use_setProperty) {
    char *properties[] = {
      "java.version", "1.1.7", /* this should really be the FLEX version */
      "java.vendor", "FLEX compiler group",
      "java.vendor.url", "http://flexc.lcs.mit.edu/",
#ifdef JAVA_HOME
      "java.home", JAVA_HOME,
#endif
      "java.vm.specification.name", "Java(tm) Virtual Machine Specification",
      "java.vm.specification.vendor", "Sun Microsystems Inc.",
      "java.vm.specification.version", "1.1",
      "java.vm.name", "flexrun",
      "java.vm.vendor", "FLEX compiler group",
      "java.vm.version", stringify(VERSION),
      "java.specification.name", "Java Platform API Specification",
      "java.specification.vendor", "Sun Microsystems Inc.",
      "java.specification.version", "1.4", /* optimistically */
      "java.class.version", "48.0",
      "java.library.path", ".", /* XXX: get from environment? */
      "java.io.tmpdir", "/tmp",
      "file.separator", "/",
      "path.separator", ":",
      "line.separator", "\n",
#ifdef CLASSPATH_VERSION
      "java.library.version", stringify(CLASSPATH_VERSION),
#endif
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
      errno = 0; /* Success */
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
    /* extra properties defined on the command line. */
    while (extra_properties!=NULL) {
      struct property_list *npl = extra_properties->next;
      printf("Extra property: %s %s\n", extra_properties->key, extra_properties->value);
      _putProperty(env, propobj, methodID,
		   extra_properties->key, extra_properties->value);
      free(extra_properties->key);
      free(extra_properties->value);
      free(extra_properties);
      extra_properties = npl;
    }
    /* done */
    return;
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
