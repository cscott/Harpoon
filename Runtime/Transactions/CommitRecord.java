// CommitRecord.java, created Fri Oct 27 16:59:59 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime.Transactions;

/**
 * A <code>CommitRecord</code> keeps track of the status of a transaction.
 * It is initialized in the <code>WAITING</code> state and can be updated
 * exactly once to either COMMITTED or ABORTED.  Transactions can be nested;
 * therefore <code>CommitRecord</code>s can be dependent on other commit
 * records.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CommitRecord.java,v 1.2 2002-02-25 21:06:48 cananian Exp $
 */
public class CommitRecord {
    // enumerated constants
    public final static int WAITING=0;
    public final static int COMMITTED=1;
    public final static int ABORTED=2;
    // fields.
    /** The commit record that this is dependent on, if any. */
    public final CommitRecord parent;
    /** The 'state' of this record: initialized to WAITING, and
     *  write-once to either COMMITTED or ABORTED. */
    private int state = WAITING;

    /** Constructor method. */
    public static CommitRecord newTransaction(CommitRecord parent) {
	return new CommitRecord(parent, 0);
    }
    /** Returns a new commit record suitable for retrying this transaction.
     * @exception TransactionAbortException if this transaction has been
     * retried too many times.
     */
    public final CommitRecord retryTransaction()
	throws TransactionAbortException {
	if (retry_count > 2) throw new TransactionAbortException(parent);
	try {
	    Thread.sleep(retry_count); // sleep for a few milliseconds.
	} catch (InterruptedException ex) { /* wake up */ }
	return new CommitRecord(this.parent, retry_count+1);
    }
    public final void commitTransaction() throws TransactionAbortException {
	if (this.commit()!=COMMITTED)
	    throw new TransactionAbortException(this);
    }

    /** Private constructor. */
    private CommitRecord(CommitRecord parent, int retry_count) {
	this.parent = parent;
	this.retry_count = retry_count;
    }
    /** Keep track of how many times this transaction's been retried. */
    private final int retry_count;

    /** Returns the 'state' of this record (including dependencies) */
    public int state() { return state(this); }

    /** 'StateP' procedure from write-up. */
    public static native int state(CommitRecord c);

    /** Abort this (sub)transaction, if possible.
     *  @return the final state of the (sub)transaction. 
     */
    public int abort() { return abort(this); }
    /** Commit this (sub)transaction, if possible.
     *  @return the final state of the (sub)transaction.
     */
    public int commit() { return commit(this); }

    /** Abort a transaction, if possible.
     *  @return the final state of the (sub)transaction. 
     */
    public static native int abort(CommitRecord c);

    /** Commit a transaction, if possible.
     *  @return the final state of the (sub)transaction.
     */
    public static native int commit(CommitRecord c);
}
