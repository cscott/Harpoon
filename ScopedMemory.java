// ScopedMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>ScopedMemory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public abstract class ScopedMemory extends MemoryArea {
    protected Object portal;

    /** */

    public ScopedMemory(long size) {
	super(size);
	portal = null;
	scoped = true;
    }
    
    /** */

    public long getMaximumSize() { 
	return size;
    }
    
    /** */

    public Object getPortal() {
	return portal;
    }
    
    /** */

    public void setPortal(Object object) {
	portal = object;
    }
    
    /** */

    public MemoryArea getOuterScope() {
	return RealtimeThread.currentRealtimeThread().outerScope(this);
    }

    public void checkAccess(Object obj) { 
	//      Stats.addCheck();
	if (obj != null) {
	    MemoryArea target = getMemoryArea(obj);
	    if ((this != target) && target.scoped &&
		(!RealtimeThread.currentRealtimeThread()
		 .checkAccess(this, target))) {
  		throw new IllegalAssignmentError();	    
//		java_lang_Brokenness++;
	    }
	}	    
    }
}
