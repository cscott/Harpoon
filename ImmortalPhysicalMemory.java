// ImmortalPhysicalMemoryFactory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>ImmortalPhysicalMemory</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class ImmortalPhysicalMemory extends MemoryArea {
    private long base, size;

    /** */

    public ImmortalPhysicalMemory(long base, long size) {
	super(size);
	this.base = base;
	this.size = size;
    }

    /** */

    protected native void initNative(long sizeInBytes);
}
