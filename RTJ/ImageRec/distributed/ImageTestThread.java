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

class ImageTestThread extends PeriodicThread
{
    public static native void writePPM(String filename, int[] data);
    protected int runCount;

    static
    {
	System.loadLibrary("readJPEG");
    }

    public ImageTestThread(SchedulingParameters sp, ReleaseParameters rp, int id)
    {
	super(sp, rp);
	this.id = id;
	runCount=0;
    }

    public void ProcessData() throws InterruptedException
    {
	System.out.println(id + ": Start - " + getTime());

	System.out.println(id + ": Portal: " + currentScope.getPortal());
	ImageData currentData = (ImageData)currentScope.getPortal();
	System.out.println(id + ": currentData: " + currentData);
	if(currentData==null)
	{
	    System.out.println("currentData is null!");
	    return;
	}

	writePPM("image"+runCount+".ppm", currentData.outvals);
	runCount++;

	System.out.println(id + ": Finished - " + getTime());
	yield();
	throw new InterruptedException();
    }

    protected void Finalize()
    {
	System.out.println(id + ": Memory: " + (currentScope.memoryConsumed()/1000) + "K used, " + (currentScope.memoryRemaining()/1000) + "K free");
	ImageData currentData = (ImageData)currentScope.getPortal();
	synchronized(currentData.lock)
	{
	    currentData.lock.notifyAll();
	}
	try
	{
	    currentData.dummyThread.join();
	} catch (InterruptedException e)
	{
	    System.out.println("Interrupted while waiting for dummyThread to end");
	}
    }
}

