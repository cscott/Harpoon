package javax.realtime;
import java.lang.Runtime;

// Memory left is kinda bogus - may want to fix sometime.

public final class HeapMemory extends MemoryArea {
    private static HeapMemory theHeap = null;
    
    private HeapMemory() {  
	super(1000000000); // Totally bogus
	//      super(Runtime.getRuntime().totalMemory());  // still bogus
	//      memoryConsumed = size - Runtime.getRuntime().freeMemory();
    }
    
    public static HeapMemory instance() {
	if (theHeap == null) { // Bypass static initializer problem.
	    theHeap = new HeapMemory();
	}
	return theHeap;
    }
    
    public String toString() {
	return "HeapMemory: " + super.toString();
    }
}
