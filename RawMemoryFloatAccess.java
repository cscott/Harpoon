// RawMemoryFloatAccess.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>RawMemoryFloatAccess</code> holds accessor methods for accessing
 *  a raw memory area by float and double types.
 */

public class RawMemoryFloatAccess extends RawMemoryAccess {
    
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

    /** Get the double at the given offset. 
     */
    public native double getDouble(long offset)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Get number double values starting at the given offset in this,
     *  and assigns them into the double array starting at position low.
     */
    public native void getDoubles(long offset, double[] doubles,
				  int low, int number)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;
    
    /** Get the float at the given offset.
     */
    public native float getFloat(long offset)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Get number float values starting at the given offset in this and
     *  assign them into the float array starting at position low.
     */
    
    public native void getFloats(long offset, float[] floats,
				 int low, int number)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set the double at the given offset.
     */

    public native void setDouble(long offset, double value)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set number double values starting at the given offset in this,
     *  from the double array starting at position low.
     */

    public native void setDoubles(long offset, double[] doubles,
				  int low, int number) 
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set the float at the given offset.
     */

    public native void setFloat(long offset, float value)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set number float values starting at the given offset in this
     *  from the float array starting at position low.
     */

    public native void setFloats(long offset, float[] floats,  
				 int low, int number)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;    
}
