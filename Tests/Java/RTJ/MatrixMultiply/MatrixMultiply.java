// n by n matrix multiplication, each column is done in a different thread;

import javax.realtime.RealtimeThread;

import java.util.Random;

public class MatrixMultiply {
    public static void main(String argv[]) {
	Random r = new Random();

	final Integer dim = new Integer(5);

	Integer[][] A = new Integer[dim.intValue()][dim.intValue()];
	Integer[][] B = new Integer[dim.intValue()][dim.intValue()];
	Integer[][] AB = new Integer[dim.intValue()][dim.intValue()];

	for (Integer i = new Integer(0); i.intValue() < dim.intValue(); 
	     i = new Integer(i.intValue() + 1)) {
	    for (Integer j = new Integer(0); j.intValue() < dim.intValue(); 
		 j = new Integer(j.intValue() + 1)) {
		A[i.intValue()][j.intValue()] = new Integer(r.nextInt());
		B[i.intValue()][j.intValue()] = new Integer(r.nextInt());
	    }
	}

	final Integer[][] Awrap = A;
	final Integer[][] Bwrap = B;

	class MultiplyColumn implements Runnable {
	    private Integer column;
	    public Integer[][] AB;
	    
	    public MultiplyColumn(Integer column) {
		this.column = column;
	    }

	    public void run() {
		class SumIntegers implements Runnable {
		    LinkedList input;
		    Integer total;

		    public SumIntegers(LinkedList input) {
			this.input = input;
			total = new Integer(0);
		    }

		    public void run() {
			for (Iterator it = input.iterator(); it.hasNext(); ) {
			    total = new Integer(total.intValue() + it.next());
			}
		    }
		    
		}

		class 

		for (Integer i = new Integer(0); i.intValue() < dim.intValue();
		     i = new Integer(i.intValue() + 1)) {
		    LinkedList list = new LinkedList();
		    for (Integer j = new Integer(0); 
			 j.intValue() < dim.intValue();
			 j = new Integer(j.intValue() + 1)) {
			Integer a = A[i.intValue()][column.intValue()];
			Integer b = B[column.intValue()][i.intValue()];
			Integer ab = new Integer(a.intValue() * b.intValue());
			list.add(new Integer(total + ab));
		    }
		    SumIntegers si = new SumIntegers(list);
		    (new LTMemory(100000000)).enter(si);
		    AB[i.intValue()][j.intValue()] = si.total;
		}
		
	    }
	    
	}

	RealtimeThread[] tasks = new RealtimeThread[];
	for (Integer column = new Integer(0); column.intValue() < 5;
	     column = new Integer(column.intValue() + 1)) {

	    MultiplyColumn task = new MultiplyColumn(column);
	    tasks[column.intValue()] = 
		new RealtimeThread(new CTMemory(10000000), task);
	}

	for (Integer column = new Integer(0); column.intValue() < 5;
	     column = new Integer(column.intValue() + 1)) {
	    tasks[column.intValue()].join();
	}
    }


}
