// ScopedMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>ScopedMemory</code> is an abstract class that 
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public abstract class ScopedMemory extends MemoryArea {
    protected Object portal;

    /** Create a new ScopedMemory of a certain maximum size. 
     */
    public ScopedMemory(long size) {
	super(size);
	portal = null;
	scoped = true;
    }
    
    /** Return the maximum size of this ScopedMemory. 
     */
    public long getMaximumSize() { 
	return size;
    }
    
    /** Get the portal object for this ScopedMemory. 
     */
    public Object getPortal() {
	return portal;
    }
    
    /** Set the portal object for this ScopedMemory.
     */
    public void setPortal(Object object) {
	portal = object;
    }
    
    /** Get the MemoryArea which contains this ScopedMemory for
     *  the current RealtimeThread.
     */
    public MemoryArea getOuterScope() {
	return RealtimeThread.currentRealtimeThread().outerScope(this);
    }

    /** Check to see if this ScopedMemory can have access to 
     *  the given object.
     */
    public void checkAccess(Object obj) { 
	//      Stats.addCheck();
	if (obj != null) {
	    MemoryArea target = getMemoryArea(obj);
	    if ((this != target) && target.scoped &&
		(!RealtimeThread.currentRealtimeThread()
		 .checkAccess(this, target))) {
		throwIllegalAssignmentError(obj, target);
	    }
	}	    
    }

    /** Cannot call this on a ScopedMemory (doesn't cleanup MemBlocks 
     *  appropriately).  Should never need to, since that'll cause
     *  an access violation according to the spec. 
     */
    protected void setupMemBlock(RealtimeThread rt) 
	throws IllegalAccessException {
	throw new IllegalAccessException();
    }
}
