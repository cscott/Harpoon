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

abstract class PeriodicThread extends ProcessThread
{
    protected AbsoluteTime start;
    protected boolean wasInterrupted;

    public PeriodicThread(SchedulingParameters sp, ReleaseParameters rp)
    {
	super(sp, rp);

	missHandler = new AsyncEventHandler()
	{
	    public void handleAsyncEvent()
	    {
		int count = getAndClearPendingFireCount();
		System.out.println(id + ": Missed " + count + " deadlines - " + getTime());
		schedulePeriodic();
		interrupt();
	    }
	};

	missHandler.setSchedulingParameters(new PriorityParameters(PriorityScheduler.MAX_PRIORITY));
    }

    public String getTime()
    {
	return (Clock.getRealtimeClock().getTime().subtract(start)).toString();
    }

    public void start()
    {
	start = Clock.getRealtimeClock().getTime();
	super.start();
    }

    public void run()
    {
	System.out.println(id + ": Starting: " + getTime());
	//System.out.println("waitForNextPeriod() returned " + waitForNextPeriod() + " - " + getTime());
	//System.out.println("Running..." + getTime() + "\n");

	for(int i=1;i<=4;i++)
	{
	    System.out.println(id + ": Begin loop..." + getTime());

	    Initialize();
	    System.out.println(id + ": done initializing - " + getTime());

	    wasInterrupted = false;
	    currentScope.enter(new Runnable()
		{
		    public void run()
		    {
			try {ProcessData();}
			catch (InterruptedException e) 
			{
			    System.out.println(id + ": ProcessData interrupted! " + getTime() + "\n...");
			    wasInterrupted = true;
			}
			Finalize();
		    }
		});
	    if(wasInterrupted) continue;

	    System.out.println(id + ": End loop..." + getTime() + "\n...");

	    
	    if(!waitForNextPeriod())
	        System.out.println(id + ": waitForNextPeriod() returned false " + getTime() + "\n...");

	    System.out.println(id + ": Next period - " + getTime());
	}
    }
}
