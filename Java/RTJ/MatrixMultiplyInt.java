import java.util.Random;

public class MatrixMultiplyInt {
    public static void main(String[] args) {
	Random r = new Random();

	int dim = 8;
	int[][] A = new int[dim][dim];
	int[][] B = new int[dim][dim];
	int[][] AB = new int[dim][dim];

	for (int i = 0; i < dim; i++) {
	    for (int j = 0; j < dim; j++) {
		A[i][j] = r.nextInt(9);
		B[i][j] = r.nextInt(9);
		AB[i][j] = 0;
	    }
	}
	
	int padding = 0;

	for (int i = 0; i < dim; i++) {
	    for (int j = 0; j < dim; j++) {
		for (int k = 0; k < dim; k++) {
		    AB[i][j] += A[i][k] * B[k][j];
		    
		}

		if (Integer.toString(AB[i][j]).length() > padding) {
		    padding = Integer.toString(AB[i][j]).length();
		}
	    }
	}

	for (int i = 0; i < dim; i++) {
	    String padString = "";

	    for (int j = 0; j < dim; j++) {
		padString = "";
		
		for (int k = 0; 
		     (k + Integer.toString(A[i][j]).length()) < padding;
		     k++) {
		    padString += " ";
		}

		System.out.print(A[i][j] + " " + padString);
	    }
	    
	    if (i == dim/2) {
		System.out.print("* " + padString);
	    } else {
		System.out.print("  " + padString);
	    }

	    for (int j = 0; j < dim; j++) {
		padString = "";

		for (int k = 0; 
		     (k + Integer.toString(B[i][j]).length()) < padding;
		     k++) {
		    padString += " ";
		}

		System.out.print(B[i][j] + " " + padString);
	    }

	    if (i == dim/2) {
		System.out.print("= " + padString);
	    } else {
		System.out.print("  " + padString);
	    }
	    
	    for (int j = 0; j < dim; j++) {
		padString = "";
		
		for (int k = 0;
		     (k + Integer.toString(AB[i][j]).length()) < padding;
		     k++) {
		    padString += " ";
		}

		System.out.print(AB[i][j] + " " + padString);
	    }

	    System.out.println();
	}
    }
}
