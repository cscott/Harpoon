import javax.realtime.AsyncEventHandler;
import javax.realtime.PeriodicParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;
import javax.realtime.ReleaseParameters;
import javax.realtime.SchedulingParameters;
import javax.realtime.PriorityParameters;
import java.util.Vector;
import javax.realtime.ScopedMemory;
import javax.realtime.LTMemory;

abstract class ProcessThread extends RealtimeThread
{
    protected Vector dataQueue;
    protected LTMemory currentScope;
    protected ProcessThread next;
    protected int id;
    public Object lock = new Object();

    public AsyncEventHandler missHandler;

    public ProcessThread(SchedulingParameters sp)
    {
	super(sp);
      	dataQueue = new Vector();
    }

    public ProcessThread(SchedulingParameters sp, ReleaseParameters rp)
    {
	super(sp,rp);
	dataQueue = new Vector();
    }

    public void SetNextThread(ProcessThread nextThread)
    {
	next = nextThread;
    }

    public void Enqueue(ScopedMemory data)
    {
    	dataQueue.add(data);
    }

    protected void Initialize()
    {
	while( dataQueue.isEmpty() )
	{
	    try
	    {
		synchronized(lock)
		{
		    System.out.println(id + ": waiting...");
		    lock.wait();
		    System.out.println(id + ": done waiting");
		}
	    } catch (InterruptedException e)
	    {
		System.out.println("Interrupted while waiting for queue to fill");
	    }
	}

	currentScope = (LTMemory)dataQueue.remove(0);
	System.out.println(id + ": currentScope: " + currentScope);
    }

    public abstract void ProcessData() throws InterruptedException;

    protected synchronized void Finalize()
    {
	next.Enqueue(currentScope);
	synchronized(next.lock)
	{
	    next.lock.notifyAll();
	}

     	/*synchronized(next.lock)
	{
	    try
	    {
		System.out.println(id + ": waiting for next thread...");
		next.lock.wait();
		System.out.println(id + ": resuming execution...");
	    } catch (InterruptedException e)
	    {
		System.out.println("Interrupted while waiting for next thread to pick up scope.");
	    }
	}*/
    }

    public abstract void run();
}


