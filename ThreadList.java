// PriorityScheduler.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

public class ThreadList {
	public long threadID;
	public ThreadList next;

	// Leaves threadID undefinded!
	public ThreadList() {
		this.next = null;
	}

	public ThreadList(long threadID) {
		this.threadID = threadID;
		this.next = null;
	}

	/******************************** METHODS **********************************/
	// Returns a new list that contains this as its second element
	public ThreadList addToFront(long threadID) {
		ThreadList l = new ThreadList(threadID);
		l.next = this;
		return l;
	}

	// ASSUMES: this is the last element of a list
	// RETURNS: the newly added element
	public ThreadList addToTail(long threadID) {
		this.next = new ThreadList(threadID);
		return this.next;
	}

	public String toString() {
		StringBuffer s = new StringBuffer("[");
		for (ThreadList l = this; l!=null; l = l.next)
			s.append(l.threadID + ",");
		s.append("]");
		return s.toString();
	}

}
