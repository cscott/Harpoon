import javax.realtime.RealtimeThread;
import javax.realtime.PriorityParameters;

class ImageData
{
    public int[] rvals, gvals, bvals;
    public int[] outvals, outvals0, outvals1, outvals2;
    public int[] pixel;
    public int width, height;

    public Object lock = new Object();
    public RealtimeThread dummyThread = new RealtimeThread(new PriorityParameters(0))
        {
	    public void run()
	    {
		getCurrentMemoryArea().enter();
		synchronized(lock)
		{
		    try
		    {
			lock.wait();
		    } catch (InterruptedException e)
		    {
			e.printStackTrace();
		    }
		}
	    }
	};
}
