package javax.realtime;

// I'm explicitly NOT going to worry about optimizing checks around
// data that's aliased through separate PhysicalMemoryFactory's (since
// determining that is undecidable).

public class ScopedPhysicalMemory extends ScopedMemory {
    private long base, size;

    public ScopedPhysicalMemory(long base, long size) {
	super(size);
	this.base = base;
	this.size = size;
    }

    public synchronized void checkAccess(java.lang.Object obj) 
	throws IllegalAccessException {
	if (obj instanceof ScopedPhysicalMemory) {
	    ScopedPhysicalMemory spm = (ScopedPhysicalMemory)obj;
	    if (!(((base<=(spm.base+spm.size))&&(spm.base<=base))||
		  (((base+size)<=(spm.base+spm.size))&&
		   (spm.base<=(base+size))))) { // It doesn't overlap
		super.checkAccess(obj);
	    }
	}
    }
}
