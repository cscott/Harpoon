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
 *  memory and reflects the current status of it.
 *  <p>
 *  When a <code>ScopedMemory</code> area is instantiated, the object itself is allocated
 *  from the current memory allocation scheme in use, but the memory space that object
 *  represents is not.  Memory is allocated, as always, using an <code>RTJ_malloc</code> call.
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

    /** Create a new <code>ScopedMemory</code> with the given parameters.
     *
     *  @param size The size of the new <code>ScopedMemory</code> area in bytes.
     *              If size is less than or equal to zero nothing happens.
     */
    public ScopedMemory(long size) {
	super(size);
	portal = null;
	scoped = true;
    }

    /** Create a new <code>ScopedMemory</code> with the given parameters.
     *
     *  @param size The size of the new <code>ScopedMemory</code> area in bytes.
     *              If size is less than or equal to zero nothing happens.
     *  @param r The logic which will use the memory represented by <code>this</code>
     *           as its initial memory area.
     */
    public ScopedMemory(long size, Runnable r) {
	this(size);
	logic = r;
    }

    /** Create a new <code>ScopedMemory</code> with the given parameters.
     *
     *  @param size The size of the new <code>ScopedMemory</code> area estimated
     *              by an instance of <code>SizeEstimator</code>.
     */
    public ScopedMemory(SizeEstimator size) {
	this(size.getEstimate());
    }

    /** Create a new <code>ScopedMemory</code> with the given parameters.
     *
     *  @param size The size of the new <code>ScopedMemory</code> area estimated
     *              by an instance of <code>SizeEstimator</code>.
     *  @param r The logic which will use the memory represented by <code>this</code>
     *           as its initial memory area.
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
     *  of the current instance of <code>Schedulable</code> or the <code>run()</code>
     *  method of the instance of <code>Schedulable</code> fiven in the constructor.
     *  During this bound period of execution, all objects are allocated from the
     *  <code>ScopedMemory</code> area until another one takes effect, or the
     *  <code>enter()</code> method is exited. A runtime exception is thrown if this
     *  method is called from a thread other than a <code>RealtimeThread</code> or
     *  <code>NoHeapRealtimeThread</code>.
     */
    public void enter() {
	// Will NEVER implement single parent rule.
	super.enter();
    }

    /** Associate this <code>ScopedMemory</code> area to the current realtime
     *  thread for the duration of the execution of the <code>run()</code> method
     *  of the current instance of <code>Schedulable</code> or the <code>run()</code>
     *  method of the instance of <code>Schedulable</code> fiven in the constructor.
     *  During this bound period of execution, all objects are allocated from the
     *  <code>ScopedMemory</code> area until another one takes effect, or the
     *  <code>enter()</code> method is exited. A runtime exception is thrown if this
     *  method is called from a thread other than a <code>RealtimeThread</code> or
     *  <code>NoHeapRealtimeThread</code>.
     *
     *  @param logic The runnable object which contains the code to execute.
     */
    public void enter(Runnable logic) {
	// Will NEVER implement single parent rule.
	super.enter(logic);
    }

    /** Get the maximum size this memory area can attain. If this is a fixed size
     *  memory area, the returned value will be equal to the initial size.
     *
     *  @return The maximum size attainable.
     */
    public long getMaximumSize() { 
	return size;
    }
    
    /** Return a reference to the portal object in this instance of <code>ScopedMemory</code>.
     *
     *  @return A reference to the portal object or null if there is no portal object.
     */
    public Object getPortal() {
	return portal;
    }

    /** Returns the reference count of this <code>ScopedMemory</code>. The reference
     *  count is an indication of the number of threads that may have access to this scope.
     *
     *  @return The reference count of this <code>ScopedMemory</code>.
     */
    public int getReferenceCount() {
	return count;
    }

    /** Wait until the reference count of this <code>ScopedMemory</code> goes down to zero.
     *
     *  @throws java.lang.InterruptedException If another thread interrupts this thread
     *                                         while it is waiting.
     */
    public void join() throws InterruptedException {
	// TODO
    }

    /** Wait at most until the time designated by the <code>time</code> parameter for the
     *  reference count of this <code>ScopedMemory</code> to go down to zero.
     *
     *  @param time If this time is an absolute time, the wait is bounded by that point
     *              in time. If the time is a relative time (or a member of the
     *              <code>RationalTime</code> subclass of <code>RelativeTime</code>) the
     *              wait is bounded by the specified interval from some time between the
     *              time <code>join<code> is called and the time it starts waiting for
     *              the reference count to reach zero.
     *  @throws java.lang.InterruptedException If another thread interrupts this thread
     *                                         while it is waiting.
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
     *
     *  @throws java.lang.InterruptedException If another thread interrupts this thread while
     *                                         it is waiting.
     */
    public void joinAndEnter() throws InterruptedException {
	joinAndEnter(this.logic);
    }

    /** Combine <code>join()</code> and <code>enter()</code> such that no <code>enter()</code>
     *  from another thread can intervene between the two method invocations. The resulting
     *  method will wait for the reference count on this <code>ScopedMemory</code> to reach
     *  zero, then enter the <code>ScopedMemory</code> and execute the <code>run()</code>
     *  method from <code>logic</code> passed in the constructor. if no <code>Runnable</code>
     *  was passed, the maethod returns immediately.
     *
     *  @param time The time that bounds the wait.
     *  @throws java.lang.InterruptedException If another thread interrupts this thread while
     *                                         it is waiting.
     */
    public void joinAndEnter(HighResolutionTime time)
	throws InterruptedException {
	joinAndEnter(this.logic, time);
    }

    /** Combine <code>join()</code> and <code>enter()</code> such that no <code>enter()</code>
     *  from another thread can intervene between the two method invocations. The resulting
     *  method will wait for the reference count on this <code>ScopedMemory</code> to reach
     *  zero, then enter the <code>ScopedMemory</code> and execute the <code>run()</code>
     *  method from <code>logic</code> passed in the constructor. if no <code>Runnable</code>
     *  was passed, the maethod returns immediately.
     *
     *  @param logic The <code>java.lang.Runnable</code> object which contains the code to execute.
     *  @throws java.lang.InterruptedException If another thread interrupts this thread while
     *                                         it is waiting.
     */
    public void joinAndEnter(Runnable logic) throws InterruptedException {
	// TODO
    }

    /** Combine <code>join()</code> and <code>enter()</code> such that no <code>enter()</code>
     *  from another thread can intervene between the two method invocations. The resulting
     *  method will wait for the reference count on this <code>ScopedMemory</code> to reach
     *  zero, then enter the <code>ScopedMemory</code> and execute the <code>run()</code>
     *  method from <code>logic</code> passed in the constructor. if no <code>Runnable</code>
     *  was passed, the method returns immediately.
     *
     *  @param logic The <code>java.lang.Runnable</code> object which contains the code to execute.
     *  @param time The time that bounds the wait.
     *  @throws java.lang.InterruptedException If another thread interrupts this thread while
     *                                         it is waiting.
     */
    public void joinAndEnter(Runnable logic, HighResolutionTime time)
	throws InterruptedException {
	// TODO
    }

    /** Set the argument to the portal object in the memory area represented by this instance
     *  of <code>ScopedMemory</code>.
     *
     *  @param object The object which will become the portal for <code>this</code>. If null
     *                the previous portal object remains the portal object for <code>this</code>
     *                or if there was no previous portal object then there is still no
     *                portal object for <code>this</code>.
     */
    public void setPortal(Object object) {
	if (this.equals(MemoryArea.getMemoryArea(object))) portal = object;
    }

    /** Returns a user-friendly representation of this <code>ScopedMemory</code>.
     *
     *  @return The string representation.
     */
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
