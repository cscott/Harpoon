// Debug.java, created Thu Feb 10 19:06:16 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.Arrays;
import java.util.Iterator;

import harpoon.Util.UComp;

import harpoon.ClassFile.HMethod;
import harpoon.Analysis.MetaMethods.MetaMethod;

/**
 * <code>Debug</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: Debug.java,v 1.1.2.6 2000-03-23 21:29:02 salcianu Exp $
 */
public abstract class Debug {

    /** Returns a sorted array containing all the objects from
	<code>set</code>. Increasing lexicographic order of the
	string representation of the objects is used. */
    public static Object[] sortedSet(Set set){
	Object[] obj = set.toArray(new Object[set.size()]);
	Arrays.sort(obj,UComp.uc);
	return obj;
    }

    /** Provides a string representation of a set; the elements of the
	set appear in increasing lexicographic order.
	<code>set1.equals(set2) <==> stringImg(set1).equals(stringImg(set2)).</code>*/
    public static String stringImg(Set set){
	StringBuffer buffer = new StringBuffer();

	Object obj[] = sortedSet(set);

	buffer.append("[ ");
	for(int i=0; i<obj.length;i++){
	    buffer.append(obj[i]);
	    buffer.append(" ");
	}
	buffer.append("]");

	return buffer.toString();
    }

    public static String stringImg(Object[] v){

	if(v==null) return "null";

	StringBuffer buffer = new StringBuffer();

	Arrays.sort(v,UComp.uc);
	for(int i=0; i<v.length;i++){
	    buffer.append(v[i]);
	    buffer.append("\n");
	}

	return buffer.toString();
    }

    /** Displays a <i>split</i> relation (see the MetaCallGraph stuff). */
    public static void show_split(Relation split){
	for(Iterator it = split.keySet().iterator(); it.hasNext(); ){
	    HMethod hm = (HMethod) it.next();
	    System.out.print(hm);
	    System.out.println("  (" + split.getValuesSet(hm).size() +
			       " specialization(s))");
	    for(Iterator itmm = split.getValues(hm); itmm.hasNext(); ){
		System.out.print("  ");
		System.out.println((MetaMethod)itmm.next());
	    }
	}
    }


    // the powers of 10!!
    private static double[] fact = 
             {1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0};


    /** Returns the string representation of the double d, with a certain
	number of decimals. Let me know if you know something better. */
    public static String doubleRep(double d, int decimals){
	double dfloor = Math.floor(d); // (double) integer part of d
	double dfrac = d - dfloor;     // fractional part of d
	int ifloor = (int) dfloor;     // integer part of d
	// the last d decimals
	int fracint = (int) Math.ceil(dfrac * fact[decimals]); 

	StringBuffer fracstr = new StringBuffer(String.valueOf(fracint));
	int missing_decs = decimals - fracstr.length();
	for(int i = 0; i < missing_decs; i++)
	    fracstr.append("0");

	return String.valueOf(ifloor) + "." + fracstr.toString();
    }

    /** Returns the string representation of the double d, with a certain
	number of decimals. Let me know if you know something better. */
    public static String doubleRep(double d, int digits, int decimals){
	StringBuffer buffer = new StringBuffer(Debug.doubleRep(d,decimals));
	int desired_point = digits - decimals - 1;
	int current_point = buffer.toString().indexOf(".");
	int needed_spaces = desired_point - current_point;
	if(needed_spaces == 0) return buffer.toString();
	StringBuffer buffer2 = new StringBuffer();
	for(int i = 0; i < needed_spaces; i++)
	    buffer2.append(" ");
	return buffer2.toString() + buffer.toString();
    }

}
