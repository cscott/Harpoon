// n threads, each building a linked list of integers and summing them.

import javax.realtime.RealtimeThread;
import javax.realtime.CTMemory;

import java.util.Iterator;
import java.util.LinkedList;

public class SumIntegersNoThreads {
    public static void main(String argv[]) {
	Integer taskNum = new Integer(1);
	final Integer intNum = new Integer(1000);

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
		    // Not thread-safe!
		    System.out.println(total.toString()); 
		}
	    };

	for (Integer i = new Integer(0); i.intValue() < taskNum.intValue(); 
	     i = new Integer(i.intValue() + 1)) {
	    (new CTMemory(1000000000)).enter(task);
	}
    }
}
