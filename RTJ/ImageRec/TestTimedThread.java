import javax.realtime.AsyncEventHandler;
import javax.realtime.ImportanceParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;
import javax.realtime.ReleaseParameters;
import javax.realtime.SchedulingParameters;
import javax.realtime.PriorityParameters;
import javax.realtime.Timed;
import javax.realtime.AsynchronouslyInterruptedException;
import javax.realtime.Interruptible;

class TestTimedThread extends TimedThread
{
    public int number;
    public int id;
    protected long start;
    protected long beginCycle;
    protected long endCycle;

    public TestTimedThread(SchedulingParameters sp, RelativeTime reltime, int id)
    {
	super(sp, reltime);
	number = 0;
	this.id = id;
    }

    protected String getTime()
    {
	return (System.currentTimeMillis()-start)+"ms";
    }

    public void ProcessData() throws AsynchronouslyInterruptedException
    {
	beginCycle = System.currentTimeMillis();
	System.out.println(id + ": In logic - " + getTime());

	for(int i=0;i<1000000;i++)
	{
	    number = (number+3-4+2)*12/3/4;
	    yield();
	}

        System.out.println(id + ": Number: " + number + " - " + getTime());
    }

    public void onInterrupted()
    {
	endCycle = System.currentTimeMillis();
	System.out.println(id + ": Interrupted - " + getTime() + "\nNumber = " + number + "\nTime = " + (endCycle - beginCycle));
    }

    protected void Initialize()
    {
    }

    protected void Finalize()
    {
    }

    public void start()
    {
	start = System.currentTimeMillis();
	super.start();
    }
}


