// Accumulator.java, created Mon Jan 17 14:37:35 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Test.PA.Test4;

import java.util.Vector;
import java.util.Enumeration;

/**
 * <code>Sum</code> is a test for the PA algorithm taken
 from the second paper on compositional PA written by John Whaley
 and Martin Rinard (Section 2 Example).

 <code>Sum.sum</code> is the interesting method.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: Sum.java,v 1.2 2002-02-25 21:07:43 cananian Exp $
 */
class Accumulator {
    int value = 0;
    synchronized void add(int v){
	value += v;
    }    
}

public class Sum{
    public static void sum(int n, Accumulator a){
	Vector v = new Vector();
	for(int i = 0; i < n ; i += 2)
	    v.add(new Integer(i));
	Worker t = new Worker();
	t.init(v,a);
	t.start();
	int s = 0;
	for(int i = 1; i < n ;i += 2){
	    s = s + i;
	}
	a.add(s);
    }

    // We need some top level procedure to instanciate these objects
    // otherwise the call graph simply ignores their methods.
    // We hope no dead code elimination is done before the PA ...
    public static void main(String[] params){
	Accumulator accum = new Accumulator();
	Sum.sum(10,accum);
    }
}


class Worker extends Thread{
    Vector work;
    Accumulator dest;
    void init(Vector v,Accumulator a){
	work = v;
	dest = a;
    }
    public void run(){
	Enumeration e = work.elements();
	int s = 0;
	while(e.hasMoreElements()){
	    Integer i = (Integer) e.nextElement();
	    s = s + i.intValue();
	}
	dest.add(s);
    }
}
