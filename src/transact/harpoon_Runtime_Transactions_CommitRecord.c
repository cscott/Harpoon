#include <assert.h>
#include <jni.h>
#include "harpoon_Runtime_Transactions_CommitRecord.h"
#include "transact.h"
#include "asm/atomicity.h" /* machine-specific atomic operation defs */

/*
 * Class:     harpoon_Runtime_Transactions_CommitRecord
 * Method:    state
 * Signature: (Lharpoon/Runtime/Transactions/CommitRecord;)I
 */
JNIEXPORT jint JNICALL Java_harpoon_Runtime_Transactions_CommitRecord_state
    (JNIEnv *env, jclass cls, jobject commitrec) {
    struct commitrec *cr = (struct commitrec *) FNI_UNWRAP(commitrec);
    assert(0);
}

/*
 * Class:     harpoon_Runtime_Transactions_CommitRecord
 * Method:    abort
 * Signature: (Lharpoon/Runtime/Transactions/CommitRecord;)I
 */
JNIEXPORT jint JNICALL Java_harpoon_Runtime_Transactions_CommitRecord_abort
    (JNIEnv *env, jclass cls, jobject commitrec) {
    return AbortCR((struct commitrec *) FNI_UNWRAP(commitrec) );
}

/*
 * Class:     harpoon_Runtime_Transactions_CommitRecord
 * Method:    commit
 * Signature: (Lharpoon/Runtime/Transactions/CommitRecord;)I
 */
JNIEXPORT jint JNICALL Java_harpoon_Runtime_Transactions_CommitRecord_commit
    (JNIEnv *env, jclass cls, jobject commitrec) {
    return CommitCR((struct commitrec *) FNI_UNWRAP(commitrec) );
}
