package javax.realtime;

// I'm explicitly NOT going to worry about the correctness of checks to
// data that's aliased through separate PhysicalMemoryFactory's....

public class ImmortalPhysicalMemory extends MemoryArea {
    private long base, size;

    public ImmortalPhysicalMemory(long base, long size) {
	super(size);
	this.base = base;
	this.size = size;
    }
}
