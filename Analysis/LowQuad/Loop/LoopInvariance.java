// Loopinvariance.java, created Mon Jun 28 13:33:40 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.UseDef;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.LowQuad.PAOFFSET;
import harpoon.IR.LowQuad.PARRAY;
import harpoon.IR.LowQuad.PCALL;
import harpoon.IR.LowQuad.PFCONST;
import harpoon.IR.LowQuad.PFIELD;
import harpoon.IR.LowQuad.PFOFFSET;
import harpoon.IR.LowQuad.PGET;
import harpoon.IR.LowQuad.PMCONST;
import harpoon.IR.LowQuad.PMETHOD;
import harpoon.IR.LowQuad.PMOFFSET;
import harpoon.IR.LowQuad.POPER;
import harpoon.IR.LowQuad.PSET;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import net.cscott.jutil.WorkSet;

import java.util.Iterator;
/**
 * <code>LoopInvariance</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopInvariance.java,v 1.3 2004-02-08 01:52:54 cananian Exp $
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
		nxt.accept(visitor);
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

	void visitdefault(Quad q) {
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


	public void visit(Quad q) {
	    System.out.println("Not expected in LoopInvariance:" + q.toString());
	    removeflag=false;
	}

	    /* All of these redefined to avoid error messages!*/
	public void visit(harpoon.IR.Quads.AGET q)    {
	    removeflag=false;
	}

	public void visit(harpoon.IR.Quads.ASET q)    {
	    removeflag=false;
	}

	public void visit(harpoon.IR.Quads.CALL q)    {
	    removeflag=false;
	}

	public void visit(harpoon.IR.Quads.GET q)     {
	    removeflag=false;
	}

	public void visit(harpoon.IR.Quads.HANDLER q) {
	    //better not be in a loop!!!!!!!
	    removeflag=false;
	}

	public void visit(harpoon.IR.Quads.OPER q)    {
	    switch (q.opcode()) {
	    case Qop.DDIV:
	    case Qop.FDIV:
	    case Qop.IDIV:
	    case Qop.LDIV:
		removeflag=false;
		break;
	    default:
		visitdefault(q);
	    }
	}

	public void visit(harpoon.IR.Quads.SET q)     {
	    removeflag=false;
	}


	public void visit(ALENGTH q) {
	    visitdefault(q);
	}

	public void visit(ANEW q) {
	    //Not loop invariant
	    removeflag=false;
	}

	public void visit(ARRAYINIT q) {
	    removeflag=false;
	}

	public void visit(COMPONENTOF q) {
	    visitdefault(q);
	}

	public void visit(CONST q) {
	    visitdefault(q);
	}

	public void visit(FOOTER q) {
	    removeflag=false;
	}

	public void visit(HEADER q) {
	    removeflag=false;
	}

	public void visit(INSTANCEOF q) {
	    visitdefault(q);
	}

	public void visit(METHOD q) {
	    removeflag=false;
	}

	public void visit(MONITORENTER q) {
	    removeflag=false;
	}

	public void visit(MONITOREXIT q) {
	    removeflag=false;
	}

	public void visit(MOVE q) {
	    visitdefault(q);
	}

	public void visit(NEW q) {
	    removeflag=false;
	}

	public void visit(NOP q) {
	    visitdefault(q);
	}

	public void visit(PCALL q) {
	    //Calls aren't loop invariant...
	    //they might have side effects
	    removeflag=false;
	} 

	public void visit(PARRAY q)     { visitdefault(q); }
	public void visit(PFIELD q)     { visitdefault(q); }
	public void visit(PMETHOD q)    { visitdefault(q); }
	public void visit(PAOFFSET q)   { visitdefault(q); }
	public void visit(PFOFFSET q)   { visitdefault(q); }
	public void visit(PMOFFSET q)   { visitdefault(q); }
	public void visit(PFCONST q)    { visitdefault(q); }
	public void visit(PMCONST q)    { visitdefault(q); }

	public void visit(PGET q) {
	    removeflag=false;
	}

	public void visit(POPER q) {
	    switch (q.opcode()) {
	    case Qop.DDIV:
	    case Qop.FDIV:
	    case Qop.IDIV:
	    case Qop.LDIV:
		removeflag=false;
		break;
	    default:
		visitdefault(q);
	    }
	}

	public void visit(PSET q) {
	    removeflag=false;
	}

	public void visit(RETURN q) {
	    removeflag=false;
	}

	public void visit(SIGMA q) {
	    removeflag=false;
	}
	
	public void visit(THROW q) {
	    removeflag=false;
	}

	public void visit(PHI q) {
	    removeflag=false;
	}
    }
}

