// RawMemoryFloatAccess.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>RawMemoryFloatAccess</code> holds accessor methods for accessing
 *  a raw memory area by float and double types.
 */

public class RawMemoryFloatAccess extends RawMemoryAccess {
    
    private long base, size;
    private Runnable logic;

    // CONSTRUCTORS IN SPECS

    public RawMemoryFloatAccess(Object type, long size)
	throws SecurityException, OffsetOutOfBoundsException,
	       SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	// TODO

	// This line inserted only to make everything compile!
	super(type, size);
    }

    public RawMemoryFloatAccess(Object type, long base, long size)
	throws SecurityException, OffsetOutOfBoundsException,
	       SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.base = base;
    }


    // CONSTRUCTORS NOT IN SPECS

    protected RawMemoryFloatAccess(long base, long size) {
	super(base, size);
    }

    /** Constructor reserved for use by the memory object factory. 
     */
    protected RawMemoryFloatAccess(RawMemoryAccess memory, long base,
				   long size) {
	super(memory, base, size);
    }
    
    /** type - An object representing the type of the memory required
     *         (dma, shared) - used to define the base address and control
     *         the mapping.
     *  size - the size of the area in bytes.
     */
    public static RawMemoryFloatAccess createFloatAccess(Object type, 
							 long size) 
	throws SecurityException, OffsetOutOfBoundsException,
	       SizeOutOfBoundsException, UnsupportedPhysicalMemoryException
    {
	/** Completely bogus */
	return new RawMemoryFloatAccess(1000, size);
    }

    /** type - An object representing the type of the memory required
     *         (dma, shared) - used to define the base address and control
     *         the mapping.
     *  base - The physical memory address of the area.
     *  size - the size of the area in bytes.
     */
    public static RawMemoryFloatAccess createFloatAccess(Object type,
							 long base,
							 long size) 
	throws SecurityException, OffsetOutOfBoundsException,
	       SizeOutOfBoundsException, UnsupportedPhysicalMemoryException
    {
	/** Totally bogus */
	return new RawMemoryFloatAccess(base, size);
    }


    // METHODS IN SPECS

    /** Get the double at the given offset. */
    public double getDouble(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Get <code>number</code> double values starting at the given 
     *  <code>offset</code> in this, and assigns them into the
     *  <code>double</code> array starting at position <code>low</code>.
     */
    public void getDoubles(long offset, double[] doubles,
			   int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    doubles[low + i] = getDouble(offset + i);
    }
    
    /** Get the float at the given offset. */
    public float getFloat(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Get <code>number</code> float values sstarting at the give
     *  <code>offset</code> in this, and assign them into the byte
     *  array starting at position <code>low</code>.
     */
    public void getFloats(long offset, float[] floats,
			  int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    floats[low + i] = getFloat(offset + i);
    }

    /** Set the double at the given offset. */
    public void setDouble(long offset, double value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Set <code>number</code> double values starting at the given
     *  offset in this from the double array starting at position
     *  <code>low</code>.
     */
    public void setDoubles(long offset, double[] doubles,
			   int low, int number) 
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setDouble(offset + i, doubles[low + i]);
    }

    /** Set the float at the given offset. */
    public void setFloat(long offset, float value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Set <code>number</code> float values starting at the given
     *  offset in this from the float array starting at position
     *  <code>low</code>.
     */
    public void setFloats(long offset, float[] floats,  
			  int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setFloat(offset + i, floats[low + i]);
    }
}
