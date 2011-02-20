#include <assert.h>
#include <jni.h>
#include <jni-private.h>
#include "harpoon_Runtime_Transactions_ImplHelper.h"
#include "transact.h"
#include "transjni.h" /* JNI transaction helpers */

/*
 * Class:     harpoon_Runtime_Transactions_ImplHelper
 * Method:    setJNITransaction
 * Signature: (Lharpoon/Runtime/Transactions/CommitRecord;)Lharpoon/Runtime/Transactions/CommitRecord;
 */
JNIEXPORT jobject JNICALL 
Java_harpoon_Runtime_Transactions_ImplHelper_setJNITransaction
    (JNIEnv *env, jclass cls, jobject commitrec) {
  // this will probably need serious looking at, if we were to use a
  // precise garbage collector.
  return FNI_WRAP
    (&(setCurrTrans(env, (struct commitrec *) FNI_UNWRAP_MASKED(commitrec))->
       header));
}
