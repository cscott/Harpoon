package javax.realtime;

// Ideally, this class should not exist.  It's the class of
// the MemoryAreas that escape the current scope of analysis
// (returned from native methods, etc.)  This class becomes
// very useful as a tracer when you're trying to track down 
// random bugs with respect to memory area restrictions and 
// native methods.

public class NullMemoryArea extends MemoryArea {
    public static long count = 0;
    private static NullMemoryArea nullMemory;

    private NullMemoryArea() {
	super(0);
	nullMem = true;
    }

    /** */

    public static NullMemoryArea instance() {
	if (nullMemory == null) {
	    nullMemory = new NullMemoryArea();
	}
	count++;
	return nullMemory;
    }

    /** */

    public void checkAccess(Object obj) {
//  	System.out.println("Checking access from a NullMemory!");
    }

    /** */

    public String toString() {
	return "NullMemory: " + super.toString();
    }
}
