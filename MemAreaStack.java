// MemAreaStack.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>MemAreaStack</code> can be used to create a tree of MemoryArea
 *  references to determine whether a ScopedMemory check fails.  This is
 *  the same cactus stack that is used to determine which MemoryArea x 
 *  RealtimeThread MemBlock to enter for the constructor of a .newArray'ed
 *  or a newInstance'd object.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

class MemAreaStack {
    public MemoryArea entry;
    public MemAreaStack next;

    public MemAreaStack() {
	next = null;
	entry = null;
    }

    public MemAreaStack(MemoryArea entry, MemAreaStack next) {
	this.entry = entry;
	this.next = next;
    }

    public MemAreaStack first(MemoryArea mem) {
	MemAreaStack current = this;
	
	while (current != null) {
	    if (current.entry == mem) {
		return current;
	    }
	    current = current.next;
	}

	return null;
    }

    public String toString() {
	if (next != null) {
	    return entry.toString()+"\n"+next.toString();
	} else {
	    return "";
	}
    }

}
