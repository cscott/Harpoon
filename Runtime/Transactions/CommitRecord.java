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
 * @version $Id: CommitRecord.java,v 1.1.2.2 2000-11-06 21:28:17 cananian Exp $
 */
public class CommitRecord {
    // enumerated constants
    private static class State { }
    public static final State WAITING = null;
    public static final State COMMITTED = new State();
    public static final State ABORTED = new State();
    // fields.
    /** The commit record that this is dependent on, if any. */
    public final CommitRecord parent;
    /** The 'state' of this record: initialized to WAITING, and
     *  write-once to either COMMITTED or ABORTED. */
    State state = WAITING;
    public CommitRecord(CommitRecord parent) {
	this.parent = parent;
    }

    /** Returns the 'state' of this record (including dependencies) */
    public State state() { return state(this); }
    /** 'StateP' procedure from write-up. */
    public static State state(CommitRecord c) {
	// XXX: may return 'WAITING' even if parent is aborted.
	for( ; c!=null; c = c.parent) {
	    State s = c.state; // fetch once (atomically?) to prevent race.
	    if (s != COMMITTED) return s;
	}
	return COMMITTED;
    }
    /** Abort this (sub)transaction, if possible.
     *  @return the final state of the (sub)transaction. 
     */
    public State abort() { return abort(this); }
    /** Commit this (sub)transaction, if possible.
     *  @return the final state of the (sub)transaction.
     */
    public State commit() { return commit(this); }
    /** Abort a transaction, if possible.
     *  @return the final state of the (sub)transaction. 
     */
    public static State abort(CommitRecord c) {
	if (c==null) return COMMITTED;
	synchronized(c) {
	    State s = c.state;
	    if (s == WAITING)
		s = c.state = ABORTED;
	    return s;
	}
    }
    /** Commit a transaction, if possible.
     *  @return the final state of the (sub)transaction.
     */
    public static State commit(CommitRecord c) {
	if (c==null) return COMMITTED;
	synchronized(c) {
	    State s = c.state;
	    if (s == WAITING)
		s = c.state = COMMITTED;
	    return s;
	}
    }
    /** A singly-linked list of <code>CommitRecord</code>s. */
    public static class List {
	public final CommitRecord transid;
	List next;
	public List(CommitRecord transid, List next) {
	    this.transid = transid; this.next = next;
	}
    }
}
