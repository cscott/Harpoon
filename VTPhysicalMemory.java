package javax.realtime;

/** An instance of <code>VTPhysicalMemory</code> allows objects to
 *  be allocated from a range of physical memory with particular
 *  attributes, determined by their memory type. This memory area
 *  has the same restrictive set of assignment rules as
 *  <code>ScopedMemory</code> memory areas, and the same performance
 *  restrictions as <code>VTMemory</code>.
 */
public class VTPhysicalMemory extends ScopedMemory {

    private long base, size;
    private Runnable logic;

    protected void initNative(long sizeInBytes) {
    }

    public VTPhysicalMemory(Object type, long size)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	// TODO

	// This line inserted only to make everything compile!
	super(size);
    }

    public VTPhysicalMemory(Object type, long base, long size)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, size);
	this.base = base;
    }

    public VTPhysicalMemory(Object type, long base, long size,
			    Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, base, size);
	this.logic = logic;
    }

    public VTPhysicalMemory(Object type, long size, Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.logic = logic;
    }

    public VTPhysicalMemory(Object type, long base, SizeEstimator size)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, size);
	this.base = base;
    }

    public VTPhysicalMemory(Object type, long base, SizeEstimator size,
			    Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, base, size);
	this.logic = logic;
    }

    public VTPhysicalMemory(Object type, SizeEstimator size)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size.getEstimate());
    }

    public VTPhysicalMemory(Object type, SizeEstimator size,
			    Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.logic = logic;
    }

    public String toString() {
	return "VTPhysicalMemory: " + super.toString();
    }
}

