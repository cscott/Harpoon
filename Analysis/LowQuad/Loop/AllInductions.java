// AllInductions.java, created Mon Jun 28 13:40:31 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.Loops.Loops;
import harpoon.Util.Collections.WorkSet;
import harpoon.Analysis.UseDef;
import harpoon.ClassFile.HCode;
import harpoon.IR.LowQuad.LQop;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.LowQuad.PAOFFSET;
import harpoon.IR.LowQuad.POPER;
import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.SET;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;

/**
 * <code>AllInductions</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: AllInductions.java,v 1.2 2002-02-25 20:57:58 cananian Exp $
 */
public class AllInductions {
    TempMap tm;
    HCode hc;

    /** Creates a <code>AllInductions</code> object. */
    public AllInductions(TempMap tm, HCode hc) {
        this.tm=tm;
	this.hc=hc;
    }
    
    /** Returns a Hashmap mapping induction <code>Temp</code>s
     *  to <code>Induction</code> objects containing information
     *  on their derivation.*/

    public HashMap doAllInductions(Loops lp, WorkSet invariants, HashMap basicinductions) {
	HashMap allInductions=new HashMap(basicinductions);
	CompleteVisitor visitor=new CompleteVisitor(allInductions,invariants);
	WorkSet elements=new WorkSet(lp.loopIncElements());
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
		element.accept(visitor);
		if (visitor.change()) {
		    change=true;
		    iterate.remove();
		}
	    }
	}
	return allInductions;
    }


    /** CompleteVisitor extends <code>LowQuadVisitor</code>
     *  and is used to find derived induction variables. */
     
    class CompleteVisitor extends LowQuadVisitor {
	HashMap inductions;
	WorkSet invariants;
	boolean changed;
	UseDef ud;

	CompleteVisitor(HashMap inductions,WorkSet invariants) {
	    super(false/*non-strict*/);
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
	    switch (q.opcode()) {
	    case Qop.IADD:
		//Binary operators		
		InstanceofCONSTVisitor visitor=new InstanceofCONSTVisitor();
		boolean good=true;
		int invar=0;
		int index=0;
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
		//Need one induction variable and constants
		if ((invar==1)&&(good)) {
		    changed=true;
		    //*****************
		    Induction tmp=new Induction((Induction)inductions.get(tm.tempMap(q.operands(index))));
		    for (int i=0;i<q.operandsLength();i++)
			if (i!=index) {
			    Temp t=tm.tempMap(q.operands(i));
			    visitor.reset();
			    ((Quad)ud.defMap(hc,t)[0]).accept(visitor);
			    if (visitor.resetstatus())
				tmp=tmp.add
				    (((Integer)(((CONST)ud.defMap(hc,t)[0]).value())).intValue());
			    else
				tmp=tmp.add(tm.tempMap(q.operands(i)));
			}
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;



	    case Qop.IMUL:
		//Binary operators		
		visitor=new InstanceofCONSTVisitor();
		good=true;
	        invar=0;
		index=0;
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
		//Need one induction variable and constants
		if ((invar==1)&&(good)) {
		    changed=true;
		    //*****************
		    Induction tmp=new Induction((Induction)inductions.get(tm.tempMap(q.operands(index))));
		    for (int i=0;i<q.operandsLength();i++)
			if (i!=index) {
			    Temp t=tm.tempMap(q.operands(i));
			    visitor.reset();
			    ((Quad)ud.defMap(hc,t)[0]).accept(visitor);
			    if (visitor.resetstatus())
				tmp=tmp.multiply(
						 ((Integer)((CONST)ud.defMap(hc,t)[0]).value()).intValue());
			    else
				tmp=tmp.multiply(tm.tempMap(q.operands(i)));
			}
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;

	    case Qop.INEG:
		//Unary operators
		
		if (inductions.containsKey(tm.tempMap(q.operands(0)))) {
		    changed=true;
		    //****************
		    Induction tmp=((Induction)inductions.get(tm.tempMap(q.operands(0)))).negate();
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;
	    default:
	    }
	}
	
	
	public void visit(PAOFFSET q) {
	    //Find if induction variable
	    if (inductions.containsKey(tm.tempMap(q.index()))) {
		changed=true;

		Induction tmp=(Induction)inductions.get(tm.tempMap(q.index()));
		Util.assert(tmp.pointerindex==false);

		//*********
		inductions.put(q.dst(), new Induction(tmp,q.arrayType()));

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
			    tmp.padd(tm.tempMap(q.operands(i)));
		    }
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;


	    case Qop.IADD:
		//Binary operators		
		InstanceofCONSTVisitor visitor=new InstanceofCONSTVisitor();
		good=true;
		invar=0;
		index=0;
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
		//Need one induction variable and constants
		if ((invar==1)&&(good)) {
		    changed=true;
		    //*****************
		    Induction tmp=new Induction((Induction)inductions.get(tm.tempMap(q.operands(index))));
		    for (int i=0;i<q.operandsLength();i++)
			if (i!=index) {
			    Temp t=tm.tempMap(q.operands(i));
			    visitor.reset();
			    ((Quad)ud.defMap(hc,t)[0]).accept(visitor);
			    if (visitor.resetstatus())
				tmp=tmp.add
				    (((Integer)(((CONST)ud.defMap(hc,t)[0]).value())).intValue());
			    else
				tmp=tmp.add(tm.tempMap(q.operands(i)));
			}
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;



	    case Qop.IMUL:
		//Binary operators		
		visitor=new InstanceofCONSTVisitor();
		good=true;
	        invar=0;
		index=0;
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
		//Need one induction variable and constants
		if ((invar==1)&&(good)) {
		    changed=true;
		    //*****************
		    Induction tmp=new Induction((Induction)inductions.get(tm.tempMap(q.operands(index))));
		    for (int i=0;i<q.operandsLength();i++)
			if (i!=index) {
			    Temp t=tm.tempMap(q.operands(i));
			    visitor.reset();
			    ((Quad)ud.defMap(hc,t)[0]).accept(visitor);
			    if (visitor.resetstatus())
				tmp=tmp.multiply(
						 ((Integer)((CONST)ud.defMap(hc,t)[0]).value()).intValue());
			    else
				tmp=tmp.multiply(tm.tempMap(q.operands(i)));
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
		    Induction tmp=((Induction)inductions.get(tm.tempMap(q.operands(0)))).negate();
		    inductions.put(q.dst(),tmp);
		} else
		    changed=false;
		break;
	    default:
	    }
	}
    }

    /** InstanceofCONSTVisitor allows <code>CompleteVisitor</code> to do its
     *  work without using an INSTANCEOF.  */

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
