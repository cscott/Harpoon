import javax.realtime.AsyncEventHandler;
import javax.realtime.ImportanceParameters;
import javax.realtime.PeriodicParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;
import javax.realtime.ReleaseParameters;
import javax.realtime.SchedulingParameters;

public class ThreadBench {
    // AsyncEventHandler (schedulingParams, releaseParams, memoryParams,
    //                    memoryArea, processingGroupParams, logic)
    static int totalMisses = 0;
    static int totalOverruns = 0;

    static AsyncEventHandler missHandler = 
	new AsyncEventHandler() {
	    public void handleAsyncEvent() {
		int fireCount = getAndClearPendingFireCount();
		System.out.println("Missed " + fireCount + " deadline(s).");
		totalMisses += fireCount;
	    }
	};

    static AsyncEventHandler overrunHandler = 
	new AsyncEventHandler() {
	    public void handleAsyncEvent() {
		int fireCount = getAndClearPendingFireCount();
		System.out.println("Overrun " + fireCount + " deadline(s).");
		totalOverruns += fireCount;
	    }
	};

    // ImportanceParameters: (priority, importance)
    static SchedulingParameters sp1 = new ImportanceParameters(0, 9);
    static SchedulingParameters sp2 = new ImportanceParameters(0, 9);

    // RelativeTime: (ms, ns) past clock time
    static RelativeTime rt1 = new RelativeTime(100, 0); // 100 ms
    static RelativeTime rt2 = new RelativeTime(40, 0); 
    static RelativeTime rt3 = new RelativeTime(40, 0); 
    static RelativeTime rt4 = new RelativeTime(20, 0);

    // ReleaseParameters (cost, deadline, overrunHandler, missHandler)
    // PeriodicParameters (start, period, cost, deadline, 
    //                     overrunHandler, missHandler)
    static ReleaseParameters rp1 = 
	new PeriodicParameters(null, rt1, rt2, null, 
			       overrunHandler, missHandler); // (100, 40)
    static ReleaseParameters rp2 = 
	new PeriodicParameters(null, rt3, rt4, null, 
			       overrunHandler, missHandler); // (40, 20)

    static RealtimeThread thread1 =
	new RealtimeThread(sp1, rp1) {
	    public void run() {
		long startTime = System.currentTimeMillis();
		for (int i = 1; i<=1000; i++) {
		    System.out.println("T1: " + i + ") " + 
				       (System.currentTimeMillis()-startTime) +
				       " ms");
		}
	    }
	};
    
    static RealtimeThread thread2 =
	new RealtimeThread(sp2, rp2) {
	    public void run() {
		long startTime = System.currentTimeMillis();
		for (int i = 1; i<=1000; i++) {
		System.out.println("T2: " + i + ") " +
				   (System.currentTimeMillis()-startTime) +
				   " ms");
		}
	    }
	};
    
    public static void main(String args[]) {
	thread1.start();
	thread2.start();
	try {
	    thread1.join();
	    thread2.join();
	} catch (InterruptedException e) {
	    System.out.println("Oops... An InterruptedException occured...");
	    System.out.println("Stack: ");
	    e.printStackTrace();
	}
	System.out.println("Total misses: " + totalMisses);
	System.out.println("Total overruns: " + totalOverruns);
    }

}
