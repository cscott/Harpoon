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

class PruningThread extends PeriodicThread
{
    protected int dx0, dx1, dx2;
    protected int numpix;

    public PruningThread(SchedulingParameters sp, ReleaseParameters rp, int id)
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

	for(int i=0;i<currentData.width*currentData.height;i++) currentData.outvals[i]=currentData.outvals0[i];
	numpix = currentData.width*currentData.height;

	//pruning
	int changed=1;
	int[] near  = new int[8];
	for(int i=0;i<10 && changed==1;i++)
	{
	    changed=0;
	    for(int p=currentData.width+1;p<numpix;p++)
	    {
		if(currentData.outvals0[p]==255)
		{
		    /* 0 1 2
		       3 * 4
		       5 6 7 */
		    near[0] = currentData.outvals0[p-currentData.width-1];
		    near[1] = currentData.outvals0[p-currentData.width];
		    near[2] = currentData.outvals0[p-currentData.width+1];
		    near[3] = currentData.outvals0[p-1];
		    near[4] = currentData.outvals0[p+1];
		    near[5] = currentData.outvals0[p+currentData.width-1];
		    near[6] = currentData.outvals0[p+currentData.width];
		    near[7] = currentData.outvals0[p+currentData.width+1];
		    
		    if( ((near[0] | near[1] | near[2] | near[3] | near[4]) < 2 && (near[5] & near[7]) < 2) ||
			((near[1] | near[2] | near[4] | near[6] | near[7]) < 2 && (near[0] & near[5]) < 2) ||
			((near[3] | near[4] | near[5] | near[6] | near[7]) < 2 && (near[0] & near[2]) < 2) ||
			((near[0] | near[1] | near[3] | near[5] | near[6]) < 2 && (near[2] & near[7]) < 2) )
		    {
			changed=1;
			currentData.outvals[p] = 0;
		    }
		}
	    }
	    int a=currentData.width*currentData.height;
	    for(int p=0;p<a;p++)
		 currentData.outvals0[p] = currentData.outvals[p];
	    yield();
	    if(interrupted())
	    {
		//throw new InterruptedException();
	    }
	}

	System.out.println(id + ": Finished - " + getTime());
	throw new InterruptedException();
    }
}









