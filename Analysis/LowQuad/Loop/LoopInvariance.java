// Loopinvariance.java, created Mon Jun 28 13:33:40 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.UseDef;
import harpoon.ClassFile.*;
import harpoon.IR.LowQuad.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.Util.WorkSet;

import java.util.Iterator;
/**
 * <code>LoopInvariance</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopInvariance.java,v 1.1.2.2 1999-06-29 17:24:24 bdemsky Exp $
 */
public class LoopInvariance {
    

    TempMap tm;
    HCode hc;

    /** Creates a <code>LoopInvariance</code>. */
    public LoopInvariance(TempMap tm,HCode hc) {
        this.tm=tm;
	this.hc=hc;
    }

    /** Creates a <code>WorkSet</code> containing <code>Quad</code>s that
     *  are loop invariant.  Takes in a <code>WorkSet</code> of 
     *  <code>Quad</code>s that are in the loop. */

    public WorkSet invariants(WorkSet elements) {
	WorkSet invariants=new WorkSet();
   	harpoon.Analysis.UseDef ud = new harpoon.Analysis.UseDef();
       	boolean change=true;
	InvariantVisitor visitor=new InvariantVisitor(ud, elements, invariants);
	while (visitor.change()) {
	    visitor.reset();
	    Iterator ourself=elements.iterator();
	    while (ourself.hasNext()) {
		Quad nxt=(Quad)ourself.next();
		//is this node invariant?
		nxt.visit(visitor);
		if (visitor.remove())
		    ourself.remove();
		//doesn't depend on this loop...so add it to invariants
	    }
	}
	return invariants;
    }

    /** <code>InvariantVisitor</code> visits Quads and determines if they are
     *  loop invariant.*/

    class InvariantVisitor extends LowQuadVisitor {
	UseDef ud;
	WorkSet invariants;
	boolean change;
	WorkSet elements;
	boolean removeflag;

	InvariantVisitor(UseDef ud, WorkSet elements, WorkSet invariants) {
	    this.ud=ud;
	    this.invariants=invariants;
	    this.elements=elements;
	    change=true;
	}
	
	public boolean remove() {
	    return removeflag;
	}
      
	public boolean change() {
	    return change;
	}

	public void reset() {
	    change=false;
	}

	public void visit(Quad q) {
	    Temp [] uses=q.use();
	    boolean ours=false;
	    for (int i=0;i<uses.length;i++) {
		HCodeElement []sources=ud.defMap(hc,tm.tempMap(uses[i]));
		for (int j=0;j<sources.length;j++) {
		    if (elements.contains(sources[j])) {
			ours=true; break;
		    }
		}
	    }	    
	    if (ours==false) {
		change=true;
		removeflag=true;
		invariants.push(q);
	    } else
		removeflag=false;
	}

	public void visit(ANEW q) {
	    //Not loop invariant
	    removeflag=false;
	}

	public void visit(ARRAYINIT q) {
	    removeflag=false;
	}

	public void visit(PCALL q) {
	    //Calls aren't loop invariant...
	    //they might have side effects
	    removeflag=false;
	}
	
	public void visit(SIGMA q) {
	    removeflag=false;
	}
	
	public void visit(PHI q) {
	    removeflag=false;
	}
    }
}
