// LockRemove.java, created Fri Nov 12 14:03:59 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.EventDriven;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.Temp.Temp;
import harpoon.Util.Tuple;
import harpoon.Util.Collections.WorkSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>LockRemove</code>
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: LockRemove.java,v 1.1.2.3 2001-11-08 00:21:41 cananian Exp $
 */
public class LockRemove implements HCodeFactory {
    Set hclassSet;
    Map typeMap;
    HCodeFactory parent;

    public LockRemove(Set hclassSet, Map typeMap, HCodeFactory parent) {
	this.hclassSet=hclassSet;
	this.typeMap=typeMap;
	this.parent=parent;
    }

    public void clear(HMethod m) { parent.clear(m); }

    public String getCodeName() { return parent.getCodeName(); }

    public HCode convert(HMethod m) {
	// Clone the class that main was in, and replace it
	HCode hc = parent.convert(m);
	if (hc!=null)
	    stripLocks(hc);
	return hc;
    }
    
    private void stripLocks(HCode hc) {
	WorkSet done=new WorkSet();
	WorkSet todo=new WorkSet();
	LockVisitor visitor=new LockVisitor(hclassSet, typeMap,hc);
	todo.add(hc.getRootElement());
	while(!todo.isEmpty()) {
	    Quad q=(Quad)todo.pop();
	    done.add(q);
	    for (int i=0;i<q.nextLength();i++)
		if (!done.contains(q.next(i)))
		    todo.add(q.next(i));
	    q.accept(visitor);
	}
    }

    static class LockVisitor extends QuadVisitor {
	Set hclassSet;
	Map typeMap;
	HCode hc;

	LockVisitor(Set hclassSet, Map typeMap, HCode hc) {
	    this.hclassSet=hclassSet;
	    this.typeMap=typeMap;
	    this.hc=hc;
	}

	public void visit(Quad q) {}

	public void visit(MONITORENTER q) {
//	    Temp lock=q.lock();
	    boolean remove=true;
//  	    HClass hcl=null;
//  	    if (typeMap.containsKey(new Tuple(new Object[] {q,lock})))
//  		hcl=(HClass)typeMap.get(new Tuple(new Object[] {q,lock}));
//  	    else
//  		hcl=((QuadNoSSA)hc).typeMap.typeMap(q,lock);
//  	    Iterator iter=hclassSet.iterator();
//  	    while (iter.hasNext()) {
//  		HClass oth=(HClass)iter.next();
//  		System.out.println(oth+"   "+hcl);
//  		if (oth.isAssignableFrom(hcl)||
//  		    hcl.isAssignableFrom(oth)) {
//  		    remove=false;
//  		    break;
//  		}
//  	    }
	    
	    if (remove)
		Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),
			     q.next(0),q.nextEdge(0).which_pred());
	}

	public void visit(MONITOREXIT q) {
//	    Temp lock=q.lock();
	    boolean remove=true;
//  	    HClass hcl=null;
//  	    if (typeMap.containsKey(new Tuple(new Object[] {q,lock})))
//  		hcl=(HClass)typeMap.get(new Tuple(new Object[] {q,lock}));
//  	    else 
//  		hcl=((QuadNoSSA)hc).typeMap.typeMap(q,lock);
//  	    Iterator iter=hclassSet.iterator();
//  	    while (iter.hasNext()) {
//  		HClass oth=(HClass)iter.next();
//  		System.out.println(oth+"   "+hcl);
//  		if (oth.isAssignableFrom(hcl)||
//  		    hcl.isAssignableFrom(oth)) {
//  		    remove=false;
//  		    break;
//  		}
//  	    }
		
	    if (remove)
		Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(),
			     q.next(0),q.nextEdge(0).which_pred());
	}
    }
}


