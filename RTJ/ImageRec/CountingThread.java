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

class CountingThread extends ProcessThread
{
    public int number;
    protected AbsoluteTime start;
    protected boolean wasInterrupted;

    public CountingThread(SchedulingParameters sp, ReleaseParameters rp)
    {
	super(sp, rp);
	number = 0;

	missHandler = new AsyncEventHandler()
	{
	    public void handleAsyncEvent()
	    {
		int count = getAndClearPendingFireCount();
		System.out.println("Missed " + count + " deadlines - " + getTime());
		schedulePeriodic();
		interrupt();
	    }
	};

	missHandler.setSchedulingParameters(new PriorityParameters(PriorityScheduler.MAX_PRIORITY));
    }

    public String getTime()
    {
	return (Clock.getRealtimeClock().getTime().subtract(start)).toString() + "ms";
    }

    public void ProcessData() throws InterruptedException
    {
	System.out.println("Start - " + getTime());

	for(int i=0;i<500000;i++)
	{
	    number += 1;
	    if(number%5000==0)
	    {
		yield();
		if(interrupted())
		{
		    throw new InterruptedException();
		}
	    }
	}

	System.out.println("Finished - " + getTime() + " - number = " + number);
    }

    protected void Initialize()
    {
    }

    protected void Finalize()
    {
    }

    public void start()
    {
	start = Clock.getRealtimeClock().getTime();
	super.start();
    }

    public void run()
    {
	System.out.println("Starting: " + getTime());
	System.out.println("waitForNextPeriod() returned " + waitForNextPeriod() + " - " + getTime());
	System.out.println("Running..." + getTime() + "\n");

	for(int i=1;i<=10;i++)
	{
	    System.out.println("Begin loop..." + getTime());

	    Initialize();
	    try {ProcessData();}
	    catch (InterruptedException e) 
	    {
		System.out.println("ProcessData interrupted! " + getTime() + " - number = " + number + "\n");
		Finalize();
		continue;
	    }
	    Finalize();

	    System.out.println("End loop..." + getTime() + "\n");

	    if(!waitForNextPeriod())
		System.out.println("waitForNextPeriod() returned false " + getTime() + "\n");

	    System.out.println("Next period - " + getTime());
	}
    }
}
