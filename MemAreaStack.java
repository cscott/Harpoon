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
    /** A MemoryArea on the stack 
     */
    public MemoryArea entry;

    /** The "next" pointer for the stack.
     */
    public MemAreaStack next;

    /** Push the MemoryArea on the cactus stack. 
     */
    public MemAreaStack(MemoryArea entry, MemAreaStack next) {
	this.entry = entry;
	this.next = next;
    }

    public static MemAreaStack PUSH(MemoryArea entry, MemAreaStack next) {
	RealtimeThread.checkInit();
	RefCountArea ref = RefCountArea.refInstance();
	Object mas = null;
	if ((next != null) && (next.memoryArea == ref)) ref.INCREF(next);
	if ((entry != null) && (entry.memoryArea == ref)) ref.INCREF(entry);
	try {
	    mas = ref.newInstance(MemAreaStack.class, 
				  new Class[] { MemoryArea.class, 
						MemAreaStack.class },
				  new Object[] { entry, next});
	} catch (Exception e) {
	    NoHeapRealtimeThread.print(e.toString());
	    System.exit(-1);
	}
	mas.memoryArea = ref;
	return (MemAreaStack)mas;
    }

    public static MemAreaStack POP(MemAreaStack top) {
	MemAreaStack next = top.next;
	RefCountArea ref = RefCountArea.refInstance();
	if (top.memoryArea == ref) ref.DECREF(top);
	return next;
    }

    /** Get the MemAreaStack of the first occurance of the 
     *  MemoryArea mem, or null if it is not found.
     */
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
    
    /** Dump a description of each MemoryArea on the stack. 
     */
    public String toString() {
	if (next != null) {
	    return entry.toString()+"\n"+next.toString();
	} else {
	    return "";
	}
    }

}
