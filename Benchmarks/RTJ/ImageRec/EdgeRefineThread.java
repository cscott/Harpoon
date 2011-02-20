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

class EdgeRefineThread extends PeriodicThread
{
    protected int dx0, dx1, dx2;
    protected int numpix, T1, T2;

    public EdgeRefineThread(SchedulingParameters sp, ReleaseParameters rp, int id, int thresh1, int thresh2)
    {
	super(sp, rp);
	this.id = id;
	T1 = thresh1;
	T2 = thresh2;
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

	currentData.outvals0 = new int[currentData.width*currentData.height];

	numpix = currentData.width*currentData.height;
	for(int p=0;p<numpix;p++)
	{
	    if(currentData.outvals[p] >= T1) currentData.outvals0[p] = 255;
	    else if(currentData.outvals[p] >= T2) currentData.outvals0[p] = 1;
	    else currentData.outvals0[p] = 0;
	}
	yield();
	if(interrupted())
        {
	    //throw new InterruptedException();
	}

	//hysteresis
	numpix = currentData.width*(currentData.height-2)-1;
	for(int p=currentData.width+1;p<numpix;p++)
	{
	    if(currentData.outvals0[p]==255) 
	    {
		recursiveHysteresis(p, currentData);
	    }
	}
	yield();
	if(interrupted())
        {
	    //throw new InterruptedException();
	}

	//thinning
	int changed=1;
	int[] near = new int[8];
	for(int i=0;i<6 && changed==1;i++)
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
		    
		    if( ((near[0] | near[1] | near[2])<2 && (near[5] & near[6] & near[7])==255) ||
			((near[2] | near[4] | near[7])<2 && (near[0] & near[3] & near[5])==255) ||
			((near[5] | near[6] | near[7])<2 && (near[0] & near[1] & near[2])==255) ||
			((near[0] | near[3] | near[5])<2 && (near[2] & near[4] & near[7])==255) ||
			
			((near[1] | near[2] | near[4])<2 && (near[3] & near[6])==255) ||
			((near[4] | near[6] | near[7])<2 && (near[1] & near[3])==255) ||
			((near[3] | near[5] | near[6])<2 && (near[1] & near[4])==255) ||
			((near[0] | near[1] | near[3])<2 && (near[4] & near[6])==255) )
		    {
			changed=1;
			currentData.outvals0[p] = 0;
		    }
		}
	    }
	    yield();
	    if(interrupted())
            {
		//throw new InterruptedException();
	    }

	}

	System.out.println(id + ": Finished - " + getTime());
    }

    protected void recursiveHysteresis(int index, ImageData currentData)
    {
	if(index<currentData.width || index>currentData.width*(currentData.height-1) || (index%currentData.width)==0 || ((index+1)%currentData.width)==0) return;
	currentData.outvals0[index] = 255;
	if(currentData.outvals0[index-currentData.width-1]==1) recursiveHysteresis(index-currentData.width-1, currentData);
	if(currentData.outvals0[index-currentData.width]==1) recursiveHysteresis(index-currentData.width, currentData);
	if(currentData.outvals0[index-currentData.width+1]==1) recursiveHysteresis(index-currentData.width+1, currentData);
	if(currentData.outvals0[index-1]==1) recursiveHysteresis(index-1, currentData);
	if(currentData.outvals0[index+1]==1) recursiveHysteresis(index+1, currentData);
	if(currentData.outvals0[index+currentData.width-1]==1) recursiveHysteresis(index+currentData.width-1, currentData);
	if(currentData.outvals0[index+currentData.width]==1) recursiveHysteresis(index+currentData.width, currentData);
	if(currentData.outvals0[index+currentData.width+1]==1) recursiveHysteresis(index+currentData.width+1, currentData);
    }
}









