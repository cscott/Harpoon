// PriorityParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public class PriorityParameters extends SchedulingParameters {

	private int priority;

	public PriorityParameters(int priority) {
		super();
		this.priority = priority;
	}

	public int getPriority() {
		return priority;
	}

	// setPriority()
	//   The book says this method should throw an exception if the priority
	//   is outside the allowed interval of the scheduler of any threads
	//   associated with these PriorityParameters. But how do I know what the
	//   associated threads are?
	//   I figure the same question applies to ImportanceParameters.
	public void setPriority(int priority)
	    throws IllegalArgumentException {
		this.priority = priority;
	}

	public String toString() {
		return "PriorityParameters: " + priority;
	}

}
