// MemoryArea.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** <code>MemoryArea</code> is the abstract base class of all classes dealing
 *  with the representations of allocatable memory areas, including the
 *  immortal memory area, physical memory and scoped memory areas.
 */
public abstract class MemoryArea {

    /** The size of this MemoryArea. */
    protected long size;

    /** The size of the consumed memory */
    // Size is somewhat inaccurate
    protected long memoryConsumed;

    /** Indicates whether this is a ScopedMemory. */
    boolean scoped;

    /** Indicates whether this is a HeapMemory. */
    boolean heap;

    /** Indicates whether this is a NullMemoryArea. */
    protected boolean nullMem;

    /** Every MemoryArea has a unique ID. */
    protected int id;

    /** The run() method of this object is called whenever enter() is called. */
    protected Runnable logic;

    /** This is used to create the unique ID. */
    private static int num = 0;

    /** Indicates whether this memoryArea refers to a constant or not. 
     *  This is set by the compiler.
     */
    boolean constant;

    /** The shadow of <code>this</code>. */
    MemoryArea shadow;
  
    abstract protected void initNative(long sizeInBytes);

    protected MemoryArea(long sizeInBytes) {
	size = sizeInBytes;
	preSetup();
	initNative(sizeInBytes);
	postSetup();
    }

    protected void preSetup() {
	scoped = false; 
	heap = false;
	id = num++;
	constant = false;
	nullMem = false;
    }
    
    protected void postSetup() {
        (shadow = shadow()).shadow = shadow;
	registerFinal();
    }

    /** */

    // Do we really need this abstract method?
    // protected abstract void initNative(long sizeInBytes);

    protected MemoryArea(long sizeInBytes, Runnable logic) {
	this(sizeInBytes);
	this.logic = logic;
    }

    protected MemoryArea(SizeEstimator size) {
	this(size.getEstimate());
    }

    protected MemoryArea(SizeEstimator size,
			 Runnable logic) {
	this(size);
	this.logic = logic;
    }

    /** Associate this memory area to the current real-time thread for the
     *  duration of the execution of the <code>run()</code> method of the
     *  <code>java.lang.Runnable</code> passed at construction time. During
     *  this bound period of execution, all objects are allocated from the
     *  memory area until another one takes effect, or the <code>enter()</code>
     *  method is exited. A runtime exception is thrown if this method is
     *  called from thread other than a <code>RealtimeThread</code> or
     *  <code>NoHeapRealtimeThrea</code>.
     */
    public void enter() throws ScopedCycleException {
	enter(this.logic);
    }

    /** Associate this memory area to the current real-time thread for the
     *  duration of the execution of the <code>run()</code> method of the
     *  <code>java.lang.Runnable</code> passed at construction time. During
     *  this bound period of execution, all objects are allocated from the
     *  memory area until another one takes effect, or the <code>enter()</code>
     *  method is exited. A runtime exception is thrown if this method is
     *  called from thread other than a <code>RealtimeThread</code> or
     *  <code>NoHeapRealtimeThrea</code>.
     */
    public void enter(Runnable logic) throws ScopedCycleException {
	RealtimeThread.checkInit();
	RealtimeThread current = RealtimeThread.currentRealtimeThread();
	MemoryArea oldMem = current.memoryArea();
	current.enter(shadow, this);
	try {
	    logic.run();
	} catch (Exception e) {
	    try {
		e.memoryArea = getMemoryArea(e);
		oldMem.checkAccess(e);
	    } catch (Exception checkException) {
		current.exitMem();
		throw new ThrowBoundaryError("An exception occurred that was " +
					     "allocated in an inner scope that " +
					     "just exited.");
	    }
	    current.exitMem();
	    throw new ThrowBoundaryError(e.toString());
	} catch (Error e) {
	    try {
		e.memoryArea = getMemoryArea(e);
		oldMem.checkAccess(e);
	    } catch (Exception checkException) {
		current.exitMem();
		throw new ThrowBoundaryError("An exception occurred that was " +
					     "allocated in an inner scope that " +
					     "just exited.");
	    }
	    current.exitMem();
	    throw e;
	}
	current.exitMem();
    }

    /** Execute the <code>run()</code> method from the <code>logic</code> parameter
     *  using this memory area as the current allocation context. If the memory
     *  area is a scoped memory type, this method behaves as if it had moved the
     *  allocation context up the scope stack to the occurrence of the memory area.
     *  If the memory area is heap or immortal memory, this method behaves as if
     *  the <code>run()</code> method were running in that memory type with an
     *  empty scope stack.
     */
    public void executeInArea(Runnable logic)
	throws InaccessibleAreaException {
	// TODO
    }

    /** Returns the <code>MemoryArea</code> in which the given object is located. */
    public static MemoryArea getMemoryArea(Object object) {
	if (object == null) {
	    return ImmortalMemory.instance();
	}
	MemoryArea mem = object.memoryArea;
	// I'm completely punting this for now...
  	if (mem == null) { // Native methods return objects 
  	    // allocated out of the current scope.
	    return RealtimeThread.currentRealtimeThread().memoryArea();
//    	    return NullMemoryArea.instance();
	} 
	if (mem.constant) { 
	    // Constants are allocated out of ImmortalMemory
	    // Also, static objects before RTJ is setup...
	    return ImmortalMemory.instance();
	}
	return mem;
    }
    
    /** An exact count, in bytes, of the all of the memory currently
     *  used by the system for the allocated objects.
     */
    public long memoryConsumed() {
	return memoryConsumed;
    }
    
    /** An approximation to the total amount of memory currently
     *  available for future allocated objects, measured in bytes.
     */
    public long memoryRemaining() {
	return size()-memoryConsumed();
    }
    
    protected native Object newArray(RealtimeThread rt, 
				     Class type, 
				     int number);
    protected native Object newArray(RealtimeThread rt, 
				     Class type, int[] dimensions);

    /** Allocate an array of <code>type</code> in this memory area. */
    public Object newArray(final Class type, final int number) 
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	RealtimeThread.checkInit();
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	if (number<0) {
	    throw new NegativeArraySizeException();
	}
	Object o = new Object();
	o.memoryArea = shadow;
	rt.memoryArea().checkAccess(o);
	(o = newArray(rt, type, number)).memoryArea = shadow;
	return o;
    }

    protected native Object newInstance(RealtimeThread rt, 
			      Constructor constructor, 
			      Object[] parameters)
	throws InvocationTargetException;
    
    /** Allocate an object in this memory area. */
    public Object newInstance(Class type)
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	return newInstance(type, new Class[0], new Object[0]);
    }
    
    /** Allocate an object in this memory area. */
    public Object newInstance(final Class type,
			      final Class[] parameterTypes,
			      final Object[] parameters) 
	throws IllegalAccessException, InstantiationException,
	OutOfMemoryError {
	RealtimeThread.checkInit();
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	Object o = new Object();
	o.memoryArea = shadow;
	rt.memoryArea().checkAccess(o);
	try {
	    Constructor c = type.getConstructor(parameterTypes);
	    o = newInstance(rt, c, parameters);
	} catch (NoSuchMethodException e) {
	    throw new InstantiationException(e.getMessage());
	} catch (InvocationTargetException e) {
	    throw new InstantiationException(e.getMessage());
	}
	o.memoryArea = shadow;
	return o;
    }
    
    /** Allocate an object in this memory area. */
    public Object newInstance(Constructor c, Object[] args)
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	// TODO

	return null;
    }

    /** Query the size of the memory area. The returned value is the
     *  current size. Current size may be larger than initial size for
     *  those areas that are allowed to grow.
     */
    public long size() {
	return size;
    }
    

    // METHODS NOT IN SPECS


    protected MemoryArea(long minimum, long maximum) {
	size = maximum;
	preSetup();
	/* Sub-class will do postSetup */
    }

    protected native void enterMemBlock(RealtimeThread rt, MemAreaStack mas);
    protected native void exitMemBlock(RealtimeThread rt, MemAreaStack mas);
    protected native void throwIllegalAssignmentError(Object obj, 
						      MemoryArea ma)
	throws IllegalAssignmentError;

    /** */

    protected native MemoryArea shadow();

    /** */
  
    protected native void registerFinal();
  
    /** */

    public void bless(java.lang.Object obj) { 
	obj.memoryArea = shadow;
    }
    
    /** Create a new array, allocated out of this MemoryArea. 
     */
    
    public Object newArray(final Class type,
			   final int[] dimensions) 
	throws IllegalAccessException, OutOfMemoryError
    {
	RealtimeThread.checkInit();
	RealtimeThread rt = RealtimeThread.currentRealtimeThread();
	for (int i = 0; i<dimensions.length; i++) {
	    if (dimensions[i]<0) {
		throw new NegativeArraySizeException();
	    }
	}
	Object o = new Object();
	o.memoryArea = shadow;
	rt.memoryArea().checkAccess(o);
	(o = newArray(rt, type, dimensions)).memoryArea = shadow;
	return o;
    }

    /** Create a new object, allocated out of this MemoryArea.
     */
    
    /** Check to see if this object can be accessed from this MemoryArea
     */
    
    public void checkAccess(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && 
	    obj.memoryArea.scoped) {
	    throwIllegalAssignmentError(obj, obj.memoryArea);
	}
    }
    
    /** Get the outerScope of this MemoryArea, for non-ScopedMemory's,
     *  this defaults to null.
     */

    public MemoryArea getOuterScope() {
	return null;
    }
    
    /** Output a helpful string describing this MemoryArea.
     */

    public String toString() {
//  	MemoryArea parent = null;
//  	try {
//  	    parent = getOuterScope();
//  	} catch (MemoryScopeError e) {
//  	    return String.valueOf(id + " not in execution path");
//  	}
//  	if (parent == null) {
//  	    return String.valueOf(id);      
//  	} else {
//  	    return parent.toString() + "." + String.valueOf(id);
//  	}    
	return String.valueOf(id);
    }
}
