package javax.realtime;

/** This is a convenient class to help people figure out
 *  how much memory the need. Instead of passing actual
 *  numbers to the <code>MemoryArea</code> constructors,
 *  one can pass <code>SizeEstimator</code> objects with
 *  which you can have a better feel of how big a memory
 *  area you require.
 */
public final class SizeEstimator {

    /** The estimated number of bytes neede to store all objects reserved. */
    private long estimate;

    public SizeEstimator() {}

    /** Returns an estimate of the number of bytes needed to store
     *  all the objects reserved.
     */
    public long getEstimate() {
	return estimate;
    }

    /** Take into account additional <code>n</code> instances of <code>Class c</code>
     *  when estimating the size of the <code>MemoryArea</code>.
     */
    public void reserve(Class c, int n) {
	// objSize(Class) is not implemented yet.
	//	estimate += n * objSize(c);
    }

    /** Take into account an additional instance of <code>SizeEstimator s</code>
     *  when estimating the size of the <code>MemoryArea</code>.
     */
    public void reserve(SizeEstimator s) {
	estimate += s.estimate;
    }

    /** Take into account additional <code>n</code> instances of <code>SizeEstimator s</code>
     *  when estimating the size of the <code>MemoryArea</code>.
     */
    public void reserve(SizeEstimator s, int n) {
	estimate += n * s.estimate;
    }

    // Not implemented yet
    //    public native static long objSize(Class c);
}
