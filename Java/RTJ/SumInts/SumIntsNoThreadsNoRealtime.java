// n threads, each building a linked list of integers and summing them.

import javax.realtime.RealtimeThread;
import javax.realtime.CTMemory;

import java.util.Iterator;
import java.util.LinkedList;

public class SumIntsNoThreadsNoRealtime {
    public static void main(String argv[]) {
	int taskNum = 100;
	final int intNum = 10000;

	Runnable task = new Runnable() {
		public void run() {
		    LinkedList l = new LinkedList();
		    for (int i = 0; i<intNum; i++) {
			l.add(new Integer(i));
		    }
		    int total = 0;
		    for (Iterator it = l.iterator(); it.hasNext();) {
			total += ((Integer)it.next()).intValue();
		    } 
		    // Not thread-safe!
		    System.out.println(total); 
		}
	    };

	for (int i = 0; i < taskNum; i++) {
	    task.run();
	}
    }
}
