// LTPhysicalMemory.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** An instance of <code>LTPhysicalMemory</code> allows objects
 *  to be allocated from a range of physical memory with particular
 *  attributes, determined by their memory type. This memory area
 *  has the same restrictive set of assignment rules as
 *  <code>ScopedMemory</code> memory areas, and the same performance
 *  restrictions as <code>LTMemory</code>.
 */
public class LTPhysicalMemory extends ScopedMemory {

    private long base, size;
    private Runnable logic;

    protected void initNative(long sizeInBytes) {
    }

    /** Create an instance of <code>LTPhysicalMemory</code> with the given parameters.
     *
     *  @param type An instance of <code>Object representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address and
     *              control the mapping.
     *  @param size The size of the area in bytes.
     *  @throws java.lang.SecurityException The application doesn't have permissions to
     *                                      access physical memory or the given type of
     *                                      memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an invalid
     *                                   range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with a
     *                                      conflict.
     */
    public LTPhysicalMemory(Object type, long size)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	// TODO

	// This line inserted only to make everything compile!
	super(size);
    }

    /** Create an instance of <code>LTPhysicalMemory</code> with the given parameters.
     *
     *  @param type An instance of <code>Object representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address and
     *              control the mapping.
     *  @param base The physical memory address of the area.
     *  @param size The size of the area in bytes.
     *  @throws java.lang.SecurityException The application doesn't have permissions to
     *                                      access physical memory or the given type of
     *                                      memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an invalid
     *                                   range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with a
     *                                      conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public LTPhysicalMemory(Object type, long base, long size)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, size);
	this.base = base;
    }

    /** Create an instance of <code>LTPhysicalMemory</code> with the given parameters.
     *
     *  @param type An instance of <code>Object representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address and
     *              control the mapping.
     *  @param base The physical memory address of the area.
     *  @param size The size of the area in bytes.
     *  @param logic <code>enter</code> this memory area with this <code>Runnable</code>
     *               after the memory area is created.
     *  @throws java.lang.SecurityException The application doesn't have permissions to
     *                                      access physical memory or the given type of
     *                                      memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an invalid
     *                                   range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with a
     *                                      conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public LTPhysicalMemory(Object type, long base, long size,
			    Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, base, size);
	this.logic = logic;
    }

    /** Create an instance of <code>LTPhysicalMemory</code> with the given parameters.
     *
     *  @param type An instance of <code>Object representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address and
     *              control the mapping.
     *  @param size The size of the area in bytes.
     *  @param logic <code>enter</code> this memory area with this <code>Runnable</code>
     *               after the memory area is created.
     *  @throws java.lang.SecurityException The application doesn't have permissions to
     *                                      access physical memory or the given type of
     *                                      memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an invalid
     *                                   range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with a
     *                                      conflict.
     */
    public LTPhysicalMemory(Object type, long size, Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.logic = logic;
    }

    /** Create an instance of <code>LTPhysicalMemory</code> with the given parameters.
     *
     *  @param type An instance of <code>Object representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address and
     *              control the mapping.
     *  @param base The physical memory address of the area.
     *  @param size The size estimator for this memory area.
     *  @throws java.lang.SecurityException The application doesn't have permissions to
     *                                      access physical memory or the given type of
     *                                      memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an invalid
     *                                   range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with a
     *                                      conflict.
     */
    public LTPhysicalMemory(Object type, long base, SizeEstimator size)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, size);
	this.base = base;
    }

    /** Create an instance of <code>LTPhysicalMemory</code> with the given parameters.
     *
     *  @param type An instance of <code>Object representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address and
     *              control the mapping.
     *  @param base The physical memory address of the area.
     *  @param size The size estimator for this memory area.
     *  @param logic <code>enter</code> this memory area with this <code>Runnable</code>
     *               after the memory area is created.
     *  @throws java.lang.SecurityException The application doesn't have permissions to
     *                                      access physical memory or the given type of
     *                                      memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an invalid
     *                                   range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with a
     *                                      conflict.
     */
    public LTPhysicalMemory(Object type, long base, SizeEstimator size,
			    Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, base, size);
	this.logic = logic;
    }

    /** Create an instance of <code>LTPhysicalMemory</code> with the given parameters.
     *
     *  @param type An instance of <code>Object representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address and
     *              control the mapping.
     *  @param size The size estimator for this memory area.
     *  @throws java.lang.SecurityException The application doesn't have permissions to
     *                                      access physical memory or the given type of
     *                                      memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an invalid
     *                                   range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with a
     *                                      conflict.
     */
    public LTPhysicalMemory(Object type, SizeEstimator size)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size.getEstimate());
    }

    /** Create an instance of <code>LTPhysicalMemory</code> with the given parameters.
     *
     *  @param type An instance of <code>Object representing the type of memory required
     *              (e.g., <i>dma, shared</i>) - used to define the base address and
     *              control the mapping.
     *  @param size The size estimator for this memory area.
     *  @param logic <code>enter</code> this memory area with this <code>Runnable</code>
     *               after the memory area is created.
     *  @throws java.lang.SecurityException The application doesn't have permissions to
     *                                      access physical memory or the given type of
     *                                      memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an invalid
     *                                   range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware does
     *                                             not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to memory
     *                                      that matches the request type, or if
     *                                      <code>type</code> specifies attributes with a
     *                                      conflict.
     */
    public LTPhysicalMemory(Object type, SizeEstimator size,
			    Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.logic = logic;
    }

    /** Creates a string representing the name of <code>this</code>.
     *
     *  @return A string representing the name of <code>this</code>.
     */
    public String toString() {
	return "LTPhysicalMemory: " + super.toString();
    }
}
