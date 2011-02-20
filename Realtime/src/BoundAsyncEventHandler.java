// BoundAsyncEventHandler.java, created by Dumitru Daniliuc
// Copyright (C) 2003 Dumitru Daniliuc
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

/** A bound asynchronous event handler is an asynchronous event handler
 *  that is permanently bound to a thread. Bound asynchronous event
 *  handlers are meant for use in situations where the added timeliness
 *  is worth the overhead of binding the handler to a thread.
 */
public abstract class BoundAsyncEventHandler extends AsyncEventHandler {

    protected boolean nonHeap = false;
    protected Runnable logic = null;

    protected SchedulingParameters scheduling;
    protected ReleaseParameters release;
    protected MemoryParameters memory;
    protected ProcessingGroupParameters group;
    protected MemoryArea area;

    /** Create a handler whose parameters are inherited from the current
     *  thread, if it is a <code>RealtimeThread</code>, or null otherwise.
     */
    public BoundAsyncEventHandler() {
	if (Thread.currentThread() instanceof RealtimeThread) {
	    RealtimeThread t = RealtimeThread.currentRealtimeThread();
	    scheduling = t.getSchedulingParameters();
	    release = t.getReleaseParameters();
	    memory = t.getMemoryParameters();
	    group = t.getProcessingGroupParameters();
	    area = t.getMemoryArea();
	}
	else {
	    scheduling = null;
	    release = null;
	    memory = null;
	    group = null;
	    area = null;
	}
    }

    /** Create a handler with the specified parameters.
     *
     *  @param scheduling A <code>SchedulingParameters</code> object which will be
     *                    associated with the constructed instance. If null,
     *                    <code>this</code> will be assigned the reference to the
     *                    <code>SchedulingParameters</code> of the current thread.
     *  @param release A <code>ReleaseParameters</code> object which will be associated
     *                 with the contructed instance. If null, <code>this</code> will
     *                 have no <code>ReleaseParameters</code>.
     *  @param memory A <code>MemoryParameters</code> object which will be associated
     *                with the constructed instance. If null, <code>this</code> will
     *                have no <code>MemoryParameters</code>.
     *  @param area The <code>MemoryArea</code> for <code>this</code>. If null, the
     *              memory area will be that of the current thread.
     *  @param group A <code>ProcessingGroupParameters</code> object which will be
     *               associated with the constructed instance. If null, <code>this</code>
     *               will not be associated with any processing group.
     *  @param nonheap A flag meaning, when true, that this will have characteristics
     *                 identical to a <code>NoHeapRealtimeThread</code>. A false value
     *                 means this will have characteristics identical to a
     *                 <code>RealtimeThread</code>. If true and the current thread is
     *                 <i>not</i> a <code>NoHeapRealtimeThread</code> or a
     *                 <code>RealtimeThread</code> executing within a <code>ScopedMemory</code>
     *                 or <code>ImmortalMemory</code> scope then an
     *                 <code>IllegalArgumentException</code> is thrown.
     *  @param logic The <code>java.lang.Runnable</code> object whose <code>run()</code>
     *               method is executed by <code>AsyncEventHandler.handleAsyncEvent()</code>.
     *  @throws java.lang.IllegalArgumentException If the initial memory area is in heap
     *                                             memory, and the <code>nonheap</code>
     *                                             parameter is true.
     */
    public BoundAsyncEventHandler(SchedulingParameters scheduling,
				  ReleaseParameters release,
				  MemoryParameters memory,
				  MemoryArea area,
				  ProcessingGroupParameters group,
				  boolean nonheap, Runnable logic) {
	this.scheduling = scheduling;
	this.release = release;
	this.memParams = memory;
	this.group = group;
	this.memArea = area;
	this.nonHeap = nonHeap;
	this.logic = logic;
    }
}
