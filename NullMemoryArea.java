package javax.realtime;

// Ideally, this class should not exist.  It's the class of
// the MemoryAreas that escape the current scope of analysis
// (returned from native methods, etc.)  This class becomes
// very useful as a tracer when you're trying to track down 
// random bugs with respect to memory area restrictions and 
// native methods.

public class NullMemoryArea extends MemoryArea {
    /** Count how many instances of the NullMemoryArea are asked for.
     */
    public static long count = 0;

    /** The one and only NullMemoryArea. 
     */
    private static NullMemoryArea nullMemory;

    /** Create the one and only NullMemoryArea.
     */
    private NullMemoryArea() {
	super(0);
	nullMem = true;
    }

    /** Initialize the native component of this NullMemoryArea. 
     */

    protected native void initNative(long sizeInBytes);

    /** Return an instance of the one and only NullMemoryArea. 
     */

    public static NullMemoryArea instance() {
	if (nullMemory == null) {
	    nullMemory = new NullMemoryArea();
	}
	count++;
	return nullMemory;
    }

    /** Check access to this NullMemoryArea. 
     */

    public void checkAccess(Object obj) {
//  	System.out.println("Checking access from a NullMemory!");
    }

    /** Print out a helpful string representing this NullMemoryArea. */

    public String toString() {
	return "NullMemory: " + super.toString();
    }
}
