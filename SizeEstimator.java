package javax.realtime;

public final class SizeEstimator {
    /** This is a convenient class to help people figure out
     *  how much memory the need. Instead of passing actual
     *  numbers to the <code>MemoryArea</code> constructors,
     *  one can pass <code>SizeEstimator</code> objects with
     *  which you can have a better feel of how big a memory
     *  area you require.
     */

    /** The estimated number of bytes neede to store all
     *  objects reserved.
     */
    private long estimate;

    public SizeEstimator() {}

    // METHODS IN SPECS

    public long getEstimate() {
	return estimate;
    }

    /** Take into account additional <code>n</code> instances of
     *  <code>Class c</code> when estimating the size of the
     *  <code>MemoryArea</code>.
     */
    public void reserve(Class c, int n) {
	// TODO
    }

    public void reserve(SizeEstimator s) {
	// TODO
    }

    public void reserve(SizeEstimator s, int n) {
	// TODO
    }
}
