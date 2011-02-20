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
import javax.realtime.SizeEstimator;

class ImageGrabThread extends PeriodicThread
{
    protected native void readJPEG(String filename, int[] arr);

    static
    {
    	System.loadLibrary("readJPEG");
    }

    protected final int MAXWIDTH=480, MAXHEIGHT=360;
    protected SizeEstimator estimator;

    public ImageGrabThread(SchedulingParameters sp, ReleaseParameters rp, int id)
    {
	super(sp, rp);
	this.id = id;

	estimator = new SizeEstimator();
	estimator.reserve(int.class, MAXWIDTH*MAXHEIGHT*10+5); //+5 = width, height, pixel[3]
	                                                       //*10 = 7 arrays + temp. 3 sample array from native
	estimator.reserve(Object.class, 17); //2*8+1 - array headers -- RTJ spec problem
	estimator.reserve(RealtimeThread.class, 1);
    }

    public void ProcessData() throws InterruptedException
    {
	System.out.println(id + ": Start - " + getTime());
	int[] imgbuffer;

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

	currentData.dummyThread.start();

	currentScope.setPortal(currentData);
	System.out.println(id + ": set portal: " + currentScope.getPortal());

	imgbuffer = new int[MAXWIDTH*MAXHEIGHT*3];
	readJPEG("plane3.jpg", imgbuffer);
	yield();

	currentData.width = 480; //CHEATING
	currentData.height = 360; //CHEATING

	int size = currentData.width*currentData.height;
	currentData.rvals = new int[size];
	currentData.gvals = new int[size];
	currentData.bvals = new int[size];
	for(int i=0;i<size;i++)
	{
	    currentData.rvals[i] = imgbuffer[3*i];
	    currentData.gvals[i] = imgbuffer[3*i+1];
	    currentData.bvals[i] = imgbuffer[3*i+2];
	}

	System.out.println(id + ": Finished - " + getTime());
	yield();
	throw new InterruptedException();
    }

    protected void Initialize()
    {
	currentScope = new LTMemory(estimator, estimator);
	System.out.println(id + ": New scope: " + currentScope);
    }
}









