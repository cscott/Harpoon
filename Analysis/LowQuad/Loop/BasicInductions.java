// BasicInduction.java, created Mon Jun 28 13:38:30 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.Loops.LoopFinder;
import harpoon.Analysis.Loops.Loops;
import harpoon.Analysis.LowQuad.Loop.Induction;
import harpoon.Analysis.UseDef;
import harpoon.ClassFile.*;
import harpoon.IR.LowQuad.*;
import harpoon.IR.Properties.HasEdges;
import harpoon.IR.Quads.*;
import harpoon.Util.WorkSet;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;


import java.util.HashMap;
import java.util.Iterator;


/**
 * <code>BasicInductions</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: BasicInductions.java,v 1.1.2.4 1999-09-22 06:07:45 bdemsky Exp $
 */
public class BasicInductions {
    HCode hc;
    TempMap tm;

    /**  Creates a <code>BasicInductions</code> object. */
    public BasicInductions(TempMap tm, HCode hc) {
	this.hc=hc;
	this.tm=tm;
    }

    /** Creates a <code>HashMap</code> mapping <code>Temp</code>s to 
     *  <code>Induction</code> classes describing the induction variable. 
     *  This code only finds basic induction variables. */

    public HashMap doInduction(Loops lp, WorkSet invariants) {
	HashMap basicinductions=new HashMap();
	
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
	WorkSet addp=new WorkSet();
	WorkSet phis=new WorkSet();

	/* Find phi's and adds in this loop's elements.*/
	BasicVisitor visitor=new BasicVisitor(adds, addp, phis);
	iterate=excelements.iterator();
	while (iterate.hasNext()) {
	    Quad q=(Quad)iterate.next();
	    q.accept(visitor);
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
	    int k=0;

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
			k=0; //Get the right phi statement
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
	    if (basicinduction&&(phi!=null)&&tous) {
		Induction tmp;
		if (addp.contains(q)) 
		    tmp=new Induction(phi.dst(k),null,true);
		    else
		    tmp=new Induction(phi.dst(k),0,1);
       		basicinductions.put(phi.dst(k),tmp);
	    }
	}
	return basicinductions;
    }

    /** BasicVisitor finds ADD and <code>PHI</code> quads.  These
     *  are used to find basic induction variables.*/

    class BasicVisitor extends LowQuadVisitor {
	WorkSet adds,addp;
	WorkSet phis;

	BasicVisitor(WorkSet adds, WorkSet addp, WorkSet phis) {
	    this.adds=adds;
	    this.addp=addp;
	    this.phis=phis;
	}
	
	public void visit(Quad q) {
	    //Do nothing
	}

	/* All of these redefined to avoid error messagqes!*/
	public void visit(harpoon.IR.Quads.AGET q)    {}

	public void visit(harpoon.IR.Quads.ASET q)    {}

	public void visit(harpoon.IR.Quads.CALL q)    {}

	public void visit(harpoon.IR.Quads.GET q)     {}

	public void visit(harpoon.IR.Quads.HANDLER q) {}

	public void visit(harpoon.IR.Quads.OPER q)    {}

	public void visit(harpoon.IR.Quads.SET q)     {}

	public void visit(OPER q) {
	    switch (q.opcode()) {
	    case Qop.IADD:
		//	    case Qop.LADD:
		adds.push(q);
		break;
	    default:
	    } 
	}

	public void visit(POPER q) {
	    switch (q.opcode()) {
	    case LQop.PADD:
		addp.push(q);//put in both list!
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
}

