import java.util.Random;

public class MatrixMultiplyIntNoPrint {
    public static void main(String[] args) {
	Random r = new Random();

	int dim = 200;
	int[][] A = new int[dim][dim];
	int[][] B = new int[dim][dim];
	int[][] AB = new int[dim][dim];

	for (int i = 0; i < dim; i++) {
	    for (int j = 0; j < dim; j++) {
		A[i][j] = r.nextInt(4);
		B[i][j] = r.nextInt(4);
		AB[i][j] = 0;
	    }
	}
	
	for (int i = 0; i < dim; i++) {
	    for (int j = 0; j < dim; j++) {
		for (int k = 0; k < dim; k++) {
		    AB[i][j] += A[i][k] * B[k][j];
		}
	    }
	}
    }
}
