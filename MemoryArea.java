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
  protected long size, memoryConsumed;
  protected boolean scoped;
  
  protected MemoryArea(long sizeInBytes) {
    size = sizeInBytes;
    scoped = false; // To avoid the dreaded instanceof
    parent = null;
  }
  
  public void enter(java.lang.Runnable logic) {
    RealtimeThread current = RealtimeThread.currentRealtimeThread();
    MemoryArea oldMem = current.mem;
    current.mem = this;
    logic.run();
    current.mem = oldMem;
  }
  
  public static MemoryArea getMemoryArea(java.lang.Object object) {
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
  
  public synchronized java.lang.Object newArray(java.lang.Class type,
                                                int number) {
    long size = 4 * number;
    if ((memoryConsumed + size) > this.size) {
      throw new OutOfMemoryError();
    }
    java.lang.Object newArray = Array.newInstance(type, number);
    memoryConsumed += size;
    newArray.memoryArea = this;
    return newArray;
  }
  
  protected synchronized java.lang.Object newArray(java.lang.Class type,
                                                   int[] dimensions) {
    long size = 4;
    for (int i = 0; i < dimensions.length; i++) {
      size *= dimensions[i];
    }
    if ((memoryConsumed + size) > this.size) {
      throw new OutOfMemoryError();
    }
    java.lang.Object newArray = Array.newInstance(type, dimensions);
    memoryConsumed += size;
    newArray.memoryArea = this;
    return newArray;	
    
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
    java.lang.Object newObj;
    try {
      newObj = type.getConstructor(parameterTypes).newInstance(parameters);
    } catch (NoSuchMethodException e) {
      throw new InstantiationException(e.getMessage());
    } catch (InvocationTargetException e) {
	    throw new InstantiationException(e.getMessage());
    }
    newObj.memoryArea = this;
    memoryConsumed += 4;
    return newObj;
  }

  public synchronized void checkAccess(java.lang.Object obj) throws IllegalAccessException {
    if ((obj!=null)&&(obj.memoryArea!=null)&&obj.memoryArea.scoped) {
      throw new IllegalAccessException();
    }
  }
}






