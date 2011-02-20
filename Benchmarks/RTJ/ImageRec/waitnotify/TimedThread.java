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
import javax.realtime.OneShotTimer;
import javax.realtime.Timer;
import javax.realtime.AbsoluteTime;
import javax.realtime.Clock;
import javax.realtime.BoundAsyncEventHandler;

abstract class TimedThread extends ProcessThread
{
    protected RelativeTime timeout;
    protected Timed aie;
    protected AsyncEventHandler interruptHandler;
    protected OneShotTimer ost;
    public AbsoluteTime start;

    protected String getTime()
    {
	return (Clock.getRealtimeClock().getTime().subtract(start)).toString();
    }

    protected class ProcessLogic implements Interruptible
    {
	public void interruptAction(AsynchronouslyInterruptedException e)
	{ onInterrupted(); }

	public void run(AsynchronouslyInterruptedException e) throws AsynchronouslyInterruptedException
	{ ProcessData(); }
    }
    protected ProcessLogic logic;

    public TimedThread(SchedulingParameters sp, RelativeTime reltime)
    {
	super(sp);
	timeout = reltime;
	logic = new ProcessLogic();
	aie = new Timed(timeout);
	interruptHandler = new AsyncEventHandler()
	    {
		public void handleAsyncEvent()
		{
		    System.out.println(getid() + ": Interrupting..." + getTime());
		    aie.fire();
		}
	    };
	interruptHandler.setSchedulingParameters(new PriorityParameters(265));
	start = Clock.getRealtimeClock().getTime();
	//ost = new OneShotTimer(timeout, interruptHandler);
    }

    public abstract void onInterrupted();

    public abstract void ProcessData() throws AsynchronouslyInterruptedException;

    public void run()
    {
	for(int i=1;i<=10;i++)
	{
	    Initialize();
	    //ost.enable();
	    //ost.start();
	    //System.out.println(getid() + ": OST will fire at " + ost.getFireTime().subtract(start));
	    //aie.resetTime(timeout);
	    aie.doInterruptible(logic);
	    //ost.disable();
	    Finalize();
	}
    }

    protected abstract int getid();
}













