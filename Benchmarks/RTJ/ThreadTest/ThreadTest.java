import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.VTMemory;

public class ThreadTest {
    public static void main(String[] args) {
	Thread rt1 = new Thread() {
	    public void run() {
		final long start = System.currentTimeMillis();
		VTMemory vt = new VTMemory();
		Runnable r = new Runnable() {
		    public void run() {
			NoHeapRealtimeThread.print("1: "+(System.currentTimeMillis()-start)+"\n");
		    }
		};
		while (true) vt.enter(r);
	    }
	};
	Thread rt2 = new Thread() {
	    public void run() {
		final long start = System.currentTimeMillis();
		VTMemory vt = new VTMemory();
		Runnable r = new Runnable() {
		    public void run() {
			NoHeapRealtimeThread.print("2: "+(System.currentTimeMillis()-start)+"\n");
		    }
		};
		while (true) vt.enter(r);
	    }
	};
	Thread rt3 = new Thread() {
	    public void run() {
		final long start = System.currentTimeMillis();
		VTMemory vt = new VTMemory();
		Runnable r = new Runnable() {
		    public void run() {
			NoHeapRealtimeThread.print("3: "+(System.currentTimeMillis()-start)+"\n");
		    }
		};
		while (true) vt.enter(r);
	    }
	};
	rt1.start();
	rt2.start();
	rt3.start();
	try {
	    rt1.join();
	    rt2.join();
	    rt3.join();
	} catch (InterruptedException e) {
	    System.out.println(e.toString());
	    System.exit(-1);
	}
    }
}
