// ImmortalPhysicalMemoryFactory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** An instance of <code>ImmortalPhysicalMemory</code> allows objects to be
 *  allocated from a range of physical memory with particular attributes,
 *  determined by their memory type. This memory area has the same restrictive
 *  set of assignment rules as <code>ImmoratMemory</code> memory areas, and
 *  may be used in any context where <code>ImmortalMemory</code> is appropriate.
 *  Objects allocated in immortal physical memory have a lifetime greater
 *  than the application as do objects allocated in immortal memory.
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */
public class ImmortalPhysicalMemory extends MemoryArea {
    private long base, size;
    private Runnable logic;

    /** Create an instance with the given parameters.
     *
     *  @param type An instance of <code>Object</code> representing the type of
     *              memory required (e.g. <i>dma, share</i>) -used to define
     *              the base address and control the mapping.
     *  @param size The size of the area in bytes.
     *  @throws java.lang.SecurityException The application doesn't have
     *                                      permissions to access physical memory
     *                                      of the given type of memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to
     *                                      memory that matches the request type, or
     *                                      if <code>type</code> specifies attirbutes
     *                                      with a conflict.
     */
    public ImmortalPhysicalMemory(Object type, long size)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	// TODO

	// This line inserted only to make everything compile!
	super(size);
    }

    /** Create an instance with the given parameters.
     *
     *  @param type An instance of <code>Object</code> representing the type of
     *              memory required (e.g. <i>dma, share</i>) -used to define
     *              the base address and control the mapping.
     *  @param base The physical memory adress of the area.
     *  @param size The size of the area in bytes.
     *  @throws java.lang.SecurityException The application doesn't have
     *                                      permissions to access physical memory
     *                                      of the given type of memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to
     *                                      memory that matches the request type, or
     *                                      if <code>type</code> specifies attirbutes
     *                                      with a conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public ImmortalPhysicalMemory(Object type, long base, long size)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, size);
	this.base = base;
    }

    /** Create an instance with the given parameters.
     *
     *  @param type An instance of <code>Object</code> representing the type of
     *              memory required (e.g. <i>dma, share</i>) -used to define
     *              the base address and control the mapping.
     *  @param base The physical memory adress of the area.
     *  @param size The size of the area in bytes.
     *  @param logic The <code>run()</code> method of this object will be called
     *               whenever <code>MemoryArea.enter()</code> is called.
     *  @throws java.lang.SecurityException The application doesn't have
     *                                      permissions to access physical memory
     *                                      of the given type of memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to
     *                                      memory that matches the request type, or
     *                                      if <code>type</code> specifies attirbutes
     *                                      with a conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public ImmortalPhysicalMemory(Object type, long base, long size,
				  Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, base, size);
	this.logic = logic;
    }

    /** Create an instance with the given parameters.
     *
     *  @param type An instance of <code>Object</code> representing the type of
     *              memory required (e.g. <i>dma, share</i>) -used to define
     *              the base address and control the mapping.
     *  @param size The size of the area in bytes.
     *  @param logic The <code>run()</code> method of this object will be called
     *               whenever <code>MemoryArea.enter()</code> is called.
     *  @throws java.lang.SecurityException The application doesn't have
     *                                      permissions to access physical memory
     *                                      of the given type of memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to
     *                                      memory that matches the request type, or
     *                                      if <code>type</code> specifies attirbutes
     *                                      with a conflict.
     */
    public ImmortalPhysicalMemory(Object type, long size, Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.logic = logic;
    }

    /** Create an instance with the given parameters.
     *
     *  @param type An instance of <code>Object</code> representing the type of
     *              memory required (e.g. <i>dma, share</i>) -used to define
     *              the base address and control the mapping.
     *  @param base The physical memory address of the area.
     *  @param size A size estimator for this memory area.
     *  @throws java.lang.SecurityException The application doesn't have
     *                                      permissions to access physical memory
     *                                      of the given type of memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to
     *                                      memory that matches the request type, or
     *                                      if <code>type</code> specifies attirbutes
     *                                      with a conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public ImmortalPhysicalMemory(Object type, long base,
				  SizeEstimator size)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, size);
	this.base = base;
    }

    /** Create an instance with the given parameters.
     *
     *  @param type An instance of <code>Object</code> representing the type of
     *              memory required (e.g. <i>dma, share</i>) -used to define
     *              the base address and control the mapping.
     *  @param base The physical memory address of the area.
     *  @param size A size estimator for this memory area.
     *  @param logic The <code>run()</code> method of this object will be called
     *               whenever <code>MemoryArea.enter()</code> is called.
     *  @throws java.lang.SecurityException The application doesn't have
     *                                      permissions to access physical memory
     *                                      of the given type of memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to
     *                                      memory that matches the request type, or
     *                                      if <code>type</code> specifies attirbutes
     *                                      with a conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public ImmortalPhysicalMemory(Object type, long base,
				  SizeEstimator size, Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       OffsetOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException, MemoryInUseException {
	this(type, base, size);
	this.logic = logic;
    }

    /** Create an instance with the given parameters.
     *
     *  @param type An instance of <code>Object</code> representing the type of
     *              memory required (e.g. <i>dma, share</i>) -used to define
     *              the base address and control the mapping.
     *  @param size A size estimator for this memory area.
     *               whenever <code>MemoryArea.enter()</code> is called.
     *  @throws java.lang.SecurityException The application doesn't have
     *                                      permissions to access physical memory
     *                                      of the given type of memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to
     *                                      memory that matches the request type, or
     *                                      if <code>type</code> specifies attirbutes
     *                                      with a conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public ImmortalPhysicalMemory(Object type, SizeEstimator size)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size.getEstimate());
    }
    
    /** Create an instance with the given parameters.
     *
     *  @param type An instance of <code>Object</code> representing the type of
     *              memory required (e.g. <i>dma, share</i>) -used to define
     *              the base address and control the mapping.
     *  @param size A size estimator for this memory area.
     *               whenever <code>MemoryArea.enter()</code> is called.
     *  @param logic The <code>run()</code> method of this object will be called
     *               whenever <code>MemoryArea.enter()</code> is called.
     *  @throws java.lang.SecurityException The application doesn't have
     *                                      permissions to access physical memory
     *                                      of the given type of memory.
     *  @throws SizeOutOfBoundsException The size is negative or extends into an
     *                                   invalid range of memory.
     *  @throws UnsupportedPhysicalMemoryException Thrown if the underlying hardware
     *                                             does not support the given type.
     *  @throws MemoryTypeConflictException The specified base does not point to
     *                                      memory that matches the request type, or
     *                                      if <code>type</code> specifies attirbutes
     *                                      with a conflict.
     *  @throws MemoryInUseException The specified memory is already in use.
     */
    public ImmortalPhysicalMemory(Object type, SizeEstimator size,
				  Runnable logic)
	throws SecurityException, SizeOutOfBoundsException,
	       UnsupportedPhysicalMemoryException,
	       MemoryTypeConflictException {
	this(type, size);
	this.logic = logic;
    }

    /** Create an instance with the given parameters.
     *
     *  @param base The physical memory address of the area.
     *  @param size A size estimator for this memory area.
     */
    public ImmortalPhysicalMemory(long base, long size) {
	super(size);
	this.base = base;
	this.size = size;
    }

    /** */

    protected native void initNative(long sizeInBytes);
}
