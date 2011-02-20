// SizeEstimator.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
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

    /** Gets an estimate of the number of bytes needed to store
     *  all the objects reserved.
     *
     *  @return The estimate size in bytes.
     */
    public long getEstimate() {
	return estimate;
    }

    /** Take into account additional <code>n</code> instances of <code>Class c</code>
     *  when estimating the size of the <code>MemoryArea</code>.
     *
     *  @param c The class to take into account.
     *  @param n The number of instances of <code>c</code> to estimate.
     */
    public void reserve(Class c, int n) {
	estimate += n * objSize(c);
    }

    /** Take into account an additional instance of <code>SizeEstimator s</code>
     *  when estimating the size of the <code>MemoryArea</code>.
     *
     *  @param size The given instance of <code>SizeEstimator</code>.
     */
    public void reserve(SizeEstimator size) {
	estimate += size.estimate;
    }

    /** Take into account additional <code>n</code> instances of <code>SizeEstimator size</code>
     *  when estimating the size of the <code>MemoryArea</code>.
     *
     *  @param size The given instance of <code>SizeEstimator</code>.
     *  @param n The number of instances of <code>size</code> to estimate.
     */
    public void reserve(SizeEstimator s, int n) {
	estimate += n * s.estimate;
    }

    public native static long objSize(Class c);
}
