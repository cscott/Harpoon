package harpoon.Analysis.ContBuilder;

import java.io.NativeIO;

import java.util.Vector;

public final class Scheduler
{
    public  static Thread currentThread;

    public static boolean switchThreads;

    private static int nThreads;

    static {
	System.out.println("Scheduler: using select.");
	NativeIO.initScheduler(NativeIO.MOD_SELECT);

	currentThread= Thread.currentThread();
	switchThreads=false;
	nThreads= 1;
    }

   
    public static int MAX_FD= 5000;
    
    // these two map fds (indices) to Threads that are blocked on them
    private static Thread[] rTable= new Thread[MAX_FD], wTable= new Thread[MAX_FD];
    private static int[] fdarray= new int[MAX_FD];

    private static int nBlockedIO= 0;

    public static Thread readyThreads= null;

    // current thread has blocked with given continuation
    // must be called AFTER current Thread added to one of the blocked lists (I/O, thread join, etc.)
    public static void blocked(VoidResultContinuation c) {
	currentThread.cc= c;
	switchThreads=true;
    }

    // Invariant: very important: after addR/W, code MUST return to the Scheduler ASAP
    // that IS normal behaviour, but especially important now
    public static void addRead(IOContinuation c)
    {
	int fd= c.getFD().fd-1;
	if (rTable[fd] != null) 
	    throw new RuntimeException("addRead: unexpected: two Continuations reading from the same FD");

	nBlockedIO++;
	rTable[fd]= currentThread;
	NativeIO.registerRead(fd);

	blocked(c);
    }
	
    public static void addWrite(IOContinuation c)
    {
	int fd= c.getFD().fd-1;
	if (wTable[fd] != null) 
	    throw new RuntimeException("addWrite: unexpected: two Continuations writing to the same FD");

	nBlockedIO++;
	wTable[fd]= currentThread;
	NativeIO.registerWrite(fd);
	
	blocked(c);
    }

    public static void addReady(VoidResultContinuation c)
    {
	if (currentThread.cc != null)
	    throw new RuntimeException("unexpected: two consecutive addReady's");
	currentThread.cc= c;
    }


    // warning: will treat given thread as the head of a list
    // but: currently needed only by join
    public static void addReadyThreadList(Thread t)
    {
	Thread i;

	for (i= t; t.link != null; t= t.link);

	i.link= readyThreads;
	readyThreads= t;
    }

    public static void addReadyThread(Thread t)
    {

	t.link= readyThreads;
	readyThreads= t;
    }

    public static void newThread(Thread t) 
    {
	nThreads++;
	t.cc= new SchedulerThreadC();
	addReadyThread(t);
    }
	
    public static void loop()
    {
	while(nThreads > 0) 
	    if (switchThreads==false)
		if (currentThread.cc != null) {
		    // System.out.println("2");
		    //System.out.println(currentThread);
		    VoidResultContinuation tmp= currentThread.cc;
		    //System.out.println(tmp);
		    //System.out.println("3");
		    currentThread.cc= null;
		    //System.out.println("4");
		    tmp.resume();
		    //System.out.println("5");
		}
		else {
		    //System.out.println("6");
		    Thread tt= currentThread;
		    //System.out.println("7");
		    switchThreads=true;
		    //System.out.println("8");
		    nThreads--;
		    //System.out.println("9");
		    tt.die();
		    //System.out.println("10");
		}
	    else if (readyThreads != null) {
		//System.out.println("11");
		currentThread= readyThreads;
		switchThreads=false;
		//System.out.println("12");
		readyThreads= currentThread.link;
		//System.out.println("13");
		currentThread.link= null;
		//System.out.println("14");
	    }
	    else {
		//System.out.println("15");
		getFDs();
		//System.out.println("0");
	    }
	//System.out.println("1");
    }	
	

    private static void getFDs() {
	int fdsize= NativeIO.getFDs(fdarray);


        Thread[] table= rTable;

	for (int i= 0; i < fdsize; i++)
	    if (fdarray[i] != -1) { 
	        Thread t= table[fdarray[i]];
		addReadyThread(t);
		table[fdarray[i]]= null;
		nBlockedIO --;
	    }
	    else table= wTable;
    }
}

class SchedulerThreadC extends VoidContinuation implements VoidResultContinuation {
    
    public void resume() {
	Scheduler.currentThread.run_Async();
    }
    
    public void exception(Throwable t) {
    }
    
    private Continuation link;
    
    public void setLink(Continuation newLink) { 
	link= newLink;
    }
    
    public Continuation getLink() { 
	return link;
    }
}


