package realtime;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

// A new foo[4] becomes:
//   RealtimeThread.currentRealtimeThread().getMemoryArea().newArray(Class.forName("foo"),4);
//
// A new foo[4][5] becomes:
//   RealtimeThread.currentRealtimeThread().getMemoryArea().newArray(Class.forName("foo",{4,5});
//
// A new foo() becomes:
//   RealtimeThread.currentRealtimeThread().getMemoryArea().newInstance(Class.forName("foo"));
//
// A foo=bar becomes (if not in a class from realtime package...):
//   foo.memoryArea.checkAccess(bar);
//   foo=bar

public abstract class MemoryArea {
  protected MemoryArea parent;
  protected long size, memoryConsumed;  // Size is somewhat inaccurate - may want to fix in future
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

  public synchronized void bless(java.lang.Object obj) { // Called implicitly by compiler...
    long size;
    try {
      size = checkMem(obj.getClass(), 1);
    } catch (OutOfMemoryError e) {
      obj = null;
      throw e;
    }
    update(obj, size);
  }

  public synchronized void bless(java.lang.Object obj, int[] dimensions) { // Called implicitly by compiler...
    long size;
    try {
      size = checkMem(obj.getClass(), dimensions);
    } catch (OutOfMemoryError e) {
      obj = null;
      throw e;
    }
    update(obj, size);
  }

  public synchronized java.lang.Object newArray(java.lang.Class type,
                                                int number) {
    long size = checkMem(type, number);
    return update(Array.newInstance(type, number), size);
  }
  
  protected synchronized java.lang.Object newArray(java.lang.Class type,
                                                   int[] dimensions) {
    long size = checkMem(type, dimensions);
    return update(Array.newInstance(type, dimensions), size);	
    
  }
  
  public synchronized java.lang.Object newInstance(java.lang.Class type)
    throws IllegalAccessException, InstantiationException,
    OutOfMemoryError {
    return newInstance(type, new java.lang.Class[0], new java.lang.Object[0]);
  }

  protected synchronized java.lang.Object newInstance(java.lang.Class type,
                                                      java.lang.Class[] parameterTypes,
                                                      java.lang.Object[]
                                                      parameters) 
    throws IllegalAccessException, InstantiationException,
    OutOfMemoryError {
    long size = checkMem(type, 1);
    java.lang.Object newObj;
    try {
      newObj = type.getConstructor(parameterTypes).newInstance(parameters);
    } catch (NoSuchMethodException e) {
      throw new InstantiationException(e.getMessage());
    } catch (InvocationTargetException e) {
	    throw new InstantiationException(e.getMessage());
    }
    return update(newObj, size);
  }

  public synchronized void checkAccess(java.lang.Object obj) throws IllegalAccessException {
    Stats.addCheck();
    if ((obj!=null)&&(obj.memoryArea!=null)&&obj.memoryArea.scoped) {
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







