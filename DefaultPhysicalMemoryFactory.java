package javax.realtime;

public class DefaultPhysicalMemoryFactory {
    private static PhysicalMemoryFactory pmf = null;

    public static PhysicalMemoryFactory instance() {
	if (pmf == null) {
	    pmf = new PhysicalMemoryFactory();
	}
	return pmf;
    }
}
