package javax.realtime.threadtests;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1999, 2000  All Rights Reserved
 */

import javax.realtime.*;

public class ThreadBasher implements Runnable
{

	static RealtimeThread[] threads;
	static PriorityParameters[] pri;
	static int threadNumber = 0;
	static int numThreads;

	/**
 	 * 
 	 */
	public ThreadBasher() 
	{
		super();
	}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	final long startTime = System.currentTimeMillis();
	boolean gotNumThreads = false;
	int priority;
	RealtimeThread thread;

	numThreads = 0;
		
	if (args.length == 1) {
		try {
			numThreads = Integer.parseInt(args[0]);
			if (numThreads > 0) gotNumThreads = true;
		} catch (NumberFormatException e) {}
	}
	
	if (!gotNumThreads) {
		System.out.println("Usage: javax.realtime.threadtests.ThreadBasher [numthreads]");
		System.out.println("Using default number of threads (28)");		
		numThreads = 28;
		gotNumThreads = true;
	}
	
	priority = RealtimeThread.RT_MIN_PRIORITY;
	ThreadBasher app = new ThreadBasher();
	threads	= new RealtimeThread[numThreads];
	pri = new PriorityParameters[numThreads];
	ReleaseParameters rel = null;
	MemoryArea area = HeapMemory.instance();
	MemoryParameters mem = new MemoryParameters(area);
	ProcessingGroupParameters group = null;
	

	for (int i = 0; i < numThreads; i++) {
		pri[i] = new PriorityParameters(priority);
		threads[i] = new RealtimeThread(pri[i], rel, mem, area, group, app);
		threads[i].start();

		if (++priority > RealtimeThread.RT_MAX_PRIORITY) {
			priority = RealtimeThread.RT_MIN_PRIORITY;
		}
	}

	for (int i = numThreads - 1; i >= 0; i--)
		try {
			threads[i].join();
		} catch (InterruptedException ie) {}
		
	println ("All threads have completed.");
	println ("ThreadBasher with " + numThreads + " threads took " + (System.currentTimeMillis() - startTime) + " milliseconds to complete.");
	
}

static synchronized int assignThreadNumber () {
	return threadNumber++;
}

/**
 * Synchronizes this test's outputs to System.out.
 *
 * @param s the String to be printed
 */
public static synchronized void println(String s) {
	System.out.println(s);
}

/**
 *
 */
public void run() {
	int threadNumber = assignThreadNumber();
		
	int iterations = 10000;
	RealtimeThread thread = RealtimeThread.currentRealtimeThread();

	println("Java ThreadBasher '" + thread.getName() + "' (" +
		((PriorityParameters)thread.getSchedulingParameters()).getPriority() + ") - starting");

	while (iterations>0) {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {}
		if ((iterations%5000)==0)
		{
			println("Java ThreadBasher '" + thread.getName() + "' (Priority: " +
					((PriorityParameters)thread.getSchedulingParameters()).getPriority() + ") - run(): iterations left="+iterations);
		
		}
		iterations--;				
	}

	println("Java ThreadBasher '" + thread.getName() + "' (" +
		((PriorityParameters)thread.getSchedulingParameters()).getPriority() + ") - complete");
}
}
