// VTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>VTMemory</code>'s have variable time allocation of memory in a scope.
 *  Currently, I'm ignoring the initial and maximum sizes...
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class VTMemory extends ScopedMemory {

    /** The logic associated with <code>this</code> */
    Runnable logic;

    /** Construct a VTMemory, with an initial size and a maximum size.  
     */
    public VTMemory(long initialSizeInBytes,
		    long maxSizeInBytes) {
	super(maxSizeInBytes);
    }

    public VTMemory(long initialSizeInBytes,
		    long maxSizeInBytes,
		    Runnable logic) {
	this(initialSizeInBytes, maxSizeInBytes);
	this.logic = logic;
    }

    public VTMemory(SizeEstimator initial, SizeEstimator maximum) {
	// TODO

	// This line inserted only to make everything compile!
	super(maximum);
    }

    public VTMemory(SizeEstimator initial, SizeEstimator maximum,
		    Runnable logic) {
	this(initial, maximum);
	this.logic = logic;
    }

    // CONSTRUCTORS NOT IN SPECS

    /** Alternate constructor, with no limits */
    public VTMemory() {
	super(0);
    }


    // METHODS IN SPECS

    /** Overrides <code>getMaximumSize</code> in class
     *  <code>ScopedMemory</code>.
     */
    public long getMaximumSize() {
	// TODO

	return 0;
    }

    /** Return a helpful string describing this VTMemory.
     */
    public String toString() {
	return "VTMemory: " + super.toString();
    }


    // METHODS NOT IN SPECS

    /** Initialize the native component of this VTMemory. 
     */
    protected native void initNative(long sizeInBytes);
}
