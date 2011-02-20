import javax.realtime.AsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;
import javax.realtime.ReleaseParameters;
import javax.realtime.SchedulingParameters;
import javax.realtime.Scheduler;

class Test3
{
    private static int width=480, height=360; 
    static SchedulingParameters sp1 = new PriorityParameters(11);
    static ReleaseParameters rp0 = new PeriodicParameters(null, new RelativeTime(1000, 0), null, null, null, null);
    static ReleaseParameters rp1 = new PeriodicParameters(new RelativeTime(100,0), new RelativeTime(1000, 0), null, null, null, null);
    static ReleaseParameters rp2 = new PeriodicParameters(new RelativeTime(2000,0), new RelativeTime(1000, 0), null, null, null, null);
    static ReleaseParameters rp3 = new PeriodicParameters(new RelativeTime(3000,0), new RelativeTime(1000, 0), null, null, null, null);
    static ReleaseParameters rp4 = new PeriodicParameters(new RelativeTime(4000,0), new RelativeTime(1000, 0), null, null, null, null);
    static ImageGrabThread myThread0 = new ImageGrabThread(sp1, rp0, 0);
    static EdgeDetectThread myThread1 = new EdgeDetectThread(sp1, rp1, 1);
    static EdgeRefineThread myThread2 = new EdgeRefineThread(sp1, rp2, 2, 240, 70);
    static PruningThread myThread3 = new PruningThread(sp1, rp3, 3);
    static ImageTestThread myThread4 = new ImageTestThread(sp1, rp4, 4);
    static
    {
	rp0.setDeadlineMissHandler(myThread0.missHandler);
	rp1.setDeadlineMissHandler(myThread1.missHandler);
	rp2.setDeadlineMissHandler(myThread2.missHandler);
	rp3.setDeadlineMissHandler(myThread3.missHandler);
	rp4.setDeadlineMissHandler(myThread4.missHandler);

	myThread0.SetNextThread(myThread1);
	myThread1.SetNextThread(myThread2);
	myThread2.SetNextThread(myThread3);
	myThread3.SetNextThread(myThread4);
    }

    public static void main(String[] args)
    {
	System.out.println("Start...");
	myThread0.start();
	myThread1.start();
	myThread2.start();
	myThread3.start();
	myThread4.start();
	try
	{
	    myThread0.join();
	    myThread1.join();
	    myThread2.join();
	    myThread3.join();
	    myThread4.join();
	} catch (InterruptedException e)
	{ }
	System.out.println("Done!");
    }
}
