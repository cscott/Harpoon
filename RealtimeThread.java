// RealtimeThread.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime; 

/** <code>RealtimeThread</code>
 * 
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RealtimeThread extends Thread {

    /** Contains all of the previous memoryAreas... */

    private MemAreaStack memAreaStack;

    /** Contains the memoryArea associated with this mem. */

    MemoryArea mem;

    /** Specifies whether this RealtimeThread has access to the heap */

    boolean noHeap;

    public RealtimeThread() {  // All RealtimeThreads default to heap.
	this((MemoryArea)null);
    }

    /** */
    
    public RealtimeThread(MemoryArea memory) {
	this(memory, null);
    }
    
    /** */

    public RealtimeThread(MemoryParameters mp, Runnable target) {
	this(mp.getMemoryArea(), target);
    }

    /** */

    public RealtimeThread(Runnable target) {
	this((MemoryArea)null, target);
    }
    
    /** */

    public RealtimeThread(String name) {
	this((Runnable)null, name);
    }
    
    /** */

    public RealtimeThread(ThreadGroup group, Runnable target, String name) {
	this(group, target, name, null);
    }

    /** */
    
    public RealtimeThread(ThreadGroup group, String name) {
	this(group, null, name);
    }
    
    /** */

    public RealtimeThread(Runnable target, String name) {
	super(target, name);
	mem = null;
	setup();
    }
    
    /** */

    public RealtimeThread(MemoryArea memory, Runnable target) {
	super(target);
	mem = memory;
	setup();
    }

    /** */

    public RealtimeThread(ThreadGroup group, Runnable target) {
	super(group, target);
	mem = null;
	setup();
    }
    
    /** */

    public RealtimeThread(ThreadGroup group, Runnable target, String name,
			  MemoryArea mem) {
	super(group, target, name);
	this.mem = mem;
	setup();
    }

    /** This sets up the unspecified local variables for the constructor. */

    private void setup() {
	memAreaStack = new MemAreaStack();
	noHeap = false;
	previousThread = null;
    }

    /** */

    public static final int MIN_PRIORITY = Thread.MIN_PRIORITY;

    /** */

    public static final int NORM_PRIORITY = Thread.NORM_PRIORITY;

    /** */

    public static final int MAX_PRIORITY = Thread.MAX_PRIORITY;
    
    /** */

    public static int activeCount() {
	return Thread.activeCount();
    }

    /** */    

    public static Thread currentThread() {
	return Thread.currentThread();
    }

    /** */
    
    public static int enumerate(Thread tarray[]) {
	return Thread.enumerate(tarray);
    }
    
    /** */

    public static boolean interrupted() {
	return Thread.interrupted();
    }
    
    /** */

    public static void yield() {
	Thread.yield();
    }
    
    /** */

    public static void sleep(long millis) throws InterruptedException {
	Thread.sleep(millis);
    }
    
    /** */

    public static void sleep(long millis, int nanos) 
	throws InterruptedException {
	Thread.sleep(millis, nanos);
    }
    
    /** */

    public static void dumpStack() {
	System.out.println("MemoryArea stack:");
	System.out.println(currentRealtimeThread().memAreaStack.toString());
	Thread.dumpStack();
    }
    
    // The following methods are part of the RTJ spec.

    /** */
    
    public static RealtimeThread currentRealtimeThread() {
	return (RealtimeThread)Thread.currentThread();
    }
    
    RealtimeThread previousThread;

    public void start() {
	previousThread = currentRealtimeThread();
	memAreaStack = previousThread.memAreaStack;
	
	MemoryArea newMem = previousThread.getMemoryArea();
	if (mem == null) {
	    enter(newMem, false);
	} else {
	    enter(mem, newMem.scoped && (mem != newMem));
	}
	mem = getMemoryArea();
	super.start();// When the run method is called, 
	// RealtimeThread points to the current scope.
	// Note that there is no exit()... this is actually legal.
    }

    /** */

    public MemoryArea getMemoryArea() {
	if (mem == null) { // Bypass static initializer problem.
	    mem = HeapMemory.instance();
	}
	return mem;
    }  

    /** */
   
    void enterFromMemArea(MemoryArea mem, boolean checkStack) {
	previousThread = this;
	enter(mem, checkStack);
    }

    /** */

    private void enter(MemoryArea mem, boolean checkStack) {
	memAreaStack = 
	    new MemAreaStack(previousThread.getMemoryArea(), memAreaStack);
	if (checkStack && (memAreaStack.first(mem) != null)) {
	    throw new MemoryScopeError("Re-entry of previous scope.");
	}
	this.mem = mem;
	mem.enterMemBlock(this);
    }

    /** */

    void exit() {
	mem.exitMemBlock(this);
	this.mem = memAreaStack.entry;
	memAreaStack = memAreaStack.next;
    }
    
    /** */

    MemoryArea outerScope(MemoryArea child) {
	MemAreaStack current = memAreaStack.first(child);
	while ((current != null) && (!current.entry.scoped)) {
	    current = current.next;
	}
	if (current == null) {
	    return null;
	} else {
	    return current.entry;
	}
    }

    /** */

    boolean checkAccess(MemoryArea source, MemoryArea target) {
	MemAreaStack sourceStack = (source == getMemoryArea()) ? 
	    memAreaStack : memAreaStack.first(source);
	return (sourceStack != null) && (sourceStack.first(target) != null);
    }

    /** */

    public void checkNoHeapWrite(Object obj) {}

    /** */

    public void checkNoHeapRead(Object obj) {}

    /** */
    
    public String toString() {
	return "RealtimeThread";
    }

}



