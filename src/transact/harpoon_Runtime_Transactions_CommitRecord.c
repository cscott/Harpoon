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
    struct commitrec *cr = (struct commitrec *) FNI_UNWRAP(commitrec);
    jint s;
    if (cr==NULL) return COMMITTED;
    do {
	/* avoid the atomic operation if possible */
	if (WAITING != (s = cr->state)) return s;
	/* atomically set to ABORTED */
	compare_and_swap(&(cr->state), WAITING, ABORTED); /* atomic */
    } while (1);
}

/*
 * Class:     harpoon_Runtime_Transactions_CommitRecord
 * Method:    commit
 * Signature: (Lharpoon/Runtime/Transactions/CommitRecord;)I
 */
JNIEXPORT jint JNICALL Java_harpoon_Runtime_Transactions_CommitRecord_commit
    (JNIEnv *env, jclass cls, jobject commitrec) {
    struct commitrec *cr = (struct commitrec *) FNI_UNWRAP(commitrec);
    jint s;
    if (cr==NULL) return COMMITTED;
    do {
	/* avoid the atomic operation if possible */
	if (WAITING != (s = cr->state)) return s;
	/* atomically set to COMMITTED */
	compare_and_swap(&(cr->state), WAITING, COMMITTED); /* atomic */
    } while (1);
}
