// CTMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>CTMemory</code> represents a constant-sized memory scope.  
 *  It uses stack allocation and is very fast and has predictable performance.
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class CTMemory extends ScopedMemory {

    /** Should the memory of this MemoryArea be reused on scope reentry? 
     *  If so, you need to call done() when you're done with the scope. 
     */
    
    private boolean reuse;

    /** This constructs a CTMemory of the appropriate size (the maximum allowed
     *  to be allocated in the scope).  reuse defaults to false.  The performance
     *  hit of allocating a large block of memory is taken when this constructor
     *  is called.  
     */

    public CTMemory(long size) {
	super(size);
	initNative(size, reuse = false);
    }
    
    /** An alternate constructor for CTMemory that allows you to specify whether
     *  you want to reuse the memory associated with the scope on reentry. 
     */

    public CTMemory(long size, boolean reuse) {
	super(size);
	initNative(size, this.reuse = reuse);
    }
    
    /** Returns a representation of this CTMemory object 
     */
    
    public String toString() {
	return "CTMemory: " + super.toString();
    }

    /** Shadow the method called by MemoryArea, we want to specify
     *  an additional argument... 
     */

    protected void initNative(long sizeInBytes) {}

    /** Initialize the native component of this MemoryArea 
     *  (set up the MemBlock) 
     */
    
    private native void initNative(long sizeInBytes, boolean reuse);

    /** Create a newMemBlock and store it in 
     *  getInflatedObject(env, rt)->temp 
     */
    
    protected native void newMemBlock(RealtimeThread rt);

    /** Invoke this method when you're finished with the MemoryArea 
     *	(could be a finalizer if we had finalizers...) 
     */

    public void done() {
	if (reuse) {
	    doneNative();
	}
    }

    /** This will actually free the memory (if refcount = 0). 
     */

    private native void doneNative();
}
