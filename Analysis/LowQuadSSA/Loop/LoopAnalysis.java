// LoopAnalysis.java, created Thu Jun 24 11:45:07 1998 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuadSSA.Loop;

import harpoon.Analysis.UseDef;
import harpoon.Analysis.Loops.LoopFinder;
import harpoon.Util.WorkSet;
import harpoon.ClassFile.*;
import harpoon.IR.LowQuad.*;
import harpoon.IR.Quads.*;
import harpoon.Analysis.Loops.*;
import harpoon.Temp.Temp;
import harpoon.Analysis.SSITOSSAMap;
import harpoon.Temp.TempMap;
import harpoon.IR.Properties.HasEdges;

import java.util.Set;
import java.util.Iterator;
/**
 * <code>LoopAnalysis</code> implements 
 * 
 * @author  Brian Demsky
 * @version $Id: LoopAnalysis.java,v 1.1.2.1 1999-06-24 18:18:21 bdemsky Exp $
 */

public class LoopAnalysis {

    HCode hc;
    LoopFinder loopf;
    TempMap tm;

    /** Creates a <code>Loop</code>. */
    public LoopAnalysis(TempMap tm) {
	this.tm=tm;
    }

    /*-----------------------------*/
    // Class state.

    /*---------------------------*/
    // public information accessor methods.

    public void test(HCode hc) {
	analyze(hc);
    }

    /*---------------------------*/
    // Analysis code.

    /** Set of analyzed methods. */

    /** Main analysis method. */
    void analyze(HCode hc) {
	this.hc=hc;
	loopf=new LoopFinder(hc);
	printtree(loopf,"");
    } // end analysis.
    
    void printtree(Loops lp, String st) {
	WorkSet kids=(WorkSet)lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext()) {
	    printtree((Loops)iterate.next(),st+" ");
	}
	//Find loop invariants
	WorkSet elements=(WorkSet)lp.loopIncelements();
	WorkSet invariants=invariance(elements);

	//Find basic induction variables
	WorkSet basicinduction=basicInduction(lp,invariants);

	WorkSet allInductions=allInductions(lp, invariants, basicinduction);


	iterate=invariants.iterator();

	System.out.println(st+"Invariants:");
	while (iterate.hasNext()) {
	    System.out.println(st+((Quad)iterate.next()).toString());
	}
	iterate=elements.iterator();
	System.out.println(st+"Noninvariants:");
	while (iterate.hasNext()) {
	    System.out.println(st+((Quad)iterate.next()).toString());
	}
	iterate=basicinduction.iterator();

	System.out.println(st+"Basic induction variables:");
	while (iterate.hasNext()) {
	    System.out.println(st+((Temp)iterate.next()).toString());
	}
    }


    
    /*==========================================================*/

    WorkSet allInductions(Loops lp, WorkSet invariants, WorkSet basicinductions) {
	WorkSet allInductions=new WorkSet(basicinductions);
	CompleteVisitor visitor=new CompleteVisitor(allInductions,invariants);
	WorkSet elements=new WorkSet(lp.loopIncelements());
       	// Want to look for patterns like:
	/*  k=j*b, k=j+-b, k=b+-j     */
	// Use the representation:
	// (i*a+b)*pa+pb
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
	WorkSet inductions,invariants;
	boolean changed;

	CompleteVisitor(WorkSet inductions,WorkSet invariants) {
	    changed=false;
	    this.inductions=inductions;
	    this.invariants=invariants;
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
	    if (inductions.contains(tm.tempMap(q.index()))) {
		changed=true;
		inductions.push(q.dst());
	    } else
		changed=false;
	}

	public void visit(POPER q) {
	    switch (q.opcode()) {
	    case LQop.PADD:
	    case Qop.IADD:
	    case Qop.IMUL:
		//Binary operators		
		int invar=0;
		boolean good=true;
		for (int i=0;i<q.operandsLength();i++) {
		    Temp t=tm.tempMap(q.operands(i));
		    if (inductions.contains(t))
			invar++;
		    else
			if (!invariants.contains(t)) {
			    good=false;
			    break;
			}
		}
		//Need one induction variable and one constant!
		if ((invar==1)&&(good)) {
		    changed=true;
		    inductions.push(q.dst());
		} else
		    changed=false;
		break;

	    case Qop.INEG:
	    case LQop.PNEG:
		//Unary operators
		if (inductions.contains(tm.tempMap(q.operands(0)))) {
		    changed=true;
		    inductions.push(q.dst());
		} else
		    changed=false;
		break;
	    default:
	    }
	}
    }


    /*==========================================================*/
    WorkSet basicInduction(Loops lp, WorkSet invariants) {
	WorkSet basicinductions=new WorkSet();

	/*Get the loop header and
	  make sure that this is a single entrance loop.*/
	Iterator iterate=(lp.loopEntrances()).iterator();
	Quad header=(Quad) iterate.next();
	if (iterate.hasNext())
	    System.out.println("This routine doesn't work on multiple entrance loops");	

	/*Set up Sets and the UseDef map*/

	WorkSet excelements=(WorkSet) lp.loopExcelements();
	WorkSet incelements=(WorkSet) lp.loopIncelements();
	UseDef ud= new UseDef();
	WorkSet adds=new WorkSet();
	WorkSet phis=new WorkSet();

	/* Find phi's and adds in this loop's elements.*/
	BasicVisitor visitor=new BasicVisitor(adds, phis);
	iterate=excelements.iterator();
	while (iterate.hasNext()) {
	    Quad q=(Quad)iterate.next();
	    q.visit(visitor);
	}


	/* Now lets iterate over the add statements.*/
	iterate=adds.iterator();
	while (iterate.hasNext()) {

	    //Get the add statement
	    POPER q=(POPER) iterate.next();
	    //debug
	    //Find the number of operands it has
	    int numofoperands=q.operandsLength();

	    //Set the flag that this is a basic induction
	    boolean basicinduction=true;

	    //Make sure the phi links back to us
	    boolean tous=false;

	    //Set the corresponding phi pointer to null
	    PHI phi=null;

	    //Loop over the operands in the add statement
	    for (int i=0;i<numofoperands;i++) {

		//Find the SSA temp for the operand
		Temp origin=tm.tempMap(q.operands(i));
     		HCodeElement []sources=ud.defMap(hc,origin);

		//See if our list of phis contains sources[0]
		if (phis.contains(sources[0])) {

		    //Have we already seen a different phi?
		    if ((phi!=sources[0])&&(phi!=null)) {

			//Yep...this isn't a basic induction variable
			basicinduction=false;
			break;
		    } else if (phi==null) {

			//Haven't seen a phi yet!

			//Set the phi pointer here
     			phi=(PHI)sources[0];

			//If the phi isn't the header, it isn't a basic induction variable

			if (phi!=header) {
			    basicinduction=false;
			    break;
			}

			//We need to search for the phi statement...
			int k=0; //Get the right phi statement
			while (origin!=phi.dst(k)) k++;

			//Get an array of the predecessor edges
			HCodeEdge[] pred=((HasEdges) phi).pred();
			//Loop through the edges of this phi
			for (int j=0; j<phi.arity();j++) {

			    //If the loop contians the incoming edge,
			    //the phi needs to use our 
			    if(incelements.contains(pred[j].from())) {

				//Needs to be  = to us!
				if(tm.tempMap(phi.src(k,j))!=q.dst()) {
				    //Else it isn't a basic induction variable
				    basicinduction=false;
				    break;
				} else {
				    tous=true;
				}
			    }
			}
		    }
		} else {
		    //If it isn't from a phi, it needs to be loop invariant!!!
		    //Look to see if this is loop invariant!
		    if ((!invariants.contains(sources[0]))&&(incelements.contains(sources[0]))) {
			//Conditions are that it comes from inside the loop and it isn't
			//loop invariant

			//We don't have an invariant...no go!
			basicinduction=false;
			break;
		    }
		}
	    }
	    //If it made it through the checks, it is a basic induction variable...
	    if (basicinduction&&(phi!=null)&&tous) basicinductions.push(q.dst());
	}
	return basicinductions;
    }

    class BasicVisitor extends LowQuadVisitor {
	WorkSet adds;
	WorkSet phis;

	BasicVisitor(WorkSet adds,WorkSet phis) {
	    this.adds=adds;
	    this.phis=phis;
	}
	
	public void visit(Quad q) {
	    //Do nothing
	}

	public void visit(OPER q) {
	    System.out.println("OPER found in LowQuad form.  Something is weird!");
	}

	public void visit(POPER q) {
	    switch (q.opcode()) {
	    case LQop.PADD:
	    case Qop.IADD:
		//	    case Qop.LADD:
		adds.push(q);
		break;
	    default:
	    }
	}

	public void visit(PHI q) {
	    phis.push(q);
	}
    }
    

    /*==================================================================*/

    WorkSet invariance(WorkSet elements) {
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

	public void visit(ASET q) {
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
