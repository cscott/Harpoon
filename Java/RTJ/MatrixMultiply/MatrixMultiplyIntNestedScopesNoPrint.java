import javax.realtime.CTMemory;
import java.util.Random;

public class MatrixMultiplyIntNestedScopesNoPrint {
    final Random r;
    final Scope1 s1;
    final CTMemory cs1;

    public static void main(String[] args) {
	new MatrixMultiplyIntNestedScopesNoPrint();
    }

    public MatrixMultiplyIntNestedScopesNoPrint() {
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
		int[][] AB = new int[dim][dim];
		
		for (int i = 0; i < dim; i++) {
		    for (int j = 0; j < dim; j++) {
			AB[i][j] = 0;
			for (int k = 0; k < dim; k++) {
			    AB[i][j] += A[i][k] * B[k][j];
			}
		    }
		}
		
	    }
	}
    }
}
