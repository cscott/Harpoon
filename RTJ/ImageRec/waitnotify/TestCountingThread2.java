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

class TestCountingThread2 extends PeriodicThread
{

    public TestCountingThread2(SchedulingParameters sp, ReleaseParameters rp, int id)
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
	    currentData.number0 += 1;
	    if(currentData.number0%5000==0)
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

    protected void Finalize()
    {
	ImageData currentData = (ImageData)currentScope.getPortal();
	System.out.println("RESULT: " + currentData.number0 + " " + currentData.number1 + " " + currentData.number2 + " - " + getTime());
    }
}

