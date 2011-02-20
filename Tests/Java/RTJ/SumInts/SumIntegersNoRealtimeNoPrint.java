// n threads, each building a linked list of integers and summing them.

import java.util.Iterator;
import java.util.LinkedList;

public class SumIntegersNoRealtimeNoPrint {
    public static void main(String argv[]) {
	Integer taskNum = new Integer(100);
	final Integer intNum = new Integer(10000);

	Runnable task = new Runnable() {
		public void run() {
		    LinkedList l = new LinkedList();
		    for (Integer i = new Integer(0); 
			 i.intValue() < intNum.intValue(); 
			 i = new Integer(i.intValue() + 1)) {
			l.add(i);
		    }
		    Integer total = new Integer(0);
		    for (Iterator it = l.iterator();
			 it.hasNext();) {
			total = new Integer(((Integer)it.next()).intValue()
					    + total.intValue());
		    } 
		}
	    };

	Thread[] tasks = new Thread[taskNum.intValue()];
	for (Integer i = new Integer(0); i.intValue() < taskNum.intValue(); 
	     i = new Integer(i.intValue() + 1)) {
	    tasks[i.intValue()] = new Thread(task);
	    tasks[i.intValue()].start();
	}
	for (Integer i = new Integer(0); i.intValue() < taskNum.intValue();
	     i = new Integer(i.intValue() + 1)) {
	    try {
		tasks[i.intValue()].join();
	    } catch (InterruptedException e) {
		System.out.println(e.toString());
		Thread.currentThread().dumpStack();
		System.exit(0);
	    }
	}
    }
}
