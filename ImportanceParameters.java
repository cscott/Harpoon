// ImportanceParameters.java, created by cata
// Copyright (C) 2001 Catalin Francu <cata@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package javax.realtime;

public class ImportanceParameters extends PriorityParameters {

	private int importance;

	public ImportanceParameters(int priority, int importance) {
		super(priority);
		this.importance = importance;
	}

	public int getImportance() {
		return importance;
	}

	public void setImportance(int importance) {
		this.importance = importance;
	}

	public String toString() {
		return "ImportanceParameters: Priority = " + getPriority()
			+ ", Importance = " + getImportance();
	}
}
