// Scheduler.java, created Wed Mar 22 02:59:54 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.ContBuilder;

import java.io.NativeIO;

import java.util.Vector;
/**
 * <code>Scheduler</code>
 *
 * @author unknown and P.Govereau govereau@mit.edu
 * @version $Id: Scheduler.java,v 1.4 2000-04-03 04:43:18 bdemsky Exp $
 */
public final class Scheduler
{
    private static int nBlockedIO = 0;
    private static int nThreads;

    public static Thread currentThread;
    public static Thread readyThreads = null;

    public static AsyncRequests acceptRequests = new AsyncRequests();
    public static AsyncRequests readRequests = new AsyncRequests();
    public static AsyncRequests writeRequests = new AsyncRequests();

    static {
	System.out.println("Scheduler: using worker threads.");

	NativeIO.initScheduler(NativeIO.MOD_SELECT);
	currentThread = Thread.currentThread();
	nThreads = 1;
	
	// The number of workers should be tuned for the application
	//(new AcceptWorker()).start();
	(new ReadWorker()).start();
	(new ReadWorker()).start();
	(new ReadWorker()).start();
	(new ReadWorker()).start();
	(new ReadWorker()).start();
	(new ReadWorker()).start();
	(new WriteWorker()).start();
	(new WriteWorker()).start();

	currentThread.yield();
    }


    public static int MAX_FD= 5000;
    
    // these two map fds (indices) to Threads that are blocked on them
    private static Thread[] rTable= new Thread[MAX_FD], wTable= new Thread[MAX_FD];

    private static int[] fdarray=new int[MAX_FD];
    
    /**
     * current thread has blocked with given continuation
     * must be called AFTER current Thread added to one of the
     * blocked lists (I/O, thread join, etc.)
     */
    public static void blocked(VoidResultContinuation c) {
	currentThread.cc = c;
	currentThread = null;
	nBlockedIO++;
    }
    
    /**
     * Adds a new accept request to te accept request queue.
     * Blocks current async thread
     *
     * @param req the request to queue
     * @return first continuation to execute after accept
     */
    public static ObjectDoneContinuation addAccept(AsyncRequest req) {
	ObjectDoneContinuation cont = new ObjectDoneContinuation();
	blocked(cont);
	acceptRequests.addRequest(req);
	synchronized (acceptRequests) {
	    acceptRequests.notify();
	}
	return cont;
    }		
    
    /**
     * Adds a new read request to read request queue.
     * Blocks current async thread
     *
     * @param req the request to queue
     * @return first continuation to execute after accept
     */
    public static IntDoneContinuation addRead(AsyncRequest req) {
	IntDoneContinuation cont = new IntDoneContinuation();
	blocked(cont);
	readRequests.addRequest(req);
	synchronized (readRequests) {
	    readRequests.notify();
	}
	return cont;
    }
    
    // Invariant: very important: after addR/W, code MUST return to the Scheduler ASAP
    // that IS normal behaviour, but especially important now
    public static void addReadA(IOContinuation c) {
	int fd= c.getFD().fd-1;
	if (rTable[fd] != null) 
	    throw new RuntimeException("addRead: unexpected: two Continuations reading from the same FD");
	
	nBlockedIO++;
	rTable[fd]= currentThread;
	NativeIO.registerRead(fd);
	
	blocked(c);
    }
    
    public static void addWriteA(IOContinuation c) {
	int fd= c.getFD().fd-1;
	if (wTable[fd] != null) 
	    throw new RuntimeException("addWrite: unexpected: two Continuations writing to the same FD");
	
	nBlockedIO++;
	wTable[fd]= currentThread;
	NativeIO.registerWrite(fd);
	
	blocked(c);
    }

    
    /**
     * Adds a write request to the write queue.
     * Blocks current async thread
     *
     * @param req the request to queue
     * @return first continuation to execute after accept
     */		   
    public static VoidDoneContinuation addWrite(AsyncRequest req) {
	VoidDoneContinuation cont = new VoidDoneContinuation();
	blocked(cont);
	writeRequests.addRequest(req);
	synchronized (writeRequests) {
	    writeRequests.notify();
	}
	return cont;
    }
    
    /**
     * Add continuation to current thread
     */
    public static void addReady(VoidResultContinuation c) {
	if (currentThread.cc != null)
	    throw new RuntimeException("unexpected: two consecutive addReady's");
	currentThread.cc = c;
    }
    
    
    /**
     * Adds a list of threads to the ready list.
     * warning: will treat given thread as the head of a list
     * but: currently needed only by join
     */
    public static synchronized void addReadyThreadList(Thread t) {
	Thread i;
	for (i = t; t.link != null; t = t.link);
	i.link = readyThreads;
	readyThreads = t;
    }
    
    /**
     * Adds a thread to the ready list.
     */
    public static synchronized void addReadyThread(Thread t) {
	t.link = readyThreads;
	readyThreads = t;
    }
    
    /**
     * creates a new asyn thread, attaches a SchedulerThreadC continuation
     * to it, and schedules the thread for execution.
     */
    public static void newThread(Thread t) {
	nThreads++;
	t.cc = new SchedulerThreadC();
	addReadyThread(t);
    }
    
    /**
     * main scheduler loop -- to be called by main after initial
     * continuations are scheduled
     */
    public static void loop() {
	while(nThreads > 0) {
	    if (currentThread != null) {
		if (currentThread.cc != null) {
		    VoidResultContinuation tmp = currentThread.cc;
		    currentThread.cc = null;
		    tmp.resume();
		} else {
		    Thread tt = currentThread;
		    currentThread = null;
		    nThreads--;
		    tt.die();
		}
	    } else if (readyThreads != null) {
		currentThread = readyThreads;
		readyThreads = currentThread.link;
		currentThread.link = null;
	    } else {
		if (!getFDs()&&(readyThreads==null))
		    Thread.currentThread().yield();
	    }
	}
    }

    private static boolean getFDs() {
	int fdsize= NativeIO.getFDsSmart(false,fdarray);
	boolean xx=false;
        Thread[] table= rTable;
	
	for (int i= 0; i < fdsize; i++)
	    if (fdarray[i] != -1) { 
	        Thread t= table[fdarray[i]];
		addReadyThread(t);
		table[fdarray[i]]= null;
		nBlockedIO --;
		xx=true;
	    }
	    else table= wTable;
	return xx;
    }
    
}

/**
 * <code>SchedulerThreadC</code>
 * schedules new "threads"
 */
class SchedulerThreadC
    extends VoidContinuation
    implements VoidResultContinuation
{
    public void resume() {
	Scheduler.currentThread.run_Async();
    }
    
    public void exception(Throwable t) {
	System.err.println("STC"+t);
    }
}






