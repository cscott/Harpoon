package javax.realtime;
import javax.realtime.RealtimeThread;
import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.ImmortalMemory;
import javax.realtime.CTMemory;
import javax.realtime.MemoryArea;

public class ThreadTest {
// 	static PriorityScheduler ps = new PriorityScheduler();

	public static RealtimeThread thread2 = new RealtimeThread("Thread 2") {
			public void run() {
				System.out.println("Starting Thread 2!");
				while(true) {
					System.out.print("2\n");
		    }
	    }
		};

	public static RealtimeThread thread3 = new RealtimeThread("Thread 3") {
	    public void run() {
				System.out.println("Starting Thread 3!");
				while(true) {
					System.out.print("3\n");
		    }
	    }
		};

	public static void main(String[] argc) {
		System.out.println("Starting thread 2");
		thread2.start();
		System.out.println("Starting thread 3");
		thread3.start();
		System.out.println("Started");

		for(int count = 10; count > 0; count--) {
	    System.out.print("M\n");
		}
	}
}
