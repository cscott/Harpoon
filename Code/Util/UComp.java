// UComp.java, created Thu Feb 10 13:22:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Comparator;

/**
 * <code>UComp</code> is an universal comparator, which compares any two
 objecs by simply compraing their string representation. It is useful
 when you need a deterministic string representation of a set (for debug
 purposes). As sets don't have any ordering info, two equal sets
 could have different representations; instead you can convert the set to
 an array of elements, sort it with the help of this universal comparator
 and print it.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: UComp.java,v 1.2 2002-02-25 21:08:55 cananian Exp $
 */
public class UComp implements Comparator{

    public static UComp uc = new UComp();

    /** Compares its two arguments for order.
	Returns a negative integer, zero, or a positive integer as the
	string representation of the first argument is less than, equal
	to, or greater to the string representation of the second. */
    public int compare(Object o1,Object o2){
	if(o1 == o2)   return 0;
	String str1 = (o1 == null)?"null":o1.toString(); 
	String str2 = (o2 == null)?"null":o2.toString();
	return str1.compareTo(str2);
    }

    /** Indicates whether some other object is &quot;equal&quot; to"
	<code>this</code> Comparator. The easiest implementation:
	always return <code>false</code> unless
	<code>obj == this</code> .*/
    public boolean equals(Object obj){
	return this == obj;
    } 
}
