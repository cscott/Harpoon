// Scheduler.java, created Wed Mar 22 02:59:54 EST 2000 by govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.ContBuilder;

/**
 * <code>Scheduler</code>
 *
 * @author unknown and P.Govereau govereau@mit.edu
 * @version $Id: Scheduler.java,v 1.1 2000-03-24 02:09:35 govereau Exp $
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
		currentThread = Thread.currentThread();
		nThreads = 1;

			// The number of workers should be tuned for the application
		(new AcceptWorker()).start();
		(new ReadWorker()).start();
		(new ReadWorker()).start();
		(new WriteWorker()).start();
		(new WriteWorker()).start();
		currentThread.yield();
    }

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
		acceptRequests.addRequest(req);
		blocked(cont);
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
		readRequests.addRequest(req);
		blocked(cont);
		synchronized (readRequests) {
			readRequests.notify();
		}
		return cont;
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
		writeRequests.addRequest(req);
		blocked(cont);
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
				Thread.currentThread().yield();
			}
		}
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
		System.err.println(t);
	}
}


