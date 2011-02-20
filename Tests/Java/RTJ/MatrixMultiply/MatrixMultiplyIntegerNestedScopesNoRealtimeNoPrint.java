import java.util.Random;

public class MatrixMultiplyIntegerNestedScopesNoRealtimeNoPrint {
    final Random r;
    final Scope1 s1;

    public static void main(String[] args) {
	new MatrixMultiplyIntegerNestedScopesNoRealtimeNoPrint();
    }

    public MatrixMultiplyIntegerNestedScopesNoRealtimeNoPrint() {
	r = new Random();
	s1 = new Scope1();
	s1.run();
    }

    class Scope1 implements Runnable {
	public void run() {
	    Scope2 s2 = new Scope2();

	    s2.dim = new Integer(200);
	    s2.A = new Integer[s2.dim.intValue()][s2.dim.intValue()];
	    s2.B = new Integer[s2.dim.intValue()][s2.dim.intValue()];
	    
	    for (Integer i = new Integer(0); i.intValue() < s2.dim.intValue();
		 i = new Integer(i.intValue() + 1)) {
		for (Integer j = new Integer(0); 
		     j.intValue() < s2.dim.intValue();
		     j = new Integer(j.intValue() + 1)) {
		    s2.A[i.intValue()][j.intValue()] = 
			new Integer(r.nextInt(4));
		    s2.B[i.intValue()][j.intValue()] = 
			new Integer(r.nextInt(4));
		}
	    }
	    
	    s2.run();
	}
	
	class Scope2 implements Runnable {
	    Integer dim;
	    Integer[][] A, B;

	    public void run() {
		Integer[][] AB = new Integer[dim.intValue()][dim.intValue()];
		
		for (Integer i = new Integer(0); 
		     i.intValue() < dim.intValue();
		     i = new Integer(i.intValue() + 1)) {
		    for (Integer j = new Integer(0); 
			 j.intValue() < dim.intValue();
			 j = new Integer(j.intValue() + 1)) {
			AB[i.intValue()][j.intValue()] = new Integer(0);
			for (Integer k = new Integer(0); 
			     k.intValue() < dim.intValue();
			     k = new Integer(k.intValue() + 1)) {
			    AB[i.intValue()][j.intValue()] = 
				new Integer((A[i.intValue()][k.intValue()]
					     .intValue() * 
					     B[k.intValue()][j.intValue()]
					     .intValue()) +
					    AB[i.intValue()][j.intValue()]
					    .intValue());
			}
		    }
		}
	    }
	}
    }
}
