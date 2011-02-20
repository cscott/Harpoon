import javax.realtime.CTMemory;
import java.util.Random;

public class MatrixMultiplyIntegerNestedScopes {
    final Random r;
    final Scope1 s1;
    final CTMemory cs1;

    public static void main(String[] args) {
	new MatrixMultiplyIntegerNestedScopes();
    }

    public MatrixMultiplyIntegerNestedScopes() {
	r = new Random();
	s1 = new Scope1();
	cs1 = new CTMemory(100000000);
	cs1.enter(s1);
    }

    class Scope1 implements Runnable {
	public void run() {
	    Scope2 s2 = new Scope2();
	    CTMemory cs2 = new CTMemory(1000000000);

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
	    
	    cs2.enter(s2);
	}
	
	class Scope2 implements Runnable {
	    Integer dim;
	    Integer[][] A, B;

	    public void run() {
		Scope3 s3 = new Scope3();
		CTMemory cs3 = new CTMemory(1000000000);

		s3.AB = new Integer[dim.intValue()][dim.intValue()];
		s3.padding = new Integer(0);
		
		for (Integer i = new Integer(0); 
		     i.intValue() < dim.intValue();
		     i = new Integer(i.intValue() + 1)) {
		    for (Integer j = new Integer(0); 
			 j.intValue() < dim.intValue();
			 j = new Integer(j.intValue() + 1)) {
			s3.AB[i.intValue()][j.intValue()] = new Integer(0);
			for (Integer k = new Integer(0); 
			     k.intValue() < dim.intValue();
			     k = new Integer(k.intValue() + 1)) {
			    s3.AB[i.intValue()][j.intValue()] = 
				new Integer((A[i.intValue()][k.intValue()]
					     .intValue() * 
					     B[k.intValue()][j.intValue()]
					     .intValue()) +
					    s3.AB[i.intValue()][j.intValue()]
					    .intValue());
			}
			
			if (s3.AB[i.intValue()][j.intValue()]
			    .toString().length() > 
			    s3.padding.intValue()) {
			    s3.padding = 
				new Integer(s3.AB[i.intValue()][j.intValue()]
					    .toString().length());
			}
		    }
		}
		
		cs3.enter(s3);
	    }
	    
	    
	    class Scope3 implements Runnable {
		Integer padding;
		Integer[][] AB;
	    
		public void run() {
		    for (Integer i = new Integer(0); 
			 i.intValue() < dim.intValue();
			 i = new Integer(i.intValue() + 1)) {
			
			String padString = "";
			
			for (Integer j = new Integer(0); 
			     j.intValue() < dim.intValue();
			     j = new Integer(j.intValue() + 1)) {
			    
			    padString = "";
			    
			    for (Integer k = new Integer(0); 
				 (k.intValue() + 
				  A[i.intValue()][j.intValue()]
				  .toString().length()) 
				     < padding.intValue();
				 k = new Integer(k.intValue() + 1)) {
				padString += " ";
			    }
			    
			    System.out.print(A[i.intValue()][j.intValue()]
					     .toString() + " " + padString);
			}
			
			if (i.intValue() == (dim.intValue()/2)) {
			    System.out.print("* " + padString);
			} else {
			    System.out.print("  " + padString);
			}
			
			padString = "";
			
			for (Integer j = new Integer(0); 
			     j.intValue() < dim.intValue();
			     j = new Integer(j.intValue() + 1)) {
			    
			    padString = "";
			    
			    for (Integer k = new Integer(0); 
				 (k.intValue() + 
				  B[i.intValue()][j.intValue()]
				  .toString().length()) 
				     < padding.intValue();
				 k = new Integer(k.intValue() + 1)) {
				padString += " ";
			    }
			    
			    System.out.print(B[i.intValue()][j.intValue()]
					     .toString() + " " + padString);
			}
			
			if (i.intValue() == (dim.intValue()/2)) {
			    System.out.print("= " + padString);
			} else {
			    System.out.print("  " + padString);
			}
			
			padString = "";
			
			for (Integer j = new Integer(0); 
			     j.intValue() < dim.intValue();
			     j = new Integer(j.intValue() + 1)) {
			    
			    padString = "";
			    
			    for (Integer k = new Integer(0); 
				 (k.intValue() + 
				  AB[i.intValue()][j.intValue()]
				  .toString().length()) 
				     < padding.intValue();
				 k = new Integer(k.intValue() + 1)) {
				padString += " ";
			    }
			    
			    System.out.print(AB[i.intValue()][j.intValue()]
					     .toString() + " " + padString);
			}
			
			System.out.println();
		    }
		}
	    }
	}
    }
}
