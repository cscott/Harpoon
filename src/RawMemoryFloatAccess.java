// RawMemoryFloatAccess.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** This class holds methods for accessing a raw memory area by
 *  float and double types. Implementations are required to
 *  implement this class if and only if the underlying Java
 *  Virtual Machine supports floating point data types.
 *  <p>
 *  Many of the constructors and methods in this class throw
 *  <code>OffsetOutOfBoundsException</code>. This exception means
 *  that the value given in the offset parameter is either negative
 *  or outside the memory area.
 *  <p>
 *  Many of the constructors and methods in this class throw
 *  <code>SizeOutOfBoundsException</code>. This exception means
 *  that the value given in the size parameter is either negative,
 *  larger than an allowable range, or would cause an accessor
 *  method to access an address outside of the memory area.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class RawMemoryFloatAccess extends RawMemoryAccess {
    
    private long base, size;
    private Runnable logic;

    /** Create a <code>RawMemoryFloatAccess</code> object using the given parameters.
     *
     *  @param type An <code>Object</code> representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address
     *              and control the mapping.
     *  @param size The size of the area in bytes.
     *  @throws java.lang.SecurityException The application doesn't have permissions
     *                                      to access physical memory or the given
     *                                      type of memory.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with
     *                                      a conflict.
     */
    public RawMemoryFloatAccess(Object type, long size)
	throws SecurityException, OffsetOutOfBoundsException,
	       SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	// TODO

	// This line inserted only to make everything compile!
	super(type, size);
    }

    /** Create a <code>RawMemoryFloatAccess</code> object using the given parameters.
     *
     *  @param type An <code>Object</code> representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address
     *              and control the mapping.
     *  @param base The physical memory address of the area.
     *  @param size The size of the area in bytes.
     *  @throws java.lang.SecurityException The application doesn't have permissions
     *                                      to access physical memory or the given
     *                                      type of memory.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with
     *                                      a conflict.
     */
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


    /** Gets the double at the given offset.
     *
     *  @param offset The offset t which to write the value.
     *  @return The double value.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid range of memory.
     */
    public double getDouble(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Gets <code>number</code> double values starting at the given 
     *  <code>offset</code> in this, and assigns them into the
     *  <code>double</code> array starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start reading.
     *  @param doubles The array into which the read items are placed.
     *  @param low The offset which is the starting point in the given
     *             array for the read items to be placed.
     *  @param number The number of items to read.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid range of memory.
     */
    public void getDoubles(long offset, double[] doubles,
			   int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    doubles[low + i] = getDouble(offset + i);
    }
    
    /** Gets the float at the given offset.
     *
     *  @param offset The offset at which to get the value.
     *  @return The float value.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid range or memory.
     */
    public float getFloat(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Gets <code>number</code> float values sstarting at the give
     *  <code>offset</code> in this, and assign them into the byte
     *  array starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start reading.
     *  @param floats The array into which the read items are placed.
     *  @param low The offset which is the starting point in the given array
     *             for the read items to be placed.
     *  @param number The number of items to read.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid range of memory.
     */
    public void getFloats(long offset, float[] floats,
			  int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    floats[low + i] = getFloat(offset + i);
    }

    /** Sets the double at the given offset.
     *
     *  @param offset The offset at which to set the value.
     *  @param value The value which will be written.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid range of memory.
     */
    public void setDouble(long offset, double value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Sets <code>number</code> double values starting at the given offset
     *  in this from the double array starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start writing.
     *  @param doubles The array from which the items are obtained.
     *  @param low The offset which is the starting point in the given array
     *             for the items to be obtained.
     *  @param number The number of items to write.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid range of memory.
     */
    public void setDoubles(long offset, double[] doubles,
			   int low, int number) 
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setDouble(offset + i, doubles[low + i]);
    }

    /** Sets the float at the given offset.
     *
     *  @param offset The offset at which to write the value.
     *  @param value The value which will be written.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid range of memory.
     */
    public void setFloat(long offset, float value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Set <code>number</code> float values starting at the given offset
     *  in this from the float array starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start writing.
     *  @param floats The array from which the items are obtained.
     *  @param low The offset which is the starting poing in the given array
     *             for the items to be obtained.
     *  @param number The number of items to write.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid range of memory.
     */
    public void setFloats(long offset, float[] floats,  
			  int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setFloat(offset + i, floats[low + i]);
    }
}
