import javax.realtime.AsyncEventHandler;
import javax.realtime.PriorityParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;
import javax.realtime.ReleaseParameters;
import javax.realtime.SchedulingParameters;
import javax.realtime.Scheduler;
import javax.realtime.PriorityScheduler;
import javax.realtime.Interruptible;
import javax.realtime.Clock;
import javax.realtime.AbsoluteTime;

class Test2
{
    static SchedulingParameters sp1 = new PriorityParameters(0);
    static SchedulingParameters sp2 = new PriorityParameters(11);
    static TestTimedThread0 myThread0 = new TestTimedThread0(sp1,new RelativeTime(800,0),0);
    static TestTimedThread1 myThread1 = new TestTimedThread1(sp1,new RelativeTime(800,0),1);
    static TestTimedThread2 myThread2 = new TestTimedThread2(sp2,new RelativeTime(800,0),2);
    static
    {
	myThread0.SetNextThread(myThread1);
	myThread1.SetNextThread(myThread2);
    }

    public static void main(String[] args)
    {
	System.out.println("Start...");
	myThread0.start = myThread1.start = myThread2.start = Clock.getRealtimeClock().getTime();
	myThread0.start();
	myThread1.start();
	myThread2.start();
	try
	{
	    myThread0.join();
	    myThread1.join();
	    myThread2.join();
	} catch (InterruptedException e)
	{
	    System.out.println("Interrupted while join()ing.");
	}
	System.out.println("Done!");
    }
}
