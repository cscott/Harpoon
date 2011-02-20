/* helper functions for jni-invoke.c */
#include <string.h> /* for strcpy/strcat */
#include "transact/transjni.h" /* JNI support functions */

static jmethodID lookupTrans(JNIEnv *env, jmethodID mid, int isStatic) {
  jclass cls = FNI_WRAP(mid->reflectinfo->declaring_class_object);
  const char *suffix="$$withtrans";
  const char *crdesc="(Lharpoon/Runtime/Transactions/CommitRecord;";
  char newname[strlen(mid->name)+strlen(suffix)+1];
  char newdesc[strlen(mid->desc)+strlen(crdesc)+1];
  jmethodID result;
  strcpy(newname, mid->name); strcat(newname, suffix);
  assert(mid->desc[0]=='(');
  strcpy(newdesc, crdesc); strcat(newdesc, mid->desc+1);
  if (isStatic)
    result = (*env)->GetStaticMethodID(env, cls, newname, newdesc);
  else
    result = (*env)->GetMethodID(env, cls, newname, newdesc);
  assert(result);
  return result;
}

#define METHOD(methodID,isStatic) \
	(currTrans(env) ? lookupTrans(env,methodID,isStatic) : methodID)
#define S_OFFSET(methodID)	(METHOD(methodID,1/*static*/)->offset)
#define NV_OFFSET(methodID)	(METHOD(methodID,0/*nonvirtual*/)->offset)
#define V_OFFSET(methodID)	(METHOD(methodID,0/*virtual*/)->offset)

#define MAX_EXTRA_ARGS 1
