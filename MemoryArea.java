// MemoryArea.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/** <code>MemoryArea</code> represents an area of memory from which objects
 *  can be allocated.  Use the "enter" method to pass in a Runnable which 
 *  will run in this MemoryArea.  You can also .newinstance and .newArray
 *  objects out of this MemoryArea and pass this MemoryArea to the constructor
 *  of a RealtimeThread, indicating that the thread is to be run in the new
 *  MemoryArea.  See the RTJ specification for more details.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public abstract class MemoryArea {

    /** The size of this MemoryArea.
     */

    protected long size;

    /** */

    protected long memoryConsumed;  // Size is somewhat inaccurate

    /** Indicates whether this is a ScopedMemory. 
     */

    boolean scoped;

    /** Indicates whether this is a HeapMemory.
     */

    boolean heap;

    /** Indicates whether this is a NullMemoryArea. 
     */
    
    protected boolean nullMem;

    /** Every MemoryArea has a unique ID. 
     */

    protected int id;

    /** This is used to create the unique ID.
     */

    private static int num = 0;

    /** Indicates whether this memoryArea refers to a constant or not. 
     *  This is set by the compiler.
     */

    boolean constant;

    MemoryArea shadow;
  
    protected native void enterMemBlock(RealtimeThread rt, MemAreaStack mas);
    protected native void exitMemBlock(RealtimeThread rt, MemAreaStack mas);
    protected native Object newArray(RealtimeThread rt, 
				     Class type, 
				     int number);
    protected native Object newArray(RealtimeThread rt, 
				     Class type, int[] dimensions);
    protected native Object newInstance(RealtimeThread rt, 
			      Constructor constructor, 
			      Object[] parameters)
	throws InvocationTargetException;
    protected native void throwIllegalAssignmentError(Object obj, 
						      MemoryArea ma)
	throws IllegalAssignmentError;

    protected MemoryArea(long sizeInBytes) {
	size = sizeInBytes;
	preSetup();
	initNative(sizeInBytes);
	postSetup();
    }

    protected MemoryArea(long minimum, long maximum) {
	size = maximum;
	preSetup();
	/* Sub-class will do postSetup */
    }

    protected void preSetup() {
	scoped = false; // To avoid the dreaded instanceof
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

    protected native MemoryArea shadow();

    /** */
  
    protected native void registerFinal();
  
    /** */

    protected abstract void initNative(long sizeInBytes);

    /** */

    public void enter(Runnable logic) {
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
		throw new ThrowBoundaryError("An exception occurred that was "
					     +"allocated in an inner scope that "
					     +"just exited.");
	    }
	    current.exitMem();
	    throw new ThrowBoundaryError(e.toString());
	} catch (Error e) {
	    try {
		e.memoryArea = getMemoryArea(e);
		oldMem.checkAccess(e);
	    } catch (Exception checkException) {
		current.exitMem();
		throw new ThrowBoundaryError("An exception occurred that was "
					     +"allocated in an inner scope that "
					     +"just exited.");
	    }
	    current.exitMem();
	    throw e;
	}
	current.exitMem();
    }

    /** */
    
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
    
    /** Return the amount of memory consumed in this MemoryArea.
     */

    public long memoryConsumed() {
	return memoryConsumed;
    }
    
    /** Return the amount of memory remaining in this MemoryArea.
     */

    public long memoryRemaining() {
	return size()-memoryConsumed();
    }
    
    /** Return the size of this MemoryArea.
     */

    public long size() {
	return size;
    }
    
    /** */

    public void bless(java.lang.Object obj) { 
	obj.memoryArea = shadow;
    }
    
    /** Create a new array, allocated out of this MemoryArea. 
     */

    public Object newArray(final Class type, final int number) 
	throws IllegalAccessException, InstantiationException, OutOfMemoryError
    {
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
    
    public Object newInstance(Class type)
	throws IllegalAccessException, InstantiationException,
	OutOfMemoryError {
	return newInstance(type, new Class[0], new Object[0]);
    }
    
    /** Create a new object, allocated out of this MemoryArea.
     */
    
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
