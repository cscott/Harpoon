// RawMemoryAccess.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** An instance of <code>RawMemoryAccess</code> models a range of
 *  physical memory as a fixed sequence of bytes. A full complement
 *  of accessor methods allow the contents of the physical area to
 *  be accessed through offsets from the base, interpreted as byte,
 *  short, int, or long data values or as arrays of these types.
 *  <p>
 *  Whether the offset addresses the high-order of low-order byte
 *  is based on the value of the <code>BYTE_ORDER</code> static
 *  boolean variable in class <code>RealtimeSystem</code>.
 *  The <code>RawMemoryAccess</code> class allows a real-time
 *  program to implement device drivers, memory-mapped I/O, flash
 *  memory, battery-backed RAM, and similar low-level software.
 *  <p>
 *  A raw memory area cannot contain references to Java objects.
 *  Such a capability would be unsafe (since it could be used to
 *  defeat Java's type checking) and error-prone (since it is
 *  sensitive to the specific representational choices made by
 *  the Java compiler).
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
 *  <p>
 *  Unlike other integral parameters in this chapter, negative
 *  values are valid for <code>byte, short, int</code> and
 *  <code>long</code> values that are copied in and out of memory
 *  by the <code>set</code> and <code>get</code> methods of this class.
 */
public class RawMemoryAccess {

    private long base, size;
    private Runnable logic;

    /** Construct an instance of <code>RawMemoryAccess</code> with the given parameters.
     *
     *  @param type An <code>Object</code> representing the type of memory required.
     *              Used to define the base address and control the mapping.
     *  @param size The size of the area in bytes.
     *  @throws java.lang.SecurityException The application doesn't have permissions
     *                                      to access physical memory or the given
     *                                      type of memory.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardwre does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with
     *                                      a conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public RawMemoryAccess(Object type, long size)
	throws SecurityException, OffsetOutOfBoundsException,
	       SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	// TODO
    }

    /** Construct an instance of <code>RawMemoryAccess</code> with the given parameters.
     *
     *  @param type An <code>Object</code> representing the type of memory required.
     *              Used to define the base address and control the mapping.
     *  @param base The physical memory address of the region.
     *  @param size The size of the area in bytes.
     *  @throws java.lang.SecurityException The application doesn't have permissions
     *                                      to access physical memory or the given
     *                                      type of memory.
     *  @throws OffsetOutOfBoundsException The address is invalid.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardwre does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with
     *                                      a conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public RawMemoryAccess(Object type, long base, long size)
	throws SecurityException, OffsetOutOfBoundsException,
	       SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.base = base;
    }


    // CONSTRUCTORS(?) NOT IN SPECS

    protected RawMemoryAccess(long base, long size) {
	
    }

    /** Constructor reserved for use by the memory object factory. */
    protected RawMemoryAccess(RawMemoryAccess memory, long base, long size) {

    }

    public static RawMemoryAccess create(Object type, long size) {
	/** Completely bogus */
	return new RawMemoryAccess(100, size);
    }

    public static RawMemoryAccess create(Object type, long base,
					 long size) {
	return new RawMemoryAccess(base, size);
    }


    // METHODS IN SPECS
    
    /** Get the byte at the given offset.
     *
     *  @param offset The offset at which to read the byte.
     *  @return The byte read.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public byte getByte(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Gets <code>nunber</code> bytes starting at the given offset and
     *  assign them to the byte array starting at the position
     *  <code>low</code>.
     *
     *  @param offset The offset at which to start reading.
     *  @param bytes The array into which the read items are placed.
     *  @param low the offset which is the starting point in the given array for
     *             the read items to be placed.
     *  @param number The number of items to read.
     *  @return The integer read.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public void getBytes(long offset, byte[] bytes, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    bytes[low + i] = getByte(offset + i);
    }

    /** Gets the <code>int</code> at the given offset.
     *
     *  @param offset The offset at which to read the integer.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public int getInt(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Gets <code>number</code> ints starting at the given offset and
     *  assign them to the int array passed at position <code>low</code>.
     *
     *  @param offset The offset at which to start reading.
     *  @param ints The array into which the read items are placed.
     *  @param low The offset which is the starting point in the given array for
     *             the read items to be placed.
     *  @param number The number of items to read.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public void getInts(long offset, int[] ints, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    ints[low + i] = getByte(offset + i);
    }

    /** Gets the <code>long</code> at the given offset.
     *
     *  @param offset The offset at which to read the long.
     *  @return The long read.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public long getLong(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Gets <code>number</code> longs starting at the given offset and assign
     *  them to the long array passed starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start reading.
     *  @param longs The array into which the read items are placed.
     *  @param low The offset which is the starting point in the given array
     *             for the read items to be placed.
     *  @param number The number of items to read.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public void getLongs(long offset, long[] longs, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    longs[low + i] = getLong(offset + i);
    }

    /** Gets the virtual memory location at which the memory region is mapped.
     *
     *  @return The virtual address to which this is mapped (for reference purposes).
     *          Same as the base address if virtual memory is not supported.
     */
    public long getMappedAddress() {
	// TODO

	return 0;
    }

    /** Gets the <code>short</code> at the given offset.
     *
     *  @param offset The offset at which to read the short.
     *  @return The short read.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public short getShort(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Gets <code>number</code> shorts starting at the give offset and assign
     *  them to the short array passed starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start reading.
     *  @param shorts The array into which the read items are placed.
     *  @param low The offset which is the starting point in the given array for
     *             the read items to be placed.
     *  @param number The number of items to read.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public void getShorts(long offset, short[] shorts, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    shorts[low + i] = getShort(offset + i);
    }

    /** Maps the physical memory range into virtual memory. No-op if the system
     *  doesn't support virtual memory.
     */
    public long map() {
	// TODO
	
	return 0;
    }

    /** Maps the physical memory range into virtual memory at the specified location.
     *  No-op if the system doesn't support virtual memory.
     *
     *  @param base The location to map at the virtual memory space.
     *  @return The starting point of the virtual memory.
     */
    public long map(long base) {
	// TODO

	return 0;
    }
    
    /** Maps the physical memory range into virtual memory. No-op if the system
     *  doesn't support virtual memory.
     *
     *  @param base The location to map at the virtual memory space.
     *  @param size The size of the block to map in.
     *  @return The starting point of the virtual memory.
     */
    public long map(long base, long size) {
	// TODO

	return 0;
    }

    /** Sets the byte at the given offset.
     *
     *  @param offset The offset at which to write the byte.
     *  @param value The byte to write.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public void setByte(long offset, byte value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Sets <code>number</code> bytes starting at the give offset from
     *  the byte array passed starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start writing.
     *  @param bytes The array from which the items are obtained.
     *  @param low The offset which is the starting point in the given array
     *             for the items to be obtained.
     *  @param number The number of items to write.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public void setBytes(long offset, byte[] bytes, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setByte(offset + i, bytes[low + i]);
    }

    /** Sets the <code>int</code> at the given offset.
     *
     *  @param offset The offset at which to write the integer.
     *  @param value The integer to write.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends into
     *                                   an invalid address range.
     */
    public void setInt(long offset, int value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Sets <code>number</code> ints starting at the given offset from
     *  the int array passed starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start writing.
     *  @param ints The array from which the itmes are obtained.
     *  @param low The offset which is the starting point in the given array
     *             for the items to be obtained.
     *  @param number The number of items to write.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid address range.
     */
    public void setInts(long offset, int[] ints, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setInt(offset + i, ints[low + i]);
    }

    /** Sets the <code>long</code> at the given offset.
     *
     *  @param offset The offset at which to write the long.
     *  @param value The long to write.
     *  @throws OffsetOutOfBoundsException The offset in invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid address range.
     */
    public void setLong(long offset, long value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Sets <code>number</code> longs starting at the given offset from
     *  the long array passed starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start writing.
     *  @param longs The array from which the items are obtained.
     *  @param low The offset which is the starting point in the given
     *             array for the items to be obtained.
     *  @param number The number of items to write.
     *  @throws OffsetOutOfboundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid address range.
     */
    public void setLongs(long offset, long[] longs, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setLong(offset + i, longs[low + i]);
    }

    /** Sets the <code>short</code> at the given offset.
     *
     *  @param offset The offset at which to write the short.
     *  @param value The short to write.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid address range.
     */
    public void setShort(long offset, short value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Sets <code>number</code> shorts starting at the given offset from
     *  the short array passed starting at position <code>low</code>.
     *
     *  @param offset The offset at which to start writing.
     *  @param shorts The array from which the items are obtained.
     *  @param low The offset which is the starting point in the given array
     *             for the items to be obtained.
     *  @param number The number of items to write.
     *  @throws OffsetOutOfBoundsException The offset is invalid.
     *  @throws SizeOutOfBoundsException The size is negative or extends
     *                                   into an invalid address range.
     */
    public void setShorts(long offset, short[] shorts, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setLong(offset + i, shorts[low + i]);
    }

    /** Unmap the physical memory range from virtual memory. No-op if the
     *  system doesn't support virtual memory.
     */
    public void unmap() {
	// TODO
    }

    /** Construct a RawMemoryAccess area at offset bytes from the 
     *  base of this area.
     */
    public RawMemoryAccess subregion(long offset, long size) {
	return new RawMemoryAccess(offset, size);
    }
}
