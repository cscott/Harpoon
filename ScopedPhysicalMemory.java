// ScopedPhysicalMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>ScopedPhysicalMemory</code>
 * 
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ScopedPhysicalMemory extends ScopedMemory {
    private long base, size;

    /** */

    public ScopedPhysicalMemory(long base, long size) {
	super(size);
	this.base = base;
	this.size = size;
    }

    /** */

    protected native void initNative(long sizeInBytes);

    /** */

    protected native void newMemBlock(RealtimeThread rt);

    /** */

    public void checkAccess(Object obj) {
	if (obj instanceof ScopedPhysicalMemory) {
	    ScopedPhysicalMemory spm = (ScopedPhysicalMemory)obj;
	    if (!(((base <= (spm.base + spm.size)) && (spm.base <= base))||
		  (((base + size) <= (spm.base + spm.size)) &&
		   (spm.base <= (base + size))))) { // It doesn't overlap
		super.checkAccess(obj);
	    }
	}
    }
}
