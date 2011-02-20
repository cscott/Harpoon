import java.util.Random;
import javax.realtime.Stats;

public class MatrixMultiplyIntegerStats {
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

	Integer padding = new Integer(0);

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
		
		if (AB[i.intValue()][j.intValue()].toString().length() > 
		    padding.intValue()) {
		    padding = 
			new Integer(AB[i.intValue()][j.intValue()]
				    .toString().length());
		}
	    }
	}
	
	for (Integer i = new Integer(0); i.intValue() < dim.intValue();
	     i = new Integer(i.intValue() + 1)) {

	    String padString = "";

	    for (Integer j = new Integer(0); j.intValue() < dim.intValue();
		 j = new Integer(j.intValue() + 1)) {
		
		padString = "";

		for (Integer k = new Integer(0); 
		     (k.intValue() + 
		      A[i.intValue()][j.intValue()].toString().length()) 
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

	    for (Integer j = new Integer(0); j.intValue() < dim.intValue();
		 j = new Integer(j.intValue() + 1)) {

		padString = "";

		for (Integer k = new Integer(0); 
		     (k.intValue() + 
		      B[i.intValue()][j.intValue()].toString().length()) 
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
	    
	    for (Integer j = new Integer(0); j.intValue() < dim.intValue();
		 j = new Integer(j.intValue() + 1)) {

		padString = "";

		for (Integer k = new Integer(0); 
		     (k.intValue() + 
		      AB[i.intValue()][j.intValue()].toString().length()) 
			 < padding.intValue();
		     k = new Integer(k.intValue() + 1)) {
		    padString += " ";
		}
    
		System.out.print(AB[i.intValue()][j.intValue()]
				 .toString() + " " + padString);
	    }

	    System.out.println();
	}

	Stats.print();
    }
}
