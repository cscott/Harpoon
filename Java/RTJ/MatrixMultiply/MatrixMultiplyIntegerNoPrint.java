import java.util.Random;

public class MatrixMultiplyIntegerNoPrint {
    public static void main(String[] args) {
	Random r = new Random();

	Integer dim = new Integer(200);
	Integer[][] A = new Integer[dim.intValue()][dim.intValue()];
	Integer[][] B = new Integer[dim.intValue()][dim.intValue()];
	Integer[][] AB = new Integer[dim.intValue()][dim.intValue()];

	for (Integer i = new Integer(0); i.intValue() < dim.intValue();
	     i = new Integer(i.intValue() + 1)) {
	    for (Integer j = new Integer(0); j.intValue() < dim.intValue();
		 j = new Integer(j.intValue() + 1)) {
		A[i.intValue()][j.intValue()] = new Integer(r.nextInt(4));
		B[i.intValue()][j.intValue()] = new Integer(r.nextInt(4));
		AB[i.intValue()][j.intValue()] = new Integer(0);
	    }
	}

	for (Integer i = new Integer(0); i.intValue() < dim.intValue();
	     i = new Integer(i.intValue() + 1)) {
	    for (Integer j = new Integer(0); j.intValue() < dim.intValue();
		 j = new Integer(j.intValue() + 1)) {
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
