package javax.realtime;

public class CTMemoryArea extends LTMemory {

    public CTMemoryArea(long initialSize, long maxSize) {
        super(initialSize, maxSize);
    }

    public CTMemoryArea(long size) {
        super(size, size);
    }

    public CTMemoryArea(long size, boolean tss) {
        super(size, size);
    }

}
