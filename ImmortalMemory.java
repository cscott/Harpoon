package javax.realtime;

public class ImmortalMemory extends MemoryArea {
    private static ImmortalMemory immortalMemory = null;

    private ImmortalMemory() {
	super(1000000000); // Totally bogus
    }

    public static ImmortalMemory instance() {
	if (immortalMemory == null) {
	    immortalMemory = new ImmortalMemory();
	}
	return immortalMemory;
    }
    
    public String toString() {
	return "ImmortalMemory: " + super.toString();
    }
}
