package javax.realtime;

// I'm explicitly NOT going to worry about the correctness of checks to
// data that's aliased through separate PhysicalMemoryFactory's....

public class PhysicalMemoryFactory {
    public synchronized Object create(String memoryType,
				      boolean foo, long base, 
				      long size) {
	if (memoryType.equals("scoped")) {
	    return new ScopedPhysicalMemory(base, size);
	} else if (memoryType.equals("immortal")) {
	    return new ImmortalPhysicalMemory(base, size);
	}
	return null;
    }

    public Object create(String memoryType, boolean foo, long size) {
	return create(memoryType, foo, 0, size);
    }
}
