#include <assert.h>
#include <stdlib.h>
#include <string.h>

#include <jni.h>
#include <jni-private.h>

int name2class_compare(const void *key, const void *element) {
  const char *name = key;
  const struct FNI_name2class *n2c = element;
  return strcmp(name, n2c->name);
}
jclass FNI_FindClass(JNIEnv *env, const char *name) {
  const struct FNI_name2class * result;
  assert(FNI_NO_EXCEPTIONS(env));
  assert(strchr(name, '.')==NULL /* all dots should be slashes! */);
  result =
    bsearch(name, name2class_start, name2class_end-name2class_start,
	    sizeof(*name2class_start), name2class_compare);
  if (result==NULL) {
    assert(strcmp(name, "java/lang/NoClassDefFoundError")!=0);
    FNI_ThrowNew(env, FNI_FindClass(env, "java/lang/NoClassDefFoundError"),
		 name);
    return NULL;
  }
  return FNI_WRAP(result->class_object);
}

/* FNI_GetClassInfo, FNI_GetFieldInfo, and FNI_GetMethodInfo definitions */
#define FNI_GETINFO(name, Name, type, rettype, accessor) \
static int name##2info_compare(const void *key, const void *element) {\
  const struct oobj *name##_object = key;\
  const struct FNI_##name##2info *x2i = element;\
  return name##_object - x2i->name##_object;\
}\
struct rettype *FNI_Get##Name##Info(type key) {\
  struct FNI_##name##2info * result;\
  assert(key!=NULL);\
  result =\
    bsearch(FNI_UNWRAP_MASKED(key),\
	    name##2info_start, name##2info_end - name##2info_start,\
	    sizeof(*name##2info_start), name##2info_compare);\
  return (result==NULL) ? NULL : result accessor ;\
}
FNI_GETINFO(class, Class, jclass, FNI_classinfo, /* info field */ -> info)
FNI_GETINFO(field, Field, jobject, FNI_field2info, /* no accessor */)
FNI_GETINFO(method,Method,jobject, FNI_method2info,/* no accessor */)

struct name_and_sig {
  const char *name; const char *sig;
};
int name2member_compare(const void *key, const void *element) {
  const struct name_and_sig *ns = key;
  const struct _jmethodID *methodID = element;
  int r;
  r = strcmp(ns->name, methodID->name);
  return (r!=0) ? r : strcmp(ns->sig, methodID->desc);
}
union _jmemberID *FNI_GetMemberID(jclass clazz,
				   const char *name, const char *sig) {
  struct FNI_classinfo *info = FNI_GetClassInfo(clazz);
  struct name_and_sig ns = { name, sig };
  union _jmemberID * result;
  return (info==NULL) ? NULL :
    bsearch(&ns, info->memberinfo, info->memberend - info->memberinfo,
	    sizeof(union _jmemberID), name2member_compare);
}

#define GETID(_name, rtype, extype) \
rtype FNI_Get##_name##ID(JNIEnv *env, jclass clazz, \
			const char *name, const char *sig) { \
  rtype result; \
  assert(FNI_NO_EXCEPTIONS(env)); \
  result = (rtype) FNI_GetMemberID(clazz, name, sig); \
  if (result==NULL) { \
    struct FNI_classinfo *info = FNI_GetClassInfo(clazz); \
    char msg[strlen(info->name)+strlen(name)+strlen(sig)+3]; \
    strcpy(msg, info->name); strcat(msg, "."); \
    strcat(msg, name); strcat(msg, " "), strcat(msg, sig); \
    FNI_ThrowNew(env, FNI_FindClass(env, extype), msg); \
    return NULL; \
  } \
  return result; \
}
GETID(Method, jmethodID, "java/lang/NoSuchMethodError")
GETID(StaticMethod, jmethodID, "java/lang/NoSuchMethodError")
GETID(Field, jfieldID, "java/lang/NoSuchFieldError")
GETID(StaticField, jfieldID, "java/lang/NoSuchFieldError")
