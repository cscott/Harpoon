// ScopedMemory.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/**
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

/** <code>ScopedMemory</code> is the abstract base class of all classes dealing
 *  with representations of memoryspaces with a limited lifetime. The
 *  <code>ScopedMemory</code> area is valid as long as there are real-time
 *  threads with access to it. A reference is created for each accessor when
 *  either a real-time thread is created with the <code>ScopedMemory</code>
 *  object as its memory area, or a real-time thread runs the <code>enter()</code>
 *  method for the memory area. When the last reference to the object is removed,
 *  by exiting the thread or exiting the <code>enter()</code> method, finalizers
 *  are run for all objects in the memory area, and the area is emptied.
 *  <p>
 *  A <code>ScopedMemory</code> area is a connection to a particular region of
 *  memory and reflects the current status of it. The object does not necessarily
 *  contain direct references to the region of memory that is implementation dependent.
 *  <p>
 *  Whe a <code>ScopedMemory</code> area is instantiated, the object itself is allocated
 *  from the current memory allocation scheme in use, but the memory space that object
 *  represents is not. Typically, the memory for a <code>ScopedMemory</code> area might
 *  be allocated using native method implementations that make appropriate use of
 *  <code>malloc()</code> and <code>free()</code> or similar routines to manipulate memory.
 *  <p>
 *  The <code>enter()</code> method of <code>ScopedMemory</code> is the mechanism used
 *  to activate a new memory scope. Entry into the scope is done by calling the method
 *  <code>enter(Runnable r)</code>, where <code>r</code> is a <code>Runnable</code>
 *  object whose <code>run()</code> method represents the entry point to the code that
 *  will run in the new scope. Exit from the scope occurs when the <code>r.run()</code>
 *  completes. Allocations of objects within <code>r.run()</code> are done with the
 *  <code>ScopedMemory</code> area. When <code>r.run()</code> is complete, the scoped
 *  memory area is no longer active. Its reference count will be decremented and if it
 *  is zero all of objects in the memory are finalized and collected.
 *  <p>
 *  Objects allocated from a <code>ScopedMemory</code> area have a unique lifetime. They
 *  cease to exist on exiting <code>enter()</code> method or upon exiting the last
 *  real-time thread referencing the area, regardless of any references that may exist
 *  to the object. Thus, to maintain the safety of Java and avoid dangling references,
 *  a very restrictive set of rules apply to <code>ScopedMemory</code> area objects:
 *  <ul>
 *  <li>1. A reference to an object in <code>ScopedMemory</code> can never be stored in
 *         an Object allocated in the Java heap.
 *  <li>2. A reference to an object in <code>ScopedMemory</code> can never be stored in
 *         an Object allocated in <code>ImmortalMemory</code>.
 *  <li>3. A reference to an object in <code>ScopedMemory</code> can only be stored in
 *         Objects allocated in the same <code>ScopedMemory</code> area, or into a
 *         -- more inner -- <code>ScopedMemory</code> area nested by the use of its
 *         <code>enter()</code> method.
 *  <li>4. References to immortal of heap objects <i>may</i> be stored into an object
 *         allocated in a <code>ScopedMemory</code> area.
 *  </ul>
 */
public abstract class ScopedMemory extends MemoryArea {
    protected Object portal;
    private int count = 0;

    /** <code>logic.run()</code> is executed when <code>enter()</code> is called. */
    protected Runnable logic;

    /** Create a new <code>ScopedMemory</code> of size <code>size</code>. */
    public ScopedMemory(long size) {
	super(size);
	portal = null;
	scoped = true;
    }

    /** Create a new <code>ScopedMemory</code> of size <code>size</code> and
     *  that executes <code>r.run()</code> when <code>enter()</code> is called.
     */
    public ScopedMemory(long size, Runnable r) {
	this(size);
	logic = r;
    }

    /** Create a new <code>ScopedMemory</code> with size equal to
     *  <code>size.getEstimate()</code>.
     */
    public ScopedMemory(SizeEstimator size) {
	this(size.getEstimate());
    }

    /** Create a new <code>ScopedMemory</code> with size equal to
     *  <code>size.getEstimate()</code> and that executes <code>r.run()</code>
     *  when <code>enter()</code> is called.
     */
    public ScopedMemory(SizeEstimator size, Runnable r) {
	this(size);
	logic = r;
    }

    public ScopedMemory(long minimum, long maximum) {
	super(minimum, maximum);
	portal = null;
	scoped = true;
    }

    /** Associate this <code>ScopedMemory</code> area to the current realtime
     *  thread for the duration of the execution of the <code>run()</code> method
     *  of the given <code>java.lang.Runnable</code>. During this bound period of
     *  execution, all objects are allocated from the <code>ScopedMemory</code>
     *  area until another one takes effect, or the <code>enter()</code> method
     *  is exited. A runtime exception is thrown if this method is called from a
     *  thread other than a <code>RealtimeThread</code> or <code>NoHeapRealtimeThrea</code>.
     */
    public void enter() throws ScopedCycleException {
	// Need to implement single parent rule.
	super.enter();
    }

    /** Associate this <code>ScopedMemory</code> area to the current realtime
     *  thread for the duration of the execution of the <code>run()</code> method
     *  of the given <code>java.lang.Runnable</code>. During this bound period of
     *  execution, all objects are allocated from the <code>ScopedMemory</code>
     *  area until another one takes effect, or the <code>enter()</code> method
     *  is exited. A runtime exception is thrown if this method is called from a
     *  thread other than a <code>RealtimeThread</code> or <code>NoHeapRealtimeThrea</code>.
     */
    public void enter(Runnable logic) throws ScopedCycleException {
	super.enter(logic);
    }

    /** Get the maximum size this memory area can attain. If this is a fixed size
     *  memory area, the returned value will be equal to the initial size.
     */
    public long getMaximumSize() { 
	return size;
    }
    
    /** Return a reference to the portal object in this instance of <code>ScopedMemory</code>. */
    public Object getPortal() {
	return portal;
    }

    /** Returns the reference count of this <code>ScopedMemory</code>. The reference
     *  count is an indication of the number of threads that may have access to this scope.
     */
    public int getReferenceCount() {
	return count;
    }

    /** Wait until the reference count of this <code>ScopedMemory</code> goes down to zero. */
    public void join() throws InterruptedException {
	// TODO
    }

    /** Wait at most until the time designated by the <code>time</code> for the reference
     *  count of this <code>ScopedMemory</code> to go down to zero.
     */
    public void join(HighResolutionTime time)
	throws InterruptedException {
	// TODO
    }

    /** Combine <code>join()</code> and <code>enter()</code> such that no <code>enter()</code>
     *  from another thread can intervene between the two method invocations. The resulting
     *  method will wait for the reference count on this <code>ScopedMemory</code> to reach
     *  zero, then enter the <code>ScopedMemory</code> and execute the <code>run()</code>
     *  method from <code>logic</code> passed in the constructor. if no <code>Runnable</code>
     *  was passed, the maethod returns immediately.
     */
    public void joinAndEnter() throws InterruptedException,
				      ScopedCycleException {
	joinAndEnter(this.logic);
    }

    /** Combine <code>join()</code> and <code>enter()</code> such that no <code>enter()</code>
     *  from another thread can intervene between the two method invocations. The resulting
     *  method will wait for the reference count on this <code>ScopedMemory</code> to reach
     *  zero, then enter the <code>ScopedMemory</code> and execute the <code>run()</code>
     *  method from <code>logic</code> passed in the constructor. if no <code>Runnable</code>
     *  was passed, the maethod returns immediately.
     */
    public void joinAndEnter(HighResolutionTime time)
	throws InterruptedException, ScopedCycleException {
	joinAndEnter(this.logic, time);
    }

    /** Combine <code>join()</code> and <code>enter()</code> such that no <code>enter()</code>
     *  from another thread can intervene between the two method invocations. The resulting
     *  method will wait for the reference count on this <code>ScopedMemory</code> to reach
     *  zero, then enter the <code>ScopedMemory</code> and execute the <code>run()</code>
     *  method from <code>logic</code> passed in the constructor. if no <code>Runnable</code>
     *  was passed, the maethod returns immediately.
     */
    public void joinAndEnter(Runnable logic) throws InterruptedException,
						    ScopedCycleException {
	// TODO
    }

    /** Combine <code>join()</code> and <code>enter()</code> such that no <code>enter()</code>
     *  from another thread can intervene between the two method invocations. The resulting
     *  method will wait for the reference count on this <code>ScopedMemory</code> to reach
     *  zero, then enter the <code>ScopedMemory</code> and execute the <code>run()</code>
     *  method from <code>logic</code> passed in the constructor. if no <code>Runnable</code>
     *  was passed, the maethod returns immediately.
     */
    public void joinAndEnter(Runnable logic, HighResolutionTime time)
	throws InterruptedException, ScopedCycleException {
	// TODO
    }

    /** Set the argument to the portal object in the memory area represented by this instance
     *  of <code>ScopedMemory</code>.
     *  <p>
     *  A portal can serve as a means of interthread communication and they are used primarily
     *  when threads need to share an object that is allocated in a <code>ScopedMemory</code>.
     *  The portal object for a <code>ScopedMemory</code> must be allocated in the same
     *  <code>ScopedMemory</code>. Thus the following condition has to evaluate to <code>true</code>
     *  for the portal to be set: <code>this.equals(MemoryArea.getMemoryArea(object))</code>.
     */
    public void setPortal(Object object) {
	if  (this.equals(MemoryArea.getMemoryArea(object))) portal = object;
    }

    /** Returns a user-friendly representation of this <code>ScopedMemory</code>. */
    public String toString() {
	return "ScopedMemory: " + super.toString();
    }

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
