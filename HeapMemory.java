package realtime;
import java.lang.Runtime;

// Memory left is kinda bogus - may want to fix sometime.

public final class HeapMemory extends MemoryArea {
  private static HeapMemory theHeap = null;

  private HeapMemory() {  
    super(1000000000); // Totally bogus - support for the following disappeared...
//      super(Runtime.getRuntime().totalMemory());  // This is still a bogus number...
//      memoryConsumed = size - Runtime.getRuntime().freeMemory();
  }

  public static HeapMemory instance() {
    if (theHeap == null) { // Bypass static initializer chicken-and-egg problem.
      theHeap = new HeapMemory();
    }
    return theHeap;
  }

  public String toString() {
    return "HeapMemory: " + super.toString();
  }
}
