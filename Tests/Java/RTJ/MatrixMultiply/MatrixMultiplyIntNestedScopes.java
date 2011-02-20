import javax.realtime.CTMemory;
import java.util.Random;

public class MatrixMultiplyIntNestedScopes {
    final Random r;
    final Scope1 s1;
    final CTMemory cs1;

    public static void main(String[] args) {
	new MatrixMultiplyIntNestedScopes();
    }

    public MatrixMultiplyIntNestedScopes() {
	r = new Random();
	s1 = new Scope1();
	cs1 = new CTMemory(100000000);
	cs1.enter(s1);
    }

    class Scope1 implements Runnable {
	public void run() {
	    Scope2 s2 = new Scope2();
	    CTMemory cs2 = new CTMemory(1000000000);

	    s2.dim = 200;
	    s2.A = new int[s2.dim][s2.dim];
	    s2.B = new int[s2.dim][s2.dim];
	    
	    for (int i = 0; i < s2.dim; i++) {
		for (int j = 0; j < s2.dim; j++) {
		    s2.A[i][j] = r.nextInt(4);
		    s2.B[i][j] = r.nextInt(4);
		}
	    }
	    
	    cs2.enter(s2);
	}
	
	class Scope2 implements Runnable {
	    int dim;
	    int[][] A, B;

	    public void run() {
		Scope3 s3 = new Scope3();
		CTMemory cs3 = new CTMemory(1000000000);

		s3.AB = new int[dim][dim];
		s3.padding = 0;
		
		for (int i = 0; i < dim; i++) {
		    for (int j = 0; j < dim; j++) {
			s3.AB[i][j] = 0;
			for (int k = 0; k < dim; k++) {
			    s3.AB[i][j] += A[i][k] * B[k][j];
			}
			
			if (Integer.toString(s3.AB[i][j]).length() > 
			    s3.padding) {
			    s3.padding = Integer.toString(s3.AB[i][j]).length();
			}
		    }
		}
		
		cs3.enter(s3);
	    }
	    
	    
	    class Scope3 implements Runnable {
		int padding;
		int[][] AB;
	    
		public void run() {
		    for (int i = 0; i < dim; i++) {
			String padString = "";
			
			for (int j = 0; j < dim; j++) { 
			    padString = "";
			    
			    for (int k = 0; 
				 (k + Integer.toString(A[i][j]).length()) 
				     < padding; k++) {
				padString += " ";
			    }
			    
			    System.out.print(A[i][j] + " " + padString);
			}
			
			if (i == (dim/2)) {
			    System.out.print("* " + padString);
			} else {
			    System.out.print("  " + padString);
			}
			
			padString = "";
			
			for (int j = 0; j < dim; j++) {
			    padString = "";
			    
			    for (int k = 0; 
				 (k + Integer.toString(B[i][j]).length())
				     < padding; k++) {
				padString += " ";
			    }
			    
			    System.out.print(B[i][j] + " " + padString);
			}
			
			if (i == (dim/2)) {
			    System.out.print("= " + padString);
			} else {
			    System.out.print("  " + padString);
			}
			
			padString = "";
			
			for (int j = 0;	j < dim; j++) {
			    padString = "";
			    
			    for (int k = 0; 
				 (k + Integer.toString(AB[i][j]).length()) 
				     < padding; k++) {
				padString += " ";
			    }
			    
			    System.out.print(AB[i][j] + " " + padString);
			}
			
			System.out.println();
		    }
		}
	    }
	}
    }
}
