#include <jni.h>
#include "jni-private.h"

#include <assert.h>
#include <stdlib.h>
#include <string.h>
#include "config.h"
#ifdef WITH_DMALLOC
#include "dmalloc.h"
#endif

static jsize utf8length(const jchar *buf, jsize len);
static jsize toUTF8(const jchar *src, int srclen, char *dst);
static jsize fromUTF8(const char *src, jchar *dst);

/* Constructs a new java.lang.String object from an array of Unicode
 * characters. 
 *
 * Returns a Java string object, or NULL if the string cannot be constructed.
 */
jstring FNI_NewString(JNIEnv *env, const jchar *unicodeChars, jsize len) {
  jstring result;
  jclass strcls = (*env)->FindClass(env, "java/lang/String");
  jmethodID cid = (*env)->GetMethodID(env, strcls, "<init>", "([C)V");
  jcharArray ca = (*env)->NewCharArray(env, len);
  (*env)->SetCharArrayRegion(env, ca, 0, len, unicodeChars);
  result = (jstring) (*env)->NewObject(env, strcls, cid, ca);
  (*env)->DeleteLocalRef(env, ca);
  (*env)->DeleteLocalRef(env, strcls);
  return result;
}

/* Returns the length (the count of Unicode characters) of a Java string.
 */
jsize FNI_GetStringLength(JNIEnv *env, jstring string) {
  jclass strcls = (*env)->FindClass(env, "java/lang/String");
  jmethodID mid = (*env)->GetMethodID(env, strcls, "length", "()I");
  (*env)->DeleteLocalRef(env, strcls);
  return (*env)->CallIntMethod(env, string, mid);
}

/* Returns a pointer to the array of Unicode characters of the string.
 * This pointer is valid until ReleaseStringchars() is called. 
 *
 * If isCopy is not NULL, then *isCopy is set to JNI_TRUE if a copy is made;
 * or it is set to JNI_FALSE if no copy is made. 
 *
 * Returns a pointer to a Unicode string, or NULL if the operation fails.
 */
const jchar * FNI_GetStringChars(JNIEnv *env, jstring string, jboolean *isCopy)
{
  jclass strcls = (*env)->FindClass(env, "java/lang/String");
  jmethodID mid = (*env)->GetMethodID(env, strcls, "toCharArray", "()[C");
  jcharArray ca = (jcharArray) (*env)->CallObjectMethod(env, string, mid);
  jsize     len = (*env)->GetArrayLength(env, ca);
  /* safe to use malloc -- no pointers to gc memory inside jchar[] */
  jchar *result = malloc(len * sizeof(jchar));
  (*env)->GetCharArrayRegion(env, ca, 0, len, result);
  if (isCopy!=NULL) *isCopy=JNI_TRUE;
  (*env)->DeleteLocalRef(env, strcls);
  (*env)->DeleteLocalRef(env, ca);
  return result;
}

/* Informs the VM that the native code no longer needs access to chars.
 * The chars argument is a pointer obtained from string using GetStringChars().
 */
void FNI_ReleaseStringChars(JNIEnv *env, jstring string, const jchar *chars) {
  free((void*)chars);
}

/* Constructs a new java.lang.String object from an array of UTF-8 characters.
 *
 * Returns a Java string object, or NULL if the string cannot be constructed.
 */
jstring FNI_NewStringUTF(JNIEnv *env, const char *bytes) {
  jstring result;
  jclass strcls = (*env)->FindClass(env, "java/lang/String");
  jmethodID cid = (*env)->GetMethodID(env, strcls, "<init>", "([C)V");
  int       len = strlen(bytes);
  /* safe to use malloc -- no pointers to gc objects inside jchar[] */
#ifdef WITH_DMALLOC /* dmalloc doesn't like zero-length allocations */
  jchar *   buf = malloc(1+sizeof(jchar)*len);
#else
  jchar *   buf = malloc(sizeof(jchar)*len);
#endif
  jsize  newlen = fromUTF8(bytes, buf);
  jcharArray ca = (*env)->NewCharArray(env, newlen);
  (*env)->SetCharArrayRegion(env, ca, 0, newlen, buf);
  free(buf);
  result = (jstring) (*env)->NewObject(env, strcls, cid, ca);
  (*env)->DeleteLocalRef(env, strcls);
  (*env)->DeleteLocalRef(env, ca);
  return result;
}

/* Returns the UTF-8 length in bytes of a string. 
 */
jsize FNI_GetStringUTFLength(JNIEnv *env, jstring string) {
  jclass strcls = (*env)->FindClass(env, "java/lang/String");
  jmethodID mid = (*env)->GetMethodID(env, strcls, "toCharArray", "()[C");
  jcharArray ca = (jcharArray) (*env)->CallObjectMethod(env, string, mid);
  jsize     len = (*env)->GetArrayLength(env, ca);
  jchar    *buf = (*env)->GetCharArrayElements(env, ca, NULL);
  jsize  result = utf8length(buf, len);
  (*env)->ReleaseCharArrayElements(env, ca, buf, 0);
  (*env)->DeleteLocalRef(env, strcls);
  (*env)->DeleteLocalRef(env, ca);
  return result;
}

/* Returns a pointer to an array of UTF-8 characters of the string, or
 * NULL if the operation fails.  The returned array is valid until it is
 * released by ReleaseStringUTFChars(). 
 *
 * If isCopy is not NULL, then *isCopy is set to JNI_TRUE if a copy is made;
 * or it is set to JNI_FALSE if no copy is made. 
 */
const char* FNI_GetStringUTFChars(JNIEnv *env, jstring string,
				  jboolean *isCopy) {
  jclass strcls = (*env)->FindClass(env, "java/lang/String");
  jmethodID mid = (*env)->GetMethodID(env, strcls, "toCharArray", "()[C");
  jcharArray ca = (jcharArray) (*env)->CallObjectMethod(env, string, mid);
  jchar *   buf = (*env)->GetCharArrayElements(env, ca, NULL);
  jsize     len = (*env)->GetArrayLength(env, ca);
  jsize  newlen = utf8length(buf, len);
  /* safe to use malloc -- no pointers to gc objects inside char[] */
  char * result = malloc(sizeof(char)*(newlen+1));
  toUTF8(buf, len, result);
  result[newlen]='\0';
  (*env)->ReleaseCharArrayElements(env, ca, buf, 0);
  if (isCopy!=NULL) *isCopy=JNI_TRUE;
  (*env)->DeleteLocalRef(env, strcls);
  (*env)->DeleteLocalRef(env, ca);
  return result;
}

/* Informs the VM that the native code no longer needs access to utf.
 * The utf argument is a pointer derived from string using 
 * GetStringUTFChars(). 
 */
void FNI_ReleaseStringUTFChars(JNIEnv *env, jstring string, const char *utf) {
  free((void*)utf);
}

static jsize utf8length(const jchar *buf, jsize len) {
  jsize i=0, result=0;
  for (i=0; i<len; i++) {
    jchar c = buf[i];
    if (c == 0x0000) result+=2;
    else if (c >= 0x0001 && c <= 0x007F) result+=1;
    else if (c >= 0x0080 && c <= 0x07FF) result+=2;
    else if (c >= 0x0800 && c <= 0xFFFF) result+=3;
    else assert(0);
  }
  return result;
}
static jsize toUTF8(const jchar *src, int srclen, char *dst) {
  jsize i=0, r=0;
  for (i=0; i<srclen; i++) {
    jchar c = src[i];
    if (c >= 0x0001 && c <= 0x007F) {
      dst[r++]=(char)c;
    } else if (c==0x0000 ||
	       (c>= 0x0080 && c <= 0x07FF)) {
      dst[r++]=0xC0 | (c>>6);
      dst[r++]=0x80 | (c&0x3F);
    } else if (c >= 0x0800 && c <= 0xFFFF) {
      dst[r++]=0xE0 | (c>>12);
      dst[r++]=0x80 | ((c>>6)&0x3F);
      dst[r++]=0x80 | (c&0x3F);
    } else assert(0);
  }
  return r;
}
static jsize fromUTF8(const char *src, jchar *dst) {
  jsize i=0;
  while (*src!=0) {
    if ((*src & 0x80)==0) {
      dst[i++]=src[0];
      src+=1;
    } else if ((*src & 0xE0)==0xC0) {
      assert((src[1]&0xC0)==0x80);
      dst[i++]=(src[1]&0x3F) | (((jchar)src[0]&0x1F)<<6);
      src+=2;
    } else if ((*src & 0xF0)==0xE0) {
      assert((src[1]&0xC0)==0x80);
      assert((src[2]&0xC0)==0x80);
      dst[i++]=(src[2]&0x3F) | (((jchar)src[1]&0x3F)<<6) |
	(((jchar)src[0]&0x0F)<<12);
      src+=3;
    } else assert(0);
  }
  return i;
}
