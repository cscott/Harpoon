// TransactionAbortException.java, created Tue Nov  7 01:36:01 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime.Transactions;

/**
 * A <code>TransactionAbortException</code> is thrown to indicate
 * the forced-suicide of a transaction.  It
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TransactionAbortException.java,v 1.2 2002-02-25 21:06:48 cananian Exp $
 */
public class TransactionAbortException extends Exception {
    public final CommitRecord abortUpTo;

    /** Creates a <code>TransactionAbortException</code> that will
     *  abort all parent subtransactions up to that specified by
     *  <code>abortUpTo</code>.  If the parameter is <code>null</code>,
     *  the entire transaction is aborted. */
    public TransactionAbortException(CommitRecord abortUpTo) {
        this.abortUpTo = abortUpTo;
    }
    public String toString() {
	return "TransactionAbortException: abort up to "+abortUpTo;
    }
}
