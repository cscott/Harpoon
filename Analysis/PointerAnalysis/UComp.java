// UComp.java, created Thu Feb 10 13:22:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Comparator;

/**
 * <code>UComp</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: UComp.java,v 1.1.2.1 2000-02-11 06:15:39 salcianu Exp $
 */
public class UComp implements Comparator{

    public static UComp uc = new UComp();

    public int compare(Object o1,Object o2){
	String str1 = o1.toString();
	String str2 = o2.toString();
	return str1.compareTo(str2);
    }
    public boolean equals(Object obj){
	return this == obj;
    } 
}
