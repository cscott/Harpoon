package javax.realtime;

public class LTMemory extends ScopedMemory {
    public LTMemory(long initialSizeInBytes, long maxSizeInBytes) {
	super(maxSizeInBytes);
    }

    public String toString() {
	return "LTMemory: " + super.toString();
    }

}
