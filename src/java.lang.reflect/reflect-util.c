/* utility methods for reflection */
#include <assert.h>
#include <string.h>
#include <jni.h>
#include "../java.lang/java_lang_Class.h"
#include "reflect-util.h"

/* return the class object corresponding to the first component of the
 * given descriptor. */
jclass REFLECT_parseDescriptor(JNIEnv *env, const char *desc) {
    const char *name, *sigptr=desc;
    switch (desc[0]) {
    case 'Z': name="boolean"; break;
    case 'B': name="byte"; break;
    case 'C': name="char"; break;
    case 'S': name="short"; break;
    case 'I': name="int"; break;
    case 'J': name="long"; break;
    case 'F': name="float"; break;
    case 'D': name="double"; break;
    case '[':
      while (*sigptr=='[') sigptr++;
    case 'L':
      if (*sigptr=='L') while (*sigptr!=';') sigptr++;
      {
	  char buf[2+sigptr-desc];
	  strncpy(buf, desc+((desc[0]=='L')?1:0), 2+sigptr-desc);
	  buf[1+sigptr-desc]='\0';
	  if (desc[0]=='L') buf[sigptr-desc-1]='\0';
	  return (*env)->FindClass(env, buf);
      }
    default: assert(0); /* illegal signature */
    }
    /* it's a primitive class */
    return Java_java_lang_Class_getPrimitiveClass
	(env, NULL, (*env)->NewStringUTF(env, name));
}

/* advance the given descriptor to the next component; returns NULL
 * if there are no more components. */
char *REFLECT_advanceDescriptor(char *sigptr) {
    switch (*sigptr) {
    case '[':
      while (*sigptr=='[') sigptr++;
    case 'L':
      if (*sigptr=='L') while (*sigptr!=';') sigptr++;
    default:
    }
    assert(*sigptr!='[' && *sigptr!='L');
    sigptr++; /* one step forward */
    if (*sigptr && *sigptr!=')') return sigptr;
    else return NULL;
}



