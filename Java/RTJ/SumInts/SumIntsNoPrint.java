// n threads, each building a linked list of integers and summing them.

import javax.realtime.RealtimeThread;
import javax.realtime.CTMemory;

import java.util.Iterator;
import java.util.LinkedList;

public class SumIntsNoPrint {
    public static void main(String argv[]) {
	int taskNum = 100;
	final int intNum = 10000;

	Runnable task = new Runnable() {
		public void run() {
		    LinkedList l = new LinkedList();
		    for (int i = 0; i < intNum; i++) {
			l.add(new Integer(i));
		    }
		    int total = 0;
		    for (Iterator it = l.iterator();
			 it.hasNext();) {
			total += ((Integer)it.next()).intValue();
		    } 
		}
	    };

	Thread[] tasks = new Thread[taskNum];
	for (int i = 0; i < taskNum; i++) {
	    tasks[i] = new RealtimeThread(new CTMemory(1000000000), task);
	    tasks[i].start();
	}
	for (int i = 0; i < taskNum; i++) {
	    try {
		tasks[i].join();
	    } catch (InterruptedException e) {
		System.out.println(e.toString());
		Thread.currentThread().dumpStack();
		System.exit(0);
	    }
	}
    }
}
