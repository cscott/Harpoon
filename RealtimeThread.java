// RealtimeThread.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime; 

/** <code>RealtimeThread</code>
 * 
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RealtimeThread extends Thread {

    /** Contains the cactus stack of previous memoryAreas... 
     */

    MemAreaStack memAreaStack;

    /** The top of the stack for this RealtimeThread 
     */

    MemAreaStack topStack;

    /** Contains the memoryArea associated with this mem. 
     */

    MemoryArea mem;

    /** Specifies whether this RealtimeThread has access to the heap.
     */

    boolean noHeap;

    /** Specifies whether the initialization code has finished setting up RTJ. 
     */
    
    static boolean RTJ_init_in_progress;

    /** The target to run. */
    
    private Runnable target;

    static void checkInit() {
        if (RTJ_init_in_progress) {
	    System.out.println("Cannot use any MemoryArea except heap until"+
			       "RTJ initialization has completed!");
	    System.exit(-1);
	}
    }

    public RealtimeThread() {  // All RealtimeThreads default to heap.
	this((MemoryArea)null);
    }

    /** */
    
    public RealtimeThread(MemoryArea memory) {
	super();
	target = null;
	mem = (memory==null)?null:(memory.shadow);
	setup();
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
	super(name);
	this.target = target;
	mem = null;
	setup();
    }
    
    /** */

    public RealtimeThread(MemoryArea memory, Runnable target) {
	super();
	this.target = target;
	mem = (memory==null)?null:(memory.shadow);
	setup();
    }

    /** */

    public RealtimeThread(ThreadGroup group, Runnable target) {
	super(group, (Runnable)null);
	this.target = target;
	mem = null;
	setup();
    }
    
    /** */

    public RealtimeThread(ThreadGroup group, Runnable target, String name,
			  MemoryArea mem) {
	super(group, name);
	this.target = target;
	this.mem = (mem==null)?null:(mem.shadow);
	setup();
    }

    /** This sets up the unspecified local variables for the constructor. */

    private void setup() {
	topStack = memAreaStack = null;
	noHeap = false;
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
    
    public void start() {
	if ((mem != null)&&(!mem.heap)) {
	    checkInit();
	}
	RealtimeThread previousThread = currentRealtimeThread();
	topStack = memAreaStack = previousThread.memAreaStack;
	
	MemoryArea newMem = previousThread.memoryArea();
	if (mem == null) {
	    enter(newMem);
	} else {
	    enter(mem);
	}
	mem = memoryArea();
	super.start();// When the run method is called, 
	// RealtimeThread points to the current scope.
	// Note that there is no exit()... this is actually legal.
    }

    /** Override the Thread.run() method, because Thread.run() doesn't work. */
    public void run() {
	if (target != null) {
	    target.run();
	}
    }

    /** For internal use only. */

    public MemoryArea memoryArea() {
	if (mem == null) { // Bypass static initializer problem.
	    mem = HeapMemory.instance();
	}
	return mem;
    }  

    /** */

    public MemoryArea getMemoryArea() {
	if (mem == null) {
	    mem = HeapMemory.instance();
	}
	return mem.original;
    }

    void enter(MemoryArea mem) {
	memAreaStack = MemAreaStack.PUSH(this.mem, memAreaStack);
	(this.mem = mem).enterMemBlock(this, memAreaStack);
    }

    /** */

    void exitMem() {
	mem.exitMemBlock(this, memAreaStack);
	mem = memAreaStack.entry;
	memAreaStack = MemAreaStack.POP(memAreaStack);
    }
    
    /** */

    void cleanup() {
	while (memAreaStack != topStack) {	
	    mem.exitMemBlock(this, memAreaStack);
	    memAreaStack = MemAreaStack.POP(memAreaStack);
	}
    }

    /** Get the outerScope of a given MemoryArea for the current 
     *  RealtimeThread. 
     */

    MemoryArea outerScope(MemoryArea child) {
	MemAreaStack current = memAreaStack.first(child);
	if (current != null) {
	    current = current.next;
	}
	while ((current != null) && (!current.entry.scoped)) {
	    current = current.next;
	}
	if (current == null) {
	    return getMemoryArea();
	} else {
	    return current.entry;
	}
    }

    /** */

    boolean checkAccess(MemoryArea source, MemoryArea target) {
	MemAreaStack sourceStack = (source == memoryArea()) ? 
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



