// AllInductions.java, created Mon Jun 28 13:40:31 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.Loops.*;
import harpoon.Util.WorkSet;
import harpoon.Analysis.UseDef;
import harpoon.ClassFile.*;
import harpoon.IR.LowQuad.*;
import harpoon.IR.Properties.HasEdges;
import harpoon.IR.Quads.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * <code>AllInductions</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: AllInductions.java,v 1.1.2.1 1999-06-28 22:55:08 bdemsky Exp $
 */
public class AllInductions {
    TempMap tm;
    HCode hc;

    /** Creates a <code>AllInductions</code>. */
    public AllInductions(TempMap tm, HCode hc) {
        this.tm=tm;
	this.hc=hc;
    }
    
    HashMap doAllInductions(Loops lp, WorkSet invariants, HashMap basicinductions) {
	HashMap allInductions=new HashMap(basicinductions);
	CompleteVisitor visitor=new CompleteVisitor(allInductions,invariants);
	WorkSet elements=new WorkSet(lp.loopIncelements());
       	// Want to look for patterns like:
	/*  k=j*b, k=j+-b, k=b+-j     */
	// Use the representation:
	// (i*a+b)*pa+pb-->i*(a+pa)+(b*pa+pb)
	boolean change=true;
	while (change) {
	    change=false;
	    Iterator iterate=elements.iterator();
	    while (iterate.hasNext()) {
		Quad element=(Quad)iterate.next();
		element.visit(visitor);
		if (visitor.change()) {
		    change=true;
		    iterate.remove();
		}
	    }
	}
	return allInductions;
    }



    class CompleteVisitor extends LowQuadVisitor {
	HashMap inductions;
	WorkSet invariants;
	boolean changed;
	UseDef ud;

	CompleteVisitor(HashMap inductions,WorkSet invariants) {
	    changed=false;
	    this.inductions=inductions;
	    this.invariants=invariants;
	    ud=new UseDef();
	}
	
	public boolean change() {
	    return changed;
	}

	public void visit(Quad q) {
	    //Do nothing
	}

	public void visit(OPER q) {
	    System.out.println("OPER found in LowQuad form.  Something is weird!");
	}
	
	public void visit(PAOFFSET q) {
	    //Find if induction variable
	    if (inductions.containsKey(tm.tempMap(q.index()))) {
		changed=true;

		Induction tmp=(Induction)inductions.get(tm.tempMap(q.index()));
		Util.assert(tmp.pointerindex==false);

		//*********
		inductions.put(q.dst(), new Induction(tmp.variable,tmp.offset,tmp.intmultiplier,q.arrayType(),tmp.pointeroffset));

	    } else
		changed=false;
	}

	public void visit(POPER q) {
	    switch (q.opcode()) {
	    case LQop.PADD:
		//Binary operators		
		int invar=0;
		int index=0;
		boolean good=true;
		for (int i=0;i<q.operandsLength();i++) {
		    Temp t=tm.tempMap(q.operands(i));
		    if (inductions.containsKey(t)) {
			index=i;
			invar++;
		    }
		    else
			if (!invariants.contains(ud.defMap(hc,t)[0])) {
			    good=false;
			    break;
			}
		}
		//Need one induction variable and invariants
		if ((invar==1)&&(good)) {
		    changed=true;
		    //*****************
		    Induction tmp=new Induction((Induction)inductions.get(tm.tempMap(q.operands(index))));
		    for (int i=0;i<q.operandsLength();i++) {
			if (i!=index)
			    tmp.pointeroffset.add(tm.tempMap(q.operands(i)));
		    }
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;


	    case Qop.IADD:
		//Binary operators		
		InstanceofCONSTVisitor visitor=new InstanceofCONSTVisitor();

		invar=0;
		index=0;
		for (int i=0;i<q.operandsLength();i++) {
		    Temp t=tm.tempMap(q.operands(i));
		    if (inductions.containsKey(t)) {
			index=i;
			invar++;
		    }
		    else
			((Quad)ud.defMap(hc,t)[0]).visit(visitor);
		}
		//Need one induction variable and constants
		if ((invar==1)&&visitor.resetstatus()) {
		    changed=true;
		    //*****************
		    Induction tmp=new Induction((Induction)inductions.get(tm.tempMap(q.operands(index))));
		    for (int i=0;i<q.operandsLength();i++)
			if (i!=index)
			    tmp.offset+=
				((Integer)(((CONST)ud.defMap(hc,tm.tempMap(q.operands(i)))[0]).value())).intValue();
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;



	    case Qop.IMUL:
		//Binary operators		
		visitor=new InstanceofCONSTVisitor();

	        invar=0;
		index=0;
		for (int i=0;i<q.operandsLength();i++) {
		    Temp t=tm.tempMap(q.operands(i));
		    if (inductions.containsKey(t)) {
			index=i;
			invar++;
		    }
		    else
			((Quad)ud.defMap(hc,t)[0]).visit(visitor);
		}
		//Need one induction variable and constants
		if ((invar==1)&&visitor.resetstatus()) {
		    changed=true;
		    //*****************
		    Induction tmp=new Induction((Induction)inductions.get(tm.tempMap(q.operands(index))));
		    for (int i=0;i<q.operandsLength();i++)
			if (i!=index) {
			    int mult=
				((Integer)((CONST)ud.defMap(hc,tm.tempMap(q.operands(i)))[0]).value()).intValue();
			    tmp.offset*=mult;
			    tmp.intmultiplier*=mult;
			}
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;

	    case Qop.INEG:
	    case LQop.PNEG:
		//Unary operators
		
		if (inductions.containsKey(tm.tempMap(q.operands(0)))) {
		    changed=true;
		    //****************
		    Induction tmp=new Induction((Induction)inductions.get(tm.tempMap(q.operands(0))));
		    tmp.intmultiplier=-tmp.intmultiplier;
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;
	    default:
	    }
	}
    }

    class InstanceofCONSTVisitor extends LowQuadVisitor {
	boolean reset;
	public InstanceofCONSTVisitor() {
	    reset=true;
	}

	public boolean resetstatus() {
	    return reset;
	}

	public void reset() {
	    reset=true;
	}
 
	public void visit(Quad q) {
	    reset=false;
	}

	public void visit(CONST q) {
	}
    }

}






