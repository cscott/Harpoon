// ImplHelper.java, created Wed Jul  9 11:36:53 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Runtime.Transactions;

/**
 * <code>ImplHelper</code> just contains stubs which we will later
 * substitute native code for.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ImplHelper.java,v 1.1 2003-07-16 01:17:03 cananian Exp $
 */
public abstract class ImplHelper {
    /** Sets the JNI "current transaction" state to the given transaction.
     *  Returns the previous value of the transaction state. */
    public static native CommitRecord setJNITransaction(CommitRecord cr);
}
