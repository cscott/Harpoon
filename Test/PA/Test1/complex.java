// complex.java, created Sun Jan 23 16:37:59 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Test.PA.Test1;

/**
 * <code>complex</code> is a test for the PA algorithm taken
 from the first example of the first paper on compositional PA
 written by John Whaley and Martin Rinard (Section 2.1 Return Values).

 <code>complex.multiplyAdd</code> is the interesting method.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: complex.java,v 1.2 2002-02-25 21:07:26 cananian Exp $
 */
public class complex {
    
    double x,y;

    complex(double a, double b){ x = a; y = b; }

    complex multiply(complex a){
	complex product = 
	    new complex(x*a.x - y*a.y, x*a.y + y*a.x);
	return product;
    }
    
    complex add(complex a){
	complex sum =
	    new complex(x+a.x, y+a.y);
	return sum;
    }

    complex multiplyAdd(complex a, complex b){
	complex product = a.multiply(b);
	complex sum = this.add(product);
	return sum;
    }

    // We need some top level procedure to create at least one complex
    // object, otherwise the call graph simply ignores its methods.
    // We hope no dead code elimination is done before the PA ...
    public static void main(String[] params){
	complex a = new complex(1.0,2.0);
	complex b = new complex(1.5,2.5);
	complex c = a.add(b);
	complex d = a.multiplyAdd(b,b);
    }
}
