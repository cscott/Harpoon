// RawMemoryAccess.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>RawMemoryAccess</code> models "raw storage" as a fixed-sequence
 *  of bytes.
 */

public class RawMemoryAccess {

    protected RawMemoryAccess(long base, long size) {
	
    }

    /** Constructor reserved for use by the memory object factory. 
     */
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
    
    /** Get the byte at the given offset. 
     */
    public native byte getByte(long offset)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Get n bytes starting at the given offset in this, and assigns them
     *  into the byte array starting at the position low.
     */
    public native void getBytes(long offset, byte[] bytes, int low, int n)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Get the int at the given offset.
     */
    public native int getInt(long offset)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Get n ints starting at the given offset in this, to the int array
     *  starting at position low.
     */
    public native void getInts(long offset, int[] ints, int low, int n)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Get the long value at the given offset.
     */
    public native void getLong(long offset)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Get n longs starting at the given offset in this, to the long array
     *  starting at position low.
     */
    public native void getLongs(long offset, long[] longs, int low, int n)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** getMappedAddress gives the virtual memory location at which the
     *  memory region is mapped. 
     */
    public long getMappedAddress() {
	return 0;
    }

    /** Get the short at the given offset. 
     */
    public native short getShort(long offset)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Get n shorts starting at the given offset in this, from the short
     *  array starting at position low.
     */
    public native void getShorts(long offset, short[] shorts, int low, int n)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** map maps the physical address range into virtual memory.
     */
    public long map() {
	return 0;
    }

    /** map maps the physical address range into virtual memory at the 
     *  specified location.
     */
    public long map(long base) {
	return 0;
    }
    
    /** map maps the physical address range into virtual memory at the
     *  specified location.
     */
    public long map(long base, long size) {
	return 0;
    }

    /** Set the byte at the given offset.
     */
    public native void setByte(long offset, byte v)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set n bytes starting at the given offset in this, from the 
     *  byte array starting at position low.
     */
    public native void setBytes(long offset, byte[] bytes, int low, int n)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set the int value at the given offset.
     */
    public native void setInt(long offset, int v)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set n ints starting at the given offset in this, from the
     *  int array starting at position low. 
     */
    public native void setInts(long offset, int[] ints, int low, int n)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set the long value at the given offset starting at position
     *  low.
     */
    public native void setLong(long offset, long v)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set n longs starting at the given offset in this, from the 
     *  long array starting at position low.
     */
    public native void setLongs(long offset, long[] longs, int low, int n)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set the short at the given offset.
     */
    public native void setShort(long offset, short v)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Set n shorts starting at the given offset in this, from
     *  the short array starting at position low.
     */
    public native void setShorts(long offset, short[] shorts, int low, int n)
	throws SizeOutOfBoundsException, OffsetOutOfBoundsException;

    /** Construct a RawMemoryAccess area at offset bytes from the 
     *  base of this area.
     */
    public RawMemoryAccess subregion(long offset, long size) {
	return new RawMemoryAccess(offset, size);
    }

    /** unmap() unmaps the physical address range from virtual memory.
     */
    public void unmap() {

    }
};
