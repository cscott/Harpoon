#include <assert.h>
#include <stdlib.h>

#include <jni.h>
#include <jni-private.h>

int name2class_compare(const void *key, const void *element) {
  const char *name = key;
  const struct FNI_name2class *n2c = element;
  return strcmp(name, n2c->name);
}
jclass FNI_FindClass(JNIEnv *env, const char *name) {
  const struct FNI_name2class * result =
    bsearch(name, &name2class_start, &name2class_end-&name2class_start,
	    sizeof(name2class_start), name2class_compare);
  if (result==NULL) {
    /* XXX: throw NoClassDefFoundError? */
    return NULL;
  }
  /* XXX: wrap the class object */
  return result->class_object;
}

int class2info_compare(const void *class_object, const void *element) {
  const struct FNI_class2info *c2i = element;
  return class_object - c2i->class_object;
}
struct FNI_classinfo *FNI_GetClassInfo(jclass clazz) {
  const struct FNI_class2info * result;
  assert(clazz!=NULL);
  /* XXX: unwrap clazz */
  result =
    bsearch(clazz, &class2info_start, &class2info_end - &class2info_start,
	    sizeof(class2info_start), class2info_compare);
  return (result==NULL) ? NULL : result->info;
}

struct name_and_sig {
  const char *name; const char *sig;
};
int name2method_compare(const void *key, const void *element) {
  const struct name_and_sig *ns = key;
  const struct _jmethodID *methodID = element;
  int r;
  r = strcmp(ns->name, methodID->name);
  return (r!=0) ? r : strcmp(ns->sig, methodID->desc);
}
jmethodID FNI_GetStaticMethodID(JNIEnv *env, jclass clazz,
				const char *name, const char *sig) {
  struct FNI_classinfo *info = FNI_GetClassInfo(clazz);
  struct name_and_sig ns = { name, sig };
  struct _jmethodID * result;
  result = (info==NULL) ? NULL :
    bsearch(&ns, info->methodinfo, info->methodend - info->methodinfo,
	    sizeof(struct _jmethodID), name2method_compare);
  if (result==NULL) {
    /* XXX: throw NoSuchMethodError */
    return NULL;
  }
  return result;
}

