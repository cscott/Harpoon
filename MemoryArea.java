package javax.realtime;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

public abstract class MemoryArea {
    protected MemoryArea parent;
    protected long size, memoryConsumed;  // Size is somewhat inaccurate
    protected boolean scoped;
    protected int id;
    private static int num = 0;

    protected MemoryArea(long sizeInBytes) {
	size = sizeInBytes;
	scoped = false; // To avoid the dreaded instanceof
	parent = null;
	id = num++;
    }
    
    public void enter(java.lang.Runnable logic) {
	RealtimeThread current = RealtimeThread.currentRealtimeThread();
	MemoryArea oldMem = current.mem;
	current.mem = this;
	logic.run();
	current.mem = oldMem;
    }
    
    public static MemoryArea getMemoryArea(java.lang.Object object) {
	if (object.memoryArea == null) { // Constants/native objects 
	    // are attached to theHeap
	    return HeapMemory.instance();
	}
	return object.memoryArea;
    }
    
    public long memoryConsumed() {
	return memoryConsumed;
    }
    
    public long memoryRemaining() {
	return size-memoryConsumed;
    }
    
    public long size() {
	return size;
    }
    
    private long checkMem(java.lang.Class type, int number) {
	long size = 4 * number;
	if ((memoryConsumed + size) > this.size) {
	    throw new OutOfMemoryError();
	}
	return size;
    }

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
    
    private java.lang.Object update(java.lang.Object obj, long size) {
	memoryConsumed += size;
	obj.memoryArea = this;
	return obj;
    }
    
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
    
    public synchronized java.lang.Object newArray(final java.lang.Class type,
						  final int number) 
	throws IllegalAccessException, InstantiationException, OutOfMemoryError
    {
	long size = checkMem(type, number);
	RealtimeThread.currentRealtimeThread().getMemoryArea().checkAccess(this);
	class NewArray implements Runnable {
	    public Object newObj;
	    public void run() {
		newObj = Array.newInstance(type, number);
	    }
	}
	NewArray newArray = new NewArray();
	enter(newArray);
	return update(newArray.newObj, size);
    }
    
    public synchronized java.lang.Object newArray(final java.lang.Class type,
						  final int[] dimensions) 
	throws IllegalAccessException, OutOfMemoryError
    {
	long size = checkMem(type, dimensions);
	RealtimeThread.currentRealtimeThread().getMemoryArea().checkAccess(this);
	class NewArray implements Runnable {
	    public Object newObj;
	    public void run() {
		newObj = Array.newInstance(type, dimensions);
	    }
	}
	NewArray newArray = new NewArray();
	enter(newArray);
	return update(newArray.newObj, size);
    }
    
    public synchronized java.lang.Object newInstance(java.lang.Class type)
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	return newInstance(type, new java.lang.Class[0], new java.lang.Object[0]);
    }
    
    public synchronized java.lang.Object 
	newInstance(final java.lang.Class type,
		    final java.lang.Class[] parameterTypes,
		    final java.lang.Object[] parameters) 
	throws IllegalAccessException, InstantiationException,
	       OutOfMemoryError {
	long size = checkMem(type, 1);
	RealtimeThread.currentRealtimeThread().getMemoryArea().checkAccess(this);
	class NewObject implements Runnable {
	    public Object newObj;
	    InstantiationException instantiationException;
	    IllegalAccessException illegalAccessException;
	    public NewObject() {
		newObj = null;
		instantiationException = null;
		illegalAccessException = null;
	    }
	    
	    public void run() {
		try {
		    newObj = type.getConstructor(parameterTypes)
			.newInstance(parameters);
		} catch (NoSuchMethodException e) {
		    instantiationException = 
			new InstantiationException(e.getMessage());
		} catch (InvocationTargetException e) {
		    instantiationException = 
			new InstantiationException(e.getMessage());
		} catch (IllegalAccessException e) {
		    illegalAccessException = e;
		} catch (InstantiationException e) {
		    instantiationException = e;
		}
	    }
	}
	NewObject newObject = new NewObject();
	enter(newObject);
	if (newObject.newObj == null) {
	    if (newObject.instantiationException != null) {
		throw newObject.instantiationException;
	    } else if (newObject.illegalAccessException != null) {
		throw newObject.illegalAccessException;
	    } else {
		throw new InstantiationException("Returned object is null.");
	    }
	}
	return update(newObject.newObj, size);
    }
    
    public synchronized void checkAccess(java.lang.Object obj) 
	throws IllegalAccessException {
	Stats.addCheck();
	if ((obj != null) && (obj.memoryArea != null) && obj.memoryArea.scoped) {
	    throw new IllegalAccessException();
	}
    }
    
    public String toString() {
	if (parent == null) {
	    return String.valueOf(id);      
	} else {
	    return parent.toString() + "." + String.valueOf(id);
	}    
    }
}







