// RawMemoryAccess.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** An instance of <code>RawMemoryAccess</code> models a range of
 *  physical memory as a fixed sequence of bytes. A full complement
 *  of accessor methods allow the contents of the physical area to
 *  be accessed through offsets from the base, interpreted as byte,
 *  short, int, or long data values or as arrays of these types.
 *  Whether the offset addresses the high-order of low-order byte
 *  is based on the value of the <code>BYTE_ORDER</code> static
 *  boolean variable in class <code>RealtimeSystem</code>.
 *  The <code>RawMemoryAccess</code> class allows a real-time
 *  program to implement device drivers, memory-mapped I/O, flash
 *  memory, battery-backed RAM, and similar low-level software.
 *  A raw memory area cannot contain references to Java objects.
 *  Such a capability would be unsafe (since it could be used to
 *  defeat Java's type checking) and error-prone (since it is
 *  sensitive to the specific representational choices made by
 *  the Java compiler).
 *  Many of the constructors and methods in this class throw
 *  <code>OffsetOutOfBoundsException</code>. This exception means
 *  that the value given in the offset parameter is either negative
 *  or outside the memory area.
 *  Many of the constructors and methods in this class throw
 *  <code>SizeOutOfBoundsException</code>. This exception means
 *  that the value given in the size parameter is either negative,
 *  larger than an allowable range, or would cause an accessor
 *  method to access an address outside of the memory area.
 *  Unlike other integral parameters in this chapter, negative
 *  values are valid for <code>byte, short, int</code> and
 *  <code>long</code> values that are copied in and out of memory
 *  by the <code>set</code> and <code>get</code> methods of this class.
 */
public class RawMemoryAccess {

    private long base, size;
    private Runnable logic;

    public RawMemoryAccess(Object type, long size)
	throws SecurityException, OffsetOutOfBoundsException,
	       SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	// TODO
    }

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
    
    /** Get the byte at the given offset. */
    public byte getByte(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Get <code>nunber</code> bytes starting at the given offset and
     *  assign them to the byte array starting at the position
     * <code>low</code>.
     */
    public void getBytes(long offset, byte[] bytes, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    bytes[low + i] = getByte(offset + i);
    }

    /** Get the <code>int</code> at the given offset. */
    public int getInt(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Get <code>number</code>  ints starting at the given offset and
     *  assign them to the int array passed at position <code>low</code>.
     */
    public void getInts(long offset, int[] ints, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    ints[low + i] = getByte(offset + i);
    }

    /** Get the <code>long</code> at the given offset. */
    public long getLong(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Get <code>number</code> longs starting at the given offset and
     *  assign them to the long array passed starting at position
     *  <code>low</code>.
     */
    public void getLongs(long offset, long[] longs, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    longs[low + i] = getLong(offset + i);
    }

    /** Returns the virtual memory location at which the memory region
     *  is mapped.
     */
    public long getMappedAddress() {
	// TODO

	return 0;
    }

    /** Get the <code>short</code> at the given offset. */
    public short getShort(long offset)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO

	return 0;
    }

    /** Get <code>number</code> shorts starting at the give offset and
     *  assign them to the short array passed starting at position
     *  <code>low</code>.
     */
    public void getShorts(long offset, short[] shorts, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    shorts[low + i] = getShort(offset + i);
    }

    /** Maps the physical memory range into virtual memory. */
    public long map() {
	// TODO
	
	return 0;
    }

    /** Maps the physical memory range into virtual memory at the
     *  specified location.
     */
    public long map(long base) {
	// TODO

	return 0;
    }
    
    /** Maps the physical memory range into virtual memory. */
    public long map(long base, long size) {
	// TODO

	return 0;
    }

    /** Set the byte at the given offset. */
    public void setByte(long offset, byte value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Set <code>number</code> bytes starting at the give offset from
     *  the byte array passed starting at position <code>low</code>.
     */
    public void setBytes(long offset, byte[] bytes, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setByte(offset + i, bytes[low + i]);
    }

    /** Set the <code>int</code> at the given offset. */
    public void setInt(long offset, int value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Set <code>number</code> ints starting at the given offset from
     *  the int array passed starting at position <code>low</code>.
     */
    public void setInts(long offset, int[] ints, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setInt(offset + i, ints[low + i]);
    }

    /** Set the <code>long</code> at the given offset. */
    public void setLong(long offset, long value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Set <code>number</code> longs starting at the given offset from
     *  the long array passed starting at position <code>low</code>.
     */
    public void setLongs(long offset, long[] longs, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setLong(offset + i, longs[low + i]);
    }

    /** Set the <code>short</code> at the given offset. */
    public void setShort(long offset, short value)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	// TODO
    }

    /** Set <code>number</code> shorts starting at the given offset from
     *  the short array passed starting at position <code>low</code>.
     */
    public void setShorts(long offset, short[] shorts, int low, int number)
	throws OffsetOutOfBoundsException, SizeOutOfBoundsException {
	for (int i = 0; i < number; i++)
	    setLong(offset + i, shorts[low + i]);
    }

    /** Unmap the physical memory range from virtual memory. */
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
