// ScopedMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** <code>ScopedMemory</code> is an abstract class that 
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public abstract class ScopedMemory extends MemoryArea {
    protected Object portal;
    private int count = 0;

    /** <code>logic.run()</code> is executed when <code>enter()</code>
     *  is called.
     */
    protected Runnable logic;

    /** Create a new ScopedMemory of a certain maximum size. 
     */
    public ScopedMemory(long size) {
	super(size);
	portal = null;
	scoped = true;
    }

    public ScopedMemory(long size, Runnable r) {
	this(size);
	logic = r;
    }
    
    public ScopedMemory(SizeEstimator size) {
	this(size.getEstimate());
    }

    public ScopedMemory(SizeEstimator size, Runnable r) {
	this(size);
	logic = r;
    }

    // THIS CONSTRUCTOR IS NOT IN SPECS
    public ScopedMemory(long minimum, long maximum) {
	super(minimum, maximum);
	portal = null;
	scoped = true;
    }

    // METHODS IN SPECS

    public void enter() throws ScopedCycleException {
	// Need to implement single parent rule.
	super.enter();
    }

    public void enter(Runnable logic) throws ScopedCycleException {
	super.enter(logic);
    }

    /** Return the maximum size of this ScopedMemory. 
     */
    public long getMaximumSize() { 
	return size;
    }
    
    /** Get the portal object for this ScopedMemory. 
     */
    public Object getPortal() {
	return portal;
    }
    
    public int getReferenceCount() {
	return count;
    }

    public void join() throws InterruptedException {
	// TODO
    }

    public void join(HighResolutionTime time)
	throws InterruptedException {
	// TODO
    }

    public void joinAndEnter() throws InterruptedException,
				      ScopedCycleException {
	joinAndEnter(this.logic);
    }

    public void joinAndEnter(HighResolutionTime time)
	throws InterruptedException, ScopedCycleException {
	joinAndEnter(this.logic, time);
    }

    public void joinAndEnter(Runnable logic) throws InterruptedException,
						    ScopedCycleException {
	// TODO
    }

    public void joinAndEnter(Runnable logic, HighResolutionTime time)
	throws InterruptedException, ScopedCycleException {
	// TODO
    }

    /** Set the portal object for this ScopedMemory.
     */
    public void setPortal(Object object) {
	portal = object;
    }

    public String toString() {
	// TODO

	return "";
    }


    // METHODS NOT IN SPECS

    
    /** Get the MemoryArea which contains this ScopedMemory for
     *  the current RealtimeThread.
     */
    public MemoryArea getOuterScope() {
	return RealtimeThread.currentRealtimeThread().outerScope(this);
    }

    /** Check to see if this ScopedMemory can have access to 
     *  the given object.
     */
    public void checkAccess(Object obj) { 
	//      Stats.addCheck();
	if (obj != null) {
	    MemoryArea target = getMemoryArea(obj);
	    if ((this != target) && target.scoped &&
		(!RealtimeThread.currentRealtimeThread()
		 .checkAccess(this, target))) {
		throwIllegalAssignmentError(obj, target);
	    }
	}	    
    }

    /** Cannot call this on a ScopedMemory (doesn't cleanup MemBlocks 
     *  appropriately).  Should never need to, since that'll cause
     *  an access violation according to the spec. 
     */
    protected void setupMemBlock(RealtimeThread rt) 
	throws IllegalAccessException {
	throw new IllegalAccessException();
    }
}
