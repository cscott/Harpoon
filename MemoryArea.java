// MemoryArea.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

/** <code>MemoryArea</code>
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public abstract class MemoryArea {

    /** */

    protected long size;

    /** */

    protected long memoryConsumed;  // Size is somewhat inaccurate

    /** */

    boolean scoped;

    /** */

    boolean heap;

    /** */
    
    protected boolean nullMem;

    /** */

    protected int id;

    /** */

    private static int num = 0;

    /* Number of bugs in Sun's libraries...
     * Use a gdb script to watch this with a hardware watchpoint to list all of the
     * methods that are broken.
     */

    protected static int java_lang_Brokenness = 0;  

    /** Indicates whether this memoryArea refers to a constant or not. 
     *  This is set by the compiler.
     */

    boolean constant;

    native void enterMemBlock(RealtimeThread rt);
    native void exitMemBlock(RealtimeThread rt);

    protected MemoryArea(long sizeInBytes) {
	size = sizeInBytes;
	scoped = false; // To avoid the dreaded instanceof
	heap = false;
	id = num++;
	constant = false;
	nullMem = false;
	initNative(sizeInBytes);
    }
    
    /** */

    protected abstract void initNative(long sizeInBytes);

    /** */

    protected abstract void newMemBlock(RealtimeThread rt);
    
    /** */

    public void enter(Runnable logic) {
	enter(logic, false);
    }

    /** */

    void enter(Runnable logic, boolean checkReentry) {
	RealtimeThread current = RealtimeThread.currentRealtimeThread();
	current.enterFromMemArea(this, checkReentry);
	logic.run();
	current.exit();
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
	    return RealtimeThread.currentRealtimeThread().getMemoryArea();
//    	    return NullMemoryArea.instance();
	} 
	if (mem.constant) { // Constants are allocated out of ImmortalMemory
	    return ImmortalMemory.instance();
	}
	return mem;
    }
    
    /** */

    public long memoryConsumed() {
	return memoryConsumed;
    }
    
    /** */

    public long memoryRemaining() {
	return size-memoryConsumed;
    }
    
    /** */

    public long size() {
	return size;
    }
    
    /** */

    private long checkMem(java.lang.Class type, int number) {
	long size = 4 * number;
	if ((memoryConsumed + size) > this.size) {
	    throw new OutOfMemoryError();
	}
	return size;
    }

    /** */

    private long checkMem(java.lang.Class type, int[] dimensions) {
	long size = 4;
	for (int i = 0; i < dimensions.length; i++) {
	    size *= dimensions[i];
	}
	if ((memoryConsumed + size) > this.size) {
	    throw new OutOfMemoryError();
	}
	return size;
    }
    
    /** */

    private java.lang.Object update(java.lang.Object obj, long size) {
	memoryConsumed += size;
	obj.memoryArea = this;
	return obj;
    }
    
    /** */

    public synchronized void bless(java.lang.Object obj) { 
	long size;
	try {
	    size = checkMem(obj.getClass(), 1);
	} catch (OutOfMemoryError e) {
	    obj = null;
	    throw e;
	}
	update(obj, size);
    }
    
    /** */

    public synchronized void bless(java.lang.Object obj, int[] dimensions) { 
	long size;
	try {
	    size = checkMem(obj.getClass(), dimensions);
	} catch (OutOfMemoryError e) {
	    obj = null;
	    throw e;
	}
	update(obj, size);
    }

    /** */

    public synchronized Object newArray(final Class type, final int number) 
	throws IllegalAccessException, InstantiationException, OutOfMemoryError
    {
	long size = checkMem(type, number);
	RealtimeThread.currentRealtimeThread().getMemoryArea()
	    .checkAccess(this);
	return update(Array.newInstance(type, number), size);
    }
    
    /** */

    public synchronized Object newArray(final Class type,
					final int[] dimensions) 
	throws IllegalAccessException, OutOfMemoryError
    {
	long size = checkMem(type, dimensions);
	RealtimeThread.currentRealtimeThread().getMemoryArea()
	    .checkAccess(this);
	return update(Array.newInstance(type,dimensions), size);
    }

    /** */
    
    public synchronized Object newInstance(Class type)
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	return newInstance(type, new Class[0], new Object[0]);
    }
    
    /** */

    public synchronized Object newInstance(final Class type,
					   final Class[] parameterTypes,
					   final Object[] parameters) 
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	long size = checkMem(type, 1);
	RealtimeThread.currentRealtimeThread().getMemoryArea().checkAccess(this);
	try {
	    return update(type.getConstructor(parameterTypes)
			  .newInstance(parameters), size);
	} catch (NoSuchMethodException e) {
	    throw new InstantiationException(e.getMessage());
	} catch (InvocationTargetException e) {
	    throw new InstantiationException(e.getMessage());
	}	
    }
    
    /** */

    public void checkAccess(Object obj) {
	if ((obj != null) && (obj.memoryArea != null) && 
	    obj.memoryArea.scoped) {
	    // Sun's libraries are broken - just annotate that this is the problem area...
	    throw new IllegalAssignmentError();
	    //	    java_lang_Brokenness++; 
	}
    }
    
    /** */

    public MemoryArea getOuterScope() {
	return null;
    }
    
    /** */

    public String toString() {
	MemoryArea parent = null;
	try {
	    parent = getOuterScope();
	} catch (MemoryScopeError e) {
	    return String.valueOf(id + " not in execution path");
	}
	if (parent == null) {
	    return String.valueOf(id);      
	} else {
	    return parent.toString() + "." + String.valueOf(id);
	}    
    }
}
