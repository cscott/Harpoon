// A.java, created Mon Feb 15 14:40:30 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Test.PA.Test5;

/**
 * This is a test for the PA algorithm. It reveals a hidden
 problem in the original algorithm: the inclusion of the callee graph
 into the new graph was too restrictive and could even lose relevant
 info. The algorithm has been modified to be more conservative: the entire
 callee's graph is now included into the graph of the caller and later,
 after the recomputation of the escape info, the empty load nodes (the load
 nodes that don't escape anywhere and hence, don't abstract any object)
 and the fake outside edges (outside edges originating in a non-escaping
 node) are removed, together with all the related info.

 <code>A.foo</code> is the interesting method.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: A.java,v 1.2 2002-02-25 21:07:52 cananian Exp $
 */

class List{
    int k;
    List next;
}

public class A{
    static void foo(List p1){
	List l3 = B.bar(p1);
	List l4 = l3.next;
	return;
    }

    public static void main(String[] param){
	foo(new List());
    }
}

class B{
    static List bar(List p0){
	List l0 = new List();
	unanalyzed(l0,p0);
	List l1 = l0.next;
	List l2 = new List();
	l2.next = l1;
	return l2;
    }

    static void unanalyzed(List a, List b){
	a.next = b;
    }
}

