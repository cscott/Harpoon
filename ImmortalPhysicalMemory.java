// ImmortalPhysicalMemoryFactory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** An instance of <code>ImmortalPhysicalMemory</code> allows objects to be
 *  allocated from a range of physical memory with particular attributes,
 *  determined by their memory type. This memory area has the same restrictive
 *  set of assignment rules as <code>ImmoratMemory</code> memory areas, and
 *  may be used in any context where <code>ImmortalMemory</code> is appropriate.
 *  Objects allocated in immortal physical memory have a lifetime greater
 *  than the application as do objects allocated in immortal memory.
 */
public class ImmortalPhysicalMemory extends MemoryArea {
    private long base, size;
    private Runnable logic;

    /** */

    public ImmortalPhysicalMemory(Object type, long size)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	// TODO

	// This line inserted only to make everything compile!
	super(size);
    }

    public ImmortalPhysicalMemory(Object type, long base, long size)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, size);
	this.base = base;
    }

    public ImmortalPhysicalMemory(Object type, long base, long size,
				  Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, base, size);
	this.logic = logic;
    }

    public ImmortalPhysicalMemory(Object type, long size, Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.logic = logic;
    }

    public ImmortalPhysicalMemory(Object type, long base,
				  SizeEstimator size)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, size);
	this.base = base;
    }

    public ImmortalPhysicalMemory(Object type, long base,
				  SizeEstimator size, Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, base, size);
	this.logic = logic;
    }

    public ImmortalPhysicalMemory(Object type, SizeEstimator size)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size.getEstimate());
    }
    
    public ImmortalPhysicalMemory(Object type, SizeEstimator size,
				  Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.logic = logic;
    }


    // CONSTRUCTORS/METHODS NOT IN SPECS

    public ImmortalPhysicalMemory(long base, long size) {
	super(size);
	this.base = base;
	this.size = size;
    }

    /** */

    protected native void initNative(long sizeInBytes);
}
