package javax.realtime;

public class VTMemory extends ScopedMemory {
    public VTMemory(long initial, long maximum) {
	super(maximum);
    }

    public String toString() {
	return "VTMemory: " + super.toString();
    }
}
