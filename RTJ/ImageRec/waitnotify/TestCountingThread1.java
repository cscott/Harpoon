import javax.realtime.AsyncEventHandler;
import javax.realtime.PeriodicParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;
import javax.realtime.ReleaseParameters;
import javax.realtime.SchedulingParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.Clock;
import javax.realtime.PriorityScheduler;
import javax.realtime.AbsoluteTime;

class TestCountingThread1 extends PeriodicThread
{
    public TestCountingThread1(SchedulingParameters sp, ReleaseParameters rp, int id)
    {
	super(sp, rp);
	this.id = id;
    }

    public void ProcessData() throws InterruptedException
    {
	System.out.println(id + ": Start - " + getTime());

	synchronized(lock)
	{
	    lock.notifyAll();
	}

	System.out.println(id + ": Portal: " + currentScope.getPortal());
	ImageData currentData = (ImageData)currentScope.getPortal();
	System.out.println(id + ": currentData: " + currentData);
	if(currentData==null)
	{
	    System.out.println("currentData is null!");
	    return;
	}

	for(int i=0;i<500000;i++)
	{
	    currentData.number1 += 1;
	    if(currentData.number1%5000==0)
	    {
		yield();
		if(interrupted())
		{
		    throw new InterruptedException();
		}
	    }
	}

	System.out.println(id + ": Finished - " + getTime());
    }
}

