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
import javax.realtime.LTMemory;

class TestCountingThread0 extends PeriodicThread
{

    public TestCountingThread0(SchedulingParameters sp, ReleaseParameters rp, int id)
    {
	super(sp, rp);
	this.id = id;
    }

    public void ProcessData() throws InterruptedException
    {
	System.out.println(id + ": Start - " + getTime());

	ImageData currentData;
	try
	{
	    currentData = (ImageData)currentScope.newInstance(ImageData.class);
	} catch (Exception e)
	{
	    System.out.println("Exception while creating data structure");
	    e.printStackTrace();
	    return;
	}

	currentScope.setPortal(currentData);
	System.out.println(id + ": set portal: " + currentScope.getPortal());

	for(int i=0;i<500000;i++)
	{
	    currentData.number2 += 1;
	    if(currentData.number2%5000==0)
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

    protected void Initialize()
    {
	currentScope = new LTMemory(50000,50000);
	System.out.println(id + ": New scope: " + currentScope);
    }
}

