import javax.realtime.AsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;
import javax.realtime.ReleaseParameters;
import javax.realtime.SchedulingParameters;
import javax.realtime.Scheduler;

class Test
{
    static SchedulingParameters sp1 = new PriorityParameters(1);
    static ReleaseParameters rp0 = new PeriodicParameters(null, new RelativeTime(200, 0), null, null, null, null);
    static ReleaseParameters rp1 = new PeriodicParameters(new RelativeTime(250,0), new RelativeTime(200, 0), null, null, null, null);
    static ReleaseParameters rp2 = new PeriodicParameters(new RelativeTime(500,0), new RelativeTime(200, 0), null, null, null, null);
    static TestCountingThread0 myThread0 = new TestCountingThread0(sp1, rp0, 0);
    static TestCountingThread1 myThread1 = new TestCountingThread1(sp1, rp1, 1);
    static TestCountingThread2 myThread2 = new TestCountingThread2(sp1, rp2, 2);
    static
    {
	rp0.setDeadlineMissHandler(myThread0.missHandler);
	rp1.setDeadlineMissHandler(myThread1.missHandler);
	rp2.setDeadlineMissHandler(myThread2.missHandler);

	myThread0.SetNextThread(myThread1);
	myThread1.SetNextThread(myThread2);
    }

    public static void main(String[] args)
    {
	System.out.println(myThread0.getName());
	System.out.println(myThread1.getName());
	System.out.println(myThread2.getName());
	System.out.println("Start...");
	myThread0.start();
	myThread1.start();
	myThread2.start();
	try
	{
	    myThread0.join();
	    myThread1.join();
	    myThread2.join();
	} catch (InterruptedException e)
	{ }
	System.out.println("Done!");
    }
}
