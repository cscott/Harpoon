package realtime;
import java.lang.Runtime;

// Memory left is kinda bogus - may want to fix sometime.

public final class HeapMemory extends MemoryArea {
  private static HeapMemory theHeap = new HeapMemory();

  private HeapMemory() {  
    super(Runtime.getRuntime().totalMemory());  // This is still a bogus number...
    memoryConsumed = size - Runtime.getRuntime().freeMemory();
  }

  public static HeapMemory instance() {
    return theHeap;
  }
}
