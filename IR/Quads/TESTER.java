// TESTER.java, created Wed Nov 15 23:01:44 2000 by root
// Copyright (C) 2000 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;
import net.cscott.jutil.WorkSet;

import java.util.Iterator;
import java.util.Set;
/**
 * <code>TESTER</code> checks that a given <code>QuadWithTry</code> doesn't
 * mention any quads in its <code>HANDLER</code>'s protectedSets which
 * aren't reachable in the code.  For debugging purposes.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: TESTER.java,v 1.4 2004-02-08 03:21:24 cananian Exp $
 */
class TESTER {
    
    static void test(QuadWithTry qwt) {
	HEADER h=(HEADER)qwt.getRootElement();
	METHOD m=(METHOD)h.next(1);
	WorkSet ws=new WorkSet();
	Iterator it=qwt.getElementsI();
	while(it.hasNext())
	    ws.add(it.next());
	for(int i=1;i<m.arity();i++) {
	    HANDLER hand=(HANDLER)m.next(i);
	    Set s=hand.protectedSet();
	    for (Object qO : s) {
		Quad q = (Quad) qO;
		if(!ws.contains(q))
		    System.out.println("Found "+q+" in handler "+h+" edge "+i+ " that isn't reachable");
	    }
	}
	for (Object qO : ws) {
	    Quad q = (Quad) qO;
	    Edge[] next=q.nextEdge();
	    for(int i=0;i<next.length;i++) {
		if (next[i].from!=q)
		    System.out.println(q+" has bad next edge "+i);
		if (!ws.contains(next[i].to))
		    System.out.println(q+" has next edge "+ i+" pointing out of ws");
	    }
	    Edge[] prev=q.prevEdge();
	    for(int i=0;i<prev.length;i++) {
		if (prev[i].to!=q)
		    System.out.println(q+" has bad prev edge "+i);
		if (!ws.contains(prev[i].from))
		    System.out.println(q+" has prev edge "+ i+" pointing out of ws");
	    }
	}
    }
    
}
