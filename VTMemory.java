// VTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>VTMemory</code>'s have variable time allocation of memory in a scope.
 *  Currently, I'm ignoring the initial and maximum sizes...
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class VTMemory extends ScopedMemory {

    /** Construct a VTMemory, with an initial size and a maximum size.  
     */
    public VTMemory(long initial, long maximum) {
	super(maximum);
    }

    /** Initialize the native component of this VTMemory. 
     */
    protected native void initNative(long sizeInBytes);

    /** Create a newMemBlock on entry of this VTMemory for a 
     *  particular RealtimeThread. 
     */

    protected native void newMemBlock(RealtimeThread rt);

    /** Return a helpful string describing this VTMemory.
     */
    public String toString() {
	return "VTMemory: " + super.toString();
    }
}
