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

class EdgeDetectThread extends PeriodicThread
{
    protected int dx0, dx1, dx2;
    protected int numpix;

    public EdgeDetectThread(SchedulingParameters sp, ReleaseParameters rp, int id)
    {
	super(sp, rp);
	this.id = id;
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

	numpix = currentData.width*currentData.height-currentData.width-1;
	currentData.outvals = new int[currentData.width*currentData.height];

	for(int p=0;p<numpix;p++)
	{
	    dx0 = Math.abs(currentData.rvals[p] - currentData.rvals[p+currentData.width+1])+Math.abs(currentData.rvals[p+1]-currentData.rvals[p+currentData.width]);
	    dx1 = Math.abs(currentData.gvals[p] - currentData.gvals[p+currentData.width+1])+Math.abs(currentData.gvals[p+1]-currentData.gvals[p+currentData.width]);
	    dx2 = Math.abs(currentData.bvals[p] - currentData.bvals[p+currentData.width+1])+Math.abs(currentData.bvals[p+1]-currentData.bvals[p+currentData.width]);
	    dx0 = Math.max(Math.max(dx0, dx1), dx2);
	    currentData.outvals[p] = Math.min(dx0*2,255);
	    yield();
	}

	System.out.println(id + ": Finished - " + getTime());
	throw new InterruptedException();
    }
}









