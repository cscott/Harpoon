// LoopOptimize.java, created Thu Jun 24 11:41:44 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;


import harpoon.ClassFile.*;
import harpoon.IR.LowQuad.*;
import harpoon.IR.Quads.*;
import harpoon.IR.Properties.HasEdges;
import harpoon.Analysis.UseDef;
import harpoon.Analysis.Loops.Loops;
import harpoon.Analysis.LowQuad.Loop.LoopAnalysis;
import harpoon.Analysis.SSITOSSAMap;
import harpoon.Analysis.LowQuad.Loop.LoopMap;
import harpoon.Analysis.Maps.AllInductionsMap;
import harpoon.Analysis.Maps.BasicInductionsMap;
import harpoon.Analysis.Maps.InvariantsMap;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.Analysis.QuadSSA.DeadCode;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 * <code>LoopOptimize</code> optimizes the code after <code>LoopAnalysis</code>.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopOptimize.java,v 1.1.2.13 1999-07-07 19:29:18 bdemsky Exp $
 */
public final class LoopOptimize {
    
    AllInductionsMap aimap;
    BasicInductionsMap bimap;
    InvariantsMap invmap;
    LoopAnalysis loopanal;
    TempMap ssitossamap;
    UseDef ud;

    /** Creates an <code>LoopOptimize</code>. */
    public LoopOptimize(AllInductionsMap aimap,BasicInductionsMap bimap,InvariantsMap invmap, LoopAnalysis loopanal, TempMap ssitossamap) {
	this.aimap=aimap;
	this.bimap=bimap;
	this.invmap=invmap;
	this.loopanal=loopanal;
	this.ssitossamap=ssitossamap;
	ud=new UseDef();
    }

    /**<code>LoopOptimize</code> constructor.  Used internally by codeFactory.*/
    public LoopOptimize(LoopAnalysis lanal, TempMap ssitossamap) {
	this(lanal,lanal,lanal,lanal, ssitossamap);
    }

    /** Returns a <code>HCodeFactory</code> that uses <code>LoopOptimize</code>. */
    public static HCodeFactory codeFactory(final HCodeFactory parent) {
	return new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode hc = parent.convert(m);
		SSITOSSAMap ssitossa=new SSITOSSAMap(hc);
		if (hc!=null) {
		    (new LoopOptimize(new LoopAnalysis(ssitossa),ssitossa)).optimize(hc);
		}
		return hc;
	    }
	    public String getCodeName() { return parent.getCodeName(); }
	    public void clear(HMethod m) { parent.clear(m); }
	};
    }

    /**<code>optimize</code> takes in a <code>HCode</code> and performs loop optimizations on it.
     * Optimization currently work only on loops with one entrance.   Furthermore, optimizations
     * currently only work on loops that the header node is a phi function of arity 2 [ie. natural
     * loops.]  This function really needs code that is passed to it to have been run through 
     * Deadcode.  Otherwise, it may make some very poor decisions on moving test conditions.
     */

    public void optimize(final HCode hc) {
    
	//Want defmap of temps in my invariants list, not new temps...
	//Force generation of defmap
	//Need to fix this!!!

	Temp []dummy=ud.allTemps(hc);	


	// actual traversal code.
	// We start at the root loop, and recurse down each of its subloops.
	Loops lp=loopanal.rootloop(hc);
	WorkSet kids=(WorkSet) lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext())
	    recursetree(hc, (Loops)iterate.next(), new WorkSet());
	
	//After doing optimizations we need to clean up any deadcode...

	DeadCode.optimize(hc);
    }

    /** <code>recursetree</code> recurses down the nested loop tree.*/
    void recursetree(HCode hc, Loops lp, WorkSet usedinvariants) {
	
	//We only treat loops with one entrance currently.  We only recognize
	//loops with one entrance, so this isn't a limitation.
	if (lp.loopEntrances().size()==1) {
	    Quad header=(Quad)(lp.loopEntrances()).toArray()[0];
	    if (header.prev().length==2) {
		//Hoist loop invariants.  The workset usedinvariants
		//keeps track of invariants we have moved, so we don't try again.

		doLoopinv(hc, lp,header, usedinvariants);

		//Create empty workset to pass induction variables that were created in.
		WorkSet newinds=new WorkSet();

		//Create induction variables.  Header has to be updated since
		//we most likely create new header nodes.
		header=doLoopind(hc, lp,header, usedinvariants, newinds);
		//Move loop test conditions from the original induction variable
		//to new ones
		doLooptest(hc, lp, header, newinds, usedinvariants);
	    }
	    else System.out.println("Multiple back edges.");
	} else
	    System.out.println("Multiple or No  entrance loop in LoopOptimize!");

	//Look for child loops.  If there are any, recurse to them.

	WorkSet kids=(WorkSet) lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext())
	    recursetree(hc, (Loops)iterate.next(),usedinvariants);
    }

    /** <code>doLooptest</code> moves test conditions from original induction variables
     * to new ones whenever possible.*/

    void doLooptest(HCode hc, Loops lp,Quad header, Set newinds, Set usedinvariants) {
	//Create the set of loop elements
	Set elements=lp.loopIncelements();

	//Iterate through this set
	Iterator iterate=elements.iterator();

	//create sets of loop invariants and map of induction variables
	//to pass to the visitor
	Set loopinvars=invmap.invariantsMap(hc,lp);
	Map allinductions=aimap.allInductionsMap(hc,lp);

	TestVisitor visitor=new TestVisitor(newinds, loopinvars, allinductions, header, ssitossamap,hc, lp);

	//visit the nodes
	while (iterate.hasNext()) {
	    Quad q=(Quad) iterate.next();

	    //make sure that our node hasn't been used...
	    //Isn't required [should be caught by the fact
	    //that loop invariant nodes don't rely on induction variables.]
	    if (!usedinvariants.contains(q))
		q.visit(visitor);
	}
    }


    /**<code>TestVisitor</code> does all the magic for changing test conditions.*/

    class TestVisitor extends LowQuadVisitor {
	Set inductvars;
	Set newinductvars;
	Set loopinvars;
	Map allinductions;
	Quad header;
	TempMap ssitossamap;
	Loops lp;
	HCode hc;

	//Create TestVisitor, and inform it of everything.
	TestVisitor(Set newinductvars, Set loopinvars, Map allinductions, Quad header, TempMap ssitossamap, HCode hc, Loops lp) {
	    this.newinductvars=newinductvars;
	    this.loopinvars=loopinvars;
	    this.allinductions=allinductions;
	    this.inductvars=allinductions.keySet();
	    this.header=header;
	    this.ssitossamap=ssitossamap;
	    this.hc=hc;
	    this.lp=lp;
	}

	//Default method...do nothing
	public void visit(Quad q) {/* Do nothing*/}

	//POPER visitor
	//Only look at ICMPEQ, and ICMPGT

	public void visit(POPER q) {
	    switch (q.opcode()) {
	    case Qop.ICMPEQ:
	    case Qop.ICMPGT:
		//look at the POPER
		//return new POPER if we can do stuff
		POPER replace=lookat(q);
		if (replace!=null) {
		    //Put the new POPER in its place
		    Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), replace,0);
		    Quad.addEdge(replace, 0, q.next(0), q.nextEdge(0).which_pred());
		}
		break;
	    default:
	    }
	}

	/**<code>lookat</code> examines a test condition, to see how we should replace it.*/
	POPER lookat(POPER q) {
	    Temp[] operands=q.operands();
	    Util.assert (operands.length==2);
	    boolean good=true;
	    int flag=-1;
	    POPER newpoper=null;

	    //Loop through the operands
	    //we need one induction variable
	    //and one loop invariant

	    for (int i=0;i<operands.length;i++) {
		if (inductvars.contains(ssitossamap.tempMap(operands[i]))) {
		    if (flag==-1)
			flag=i;
		    else
			good=false;
		}
		else {
		    HCodeElement[] sources=ud.defMap(hc,ssitossamap.tempMap(operands[i]));
		    Util.assert(sources.length==1);
		    if (!loopinvars.contains(sources[0]))
			good=false;
		}
	    }


	    //if we found these, look for possible replacement induction variables
	    if (good&&(flag!=-1)) {
		//get the Induction data type for the current induction variable
		Induction induction=(Induction)allinductions.get(ssitossamap.tempMap(operands[flag]));
		//Iterate through all of the newly created induction variables

		Iterator iterate=newinductvars.iterator();
		while (iterate.hasNext()) {
		    Temp indvar=(Temp) iterate.next();
		    Induction t=(Induction) allinductions.get(indvar);
		    /*Policy for moving:
		      1) Both have the same base induction variable.
		      2) The new one isn't used to derive any other induction variables.
		         Presumably it has a use for being around.
		      3) If the original test was on a pointer, make sure any new pointer
		         has the same stride.
		    */

		    if (t.variable!=induction.variable) 
			continue;
		    if (t.copied)
			continue;    
		    if ((induction.intmultiplier!=1)&&(induction.intmultiplier!=-1))
			if ((t.intmultiplier!=induction.intmultiplier)&&(t.intmultiplier!=-induction.intmultiplier))
			    continue;
		    if (induction.objectsize!=null) {
			if ((t.objectsize!=induction.objectsize)||(t.intmultiplier!=induction.intmultiplier))
			    continue;
		    }
		    
		    //Found something that passes the policy

		    Temp initial=operands[1-flag];
		    //Call the method to build us a new compare
		    newpoper=movecompare(header, initial, induction, indvar,t,q, flag, new LoopMap(hc,lp,ssitossamap));
     		    break;
		}
	    }
	    return newpoper;
	}
    }

    /** <code>movecompare</code> builds a new compare statement.  The new compared statement uses
     *	the derived induction variable t instead of the original induction.*/
    POPER movecompare(Quad header, Temp initial, Induction induction, Temp indvar, Induction t, POPER q, int flag, TempMap loopmap) {

	//Set up pointers for linking in nodes for providing new test constant
	Quad loopcaller=header.prev(0);
	int which_succ=header.prevEdge(0).which_succ();
	Quad successor=header;
	int which_pred=0;
	if (induction.offset!=0) {
	    //Add -bc term
	    Quad newquad;
	    Temp newtemp=new Temp(initial.tempFactory(),initial.name());
	    if (induction.objectsize!=null) {
		Temp newtempx=new Temp(initial.tempFactory(),initial.name());
		newquad=new CONST(loopcaller.getFactory(),loopcaller, newtempx, new Integer(-induction.offset), HClass.Int);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		newquad=new PAOFFSET(((LowQuadFactory)loopcaller.getFactory()),loopcaller,newtemp,induction.objectsize, newtempx);
	    }
	    else
		newquad=new CONST(loopcaller.getFactory(),loopcaller, newtemp, new Integer(-induction.offset), HClass.Int);

	    Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
	    Temp[] sources=new Temp[2];
	    sources[0]=newtemp;
	    sources[1]=initial;
	    Quad.addEdge(loopcaller, which_succ,newquad,0);
	    loopcaller=newquad; which_succ=0;
	    if (induction.objectsize==null)
		newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,Qop.IADD,newtemp2, sources);
	    else
		newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,LQop.PADD,newtemp2, sources); 
	    Quad.addEdge(loopcaller, which_succ, newquad,0);
	    loopcaller=newquad; which_succ=0;
	    Quad.addEdge(loopcaller, which_succ, successor, which_pred);
	    initial=newtemp2;
	}
	
	
	if (!induction.pointeroffset.isEmpty()) {
	    Iterator pointers=induction.pointeroffset.iterator();
	    while (pointers.hasNext()) {
		Temp term=loopmap.tempMap((Temp) pointers.next());
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		Temp[] sources=new Temp[1];
		sources[0]=term;
		Quad newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,LQop.PNEG,newtemp, sources);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		sources=new Temp[2];
		sources[0]=newtemp2;
		sources[1]=initial;
		newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,LQop.PADD,newtemp, sources);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		initial=newtemp;
	    }
	}
	

	if ((t.intmultiplier/induction.intmultiplier)!=1) {
	    if (induction.objectsize==null) {
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		Quad newquad=new CONST(loopcaller.getFactory(),loopcaller, newtemp, new Integer(t.intmultiplier/induction.intmultiplier), HClass.Int);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		Temp[] sources=new Temp[2];
		sources[0]=newtemp;
		sources[1]=initial;
		newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,Qop.IMUL,newtemp2, sources);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		initial=newtemp2;
	    } else {
		//flip the sign
		Util.assert((t.intmultiplier/induction.intmultiplier)==-1);
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Temp[] sources=new Temp[1];
		sources[0]=initial;
		Quad newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,LQop.PNEG,newtemp, sources);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		initial=newtemp;
	    }
	}

		    
	if ((t.objectsize!=null)&&(induction.objectsize==null)) {
	    //Calculate pointer if necessary
	    Temp newtemp=new Temp(initial.tempFactory(),initial.name());
	    Quad newquad=new PAOFFSET(((LowQuadFactory)loopcaller.getFactory()),loopcaller,newtemp,t.objectsize, initial);
	    Quad.addEdge(loopcaller, which_succ,newquad,0);
	    loopcaller=newquad; which_succ=0;
	    Quad.addEdge(loopcaller, which_succ, successor, which_pred);
	    initial=newtemp;
	}

	if (t.offset!=0) {
	    //add array dereference
	    if (t.objectsize==null) {
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		Temp[] sources=new Temp[2];
		Quad newquad=new CONST(loopcaller.getFactory(),loopcaller, newtemp, new Integer(t.offset), HClass.Int);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		sources[0]=newtemp;
		sources[1]=initial;
		newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,Qop.IADD,newtemp2, sources);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		initial=newtemp2;
	    } else {
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		Temp newtemp3=new Temp(initial.tempFactory(),initial.name());
		Temp[] sources=new Temp[2];
		Quad newquad=new CONST(loopcaller.getFactory(),loopcaller, newtemp, new Integer(t.offset), HClass.Int);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		newquad=new PAOFFSET(((LowQuadFactory)loopcaller.getFactory()),loopcaller,newtemp2,t.objectsize, newtemp);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		sources[0]=newtemp2;
		sources[1]=initial;
		newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,LQop.PADD,newtemp3, sources);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		initial=newtemp3; 
	    }
	}

	if (!t.pointeroffset.isEmpty()) {
	    Iterator pointers=t.pointeroffset.iterator();
	    while (pointers.hasNext()) {
		Temp term=loopmap.tempMap((Temp) pointers.next());
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Temp[] sources=new Temp[2];
		sources[0]=term;
		sources[1]=initial;
		Quad newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,LQop.PADD,newtemp, sources);
		Quad.addEdge(loopcaller, which_succ,newquad,0);
		loopcaller=newquad; which_succ=0;
		Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		initial=newtemp;
	    }
	}
	int opcode=q.opcode();
	if (t.objectsize!=null) {
	    if (q.opcode()==Qop.ICMPGT) 
		opcode=LQop.PCMPGT;
	    if (q.opcode()==Qop.ICMPEQ)
		opcode=LQop.PCMPEQ;
	}
	Temp[] sources=new Temp[2];
	if ((t.intmultiplier/induction.intmultiplier)>0) {
	    //Keep same order
	    sources[flag]=indvar;
	    sources[1-flag]=initial;
	} else {
	    //Flip order
	    sources[flag]=initial;
	    sources[1-flag]=indvar;
	}
	return new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,opcode,q.dst(), sources);
    }

    Quad doLoopind(HCode hc, Loops lp,Quad header,WorkSet usedinvariants, WorkSet complete) {
	Map basmap=bimap.basicInductionsMap(hc,lp);
	Map allmap=aimap.allInductionsMap(hc,lp);
	WorkSet basic=new WorkSet(basmap.keySet());
	Iterator iterat=allmap.keySet().iterator();
	//Copy allmap
	while(iterat.hasNext())
	    complete.add(iterat.next());
	LoopMap loopmap=new LoopMap(hc,lp,ssitossamap);

	Iterator iterate=complete.iterator();

	
	int linkin;
	Util.assert(((HasEdges)header).pred().length==2);
	//Only worry about headers with two edges
	if (lp.loopIncelements().contains(header.prev(0)))
	    linkin=1;
	else
	    linkin=0;



	while (iterate.hasNext()) {
	    Temp indvariable=(Temp) iterate.next();
	    Induction induction=(Induction) allmap.get(indvariable);
	    Temp[] headuses=header.use();
	    boolean skip=false;
	    for(int l=0;l<headuses.length;l++)
		if (ssitossamap.tempMap(headuses[l])==indvariable) {
		    skip=true;
		    break;
		}
	    if (indvariable==induction.variable)
		skip=true;

	    if (induction.pointerindex||skip) {
		iterate.remove();
	    } else {
		//Non pointer index...
		//We have a derived induction variable...
		Temp consttemp=null;
		Temp initial=initialTemp(hc,(PHI)header, induction.variable, lp.loopIncelements());
		Quad loopcaller=header.prev(linkin);
		int which_succ=header.prevEdge(linkin).which_succ();
		Quad successor=header;
		int which_pred=linkin;

		if (induction.intmultiplier!=1) {
		    //Add multiplication
		    consttemp=new Temp(initial.tempFactory(),initial.name());
		    Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		    Temp[] sources=new Temp[2];
		    sources[0]=consttemp;
		    sources[1]=initial;
		    Quad newquad=new CONST(loopcaller.getFactory(),loopcaller,consttemp, new Integer(induction.intmultiplier), HClass.Int);
		    Quad.addEdge(loopcaller, which_succ,newquad,0);
		    loopcaller=newquad; which_succ=0;
		    newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,Qop.IMUL,newtemp2, sources);
		    Quad.addEdge(loopcaller, which_succ, newquad,0);
		    loopcaller=newquad; which_succ=0;
		    Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		    initial=newtemp2;
		}
		
		if (induction.offset!=0) {
		    //Add addition
		    Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		    Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		    Temp[] sources=new Temp[2];
		    sources[0]=newtemp;
		    sources[1]=initial;
		    Quad newquad=new CONST(loopcaller.getFactory(),loopcaller,newtemp, new Integer(induction.offset), HClass.Int);
		    Quad.addEdge(loopcaller, which_succ,newquad,0);
		    loopcaller=newquad; which_succ=0;
		    newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,Qop.IADD,newtemp2, sources);
		    Quad.addEdge(loopcaller, which_succ,newquad,0);
		    loopcaller=newquad; which_succ=0;
		    Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		    initial=newtemp2;
		}

		if (induction.objectsize!=null) {
		    //add array dereference
		    Temp newtemp=new Temp(indvariable.tempFactory(),indvariable.name());
		    Quad newquad=new PAOFFSET(((LowQuadFactory)loopcaller.getFactory()),loopcaller,newtemp,induction.objectsize, initial);
		    Quad.addEdge(loopcaller, which_succ,newquad,0);
		    loopcaller=newquad; which_succ=0;
		    Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		    initial=newtemp;
		}

		if (!induction.pointeroffset.isEmpty()) {
		    Iterator pointers=induction.pointeroffset.iterator();
		    while (pointers.hasNext()) {
			Temp t=loopmap.tempMap((Temp) pointers.next());
			Temp newtemp=new Temp(indvariable.tempFactory(),indvariable.name());
			Temp[] sources=new Temp[2];
			sources[0]=t;
			sources[1]=initial;
			Quad newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,LQop.PADD,newtemp, sources);
			Quad.addEdge(loopcaller, which_succ,newquad,0);
			loopcaller=newquad; which_succ=0;
			Quad.addEdge(loopcaller, which_succ, successor, which_pred);
			initial=newtemp;
		    }
		}


		//and calculate the increment size.. [done]

		Temp increment=loopmap.tempMap(findIncrement(hc, induction.variable, lp.loopIncelements(),header));
		    //Need to do multiply...
		if (induction.intmultiplier!=1) {
		    //Add multiplication
		    Temp newtemp2=new Temp(increment.tempFactory(),increment.name());
		    Temp[] sources=new Temp[2];
		    sources[0]=consttemp;
		    sources[1]=increment;
		    Quad newquad=new POPER(((LowQuadFactory)loopcaller.getFactory()),loopcaller,Qop.IMUL,newtemp2, sources);
		    Quad.addEdge(loopcaller, which_succ,newquad,0);
		    loopcaller=newquad; which_succ=0;
		    Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		    increment=newtemp2;
		}
		
		if (induction.objectsize!=null) {
		    //Integer induction variable		    
		    //add array dereference
		    Temp newtemp=new Temp(increment.tempFactory(),increment.name());
		    Quad newquad=new PAOFFSET(((LowQuadFactory)loopcaller.getFactory()),loopcaller,newtemp,induction.objectsize, increment);
		    Quad.addEdge(loopcaller, which_succ,newquad,0);
		    loopcaller=newquad; which_succ=0;
		    Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		    increment=newtemp;
		}
		
		//delete the original definition
		HCodeElement[] sources=ud.defMap(hc,indvariable);
		Util.assert(sources.length==1);
		Quad delquad=(Quad)sources[0];

		//Mark it used....wouldn't want to try to hoist this into existance the future...
		usedinvariants.push(delquad);

		Quad.addEdge(delquad.prev(0),delquad.prevEdge(0).which_succ(), delquad.next(0), delquad.nextEdge(0).which_pred());
		//Now we need to add phi's
		Temp addresult=new Temp(initial.tempFactory(), initial.name());
		header=handlePHI((PHI) header, indvariable,initial, addresult,hc,lp);
		//and add the add operands...
		makeADD(induction, addresult,indvariable,increment,hc,lp,header);
	    }
	}
	return header;
    }



    Quad handlePHI(PHI phi, Temp indvariable, Temp initial, Temp addresult, HCode hc,Loops lp) {
	//Add phi to PHI that looks like
	//indvariable=phi(initial, addresult)
	Temp[][] newsrc=new Temp[phi.numPhis()+1][phi.arity()];
	Temp[] newdst=new Temp[phi.numPhis()+1];
	int entrance=-1;
	Util.assert(phi.arity()==2);

	for (int i=0;i<phi.arity();i++) 
	    if (!lp.loopIncelements().contains(phi.prev(i))) {
		entrance=i;
		break;
	    }
	Util.assert(entrance!=-1);
	for (int philoop=0;philoop<phi.numPhis();philoop++) {
	    newdst[philoop]=phi.dst(philoop);
	    for (int arityloop=0;arityloop<phi.arity();arityloop++) {
		newsrc[philoop][arityloop]=phi.src(philoop,arityloop);
	    }
	}
	newdst[phi.numPhis()]=indvariable;
	for (int j=0;j<phi.arity();j++) {
	    if (j==entrance) 
		newsrc[phi.numPhis()][j]=initial;
	    else
		newsrc[phi.numPhis()][j]=addresult;
	}
	PHI newphi=new PHI(phi.getFactory(), phi, newdst, newsrc,phi.arity());
	//Link our successor in
	Quad.addEdge(newphi,0,phi.next(0),phi.nextEdge(0).which_pred());
	//Link our predecessors in
	for (int k=0;k<phi.arity();k++) {
	    Quad.addEdge(phi.prev(k),phi.prevEdge(k).which_succ(),newphi,k);
	}
	return newphi;
    }

    void makeADD(Induction induction, Temp addresult, Temp indvariable, Temp increment, HCode hc, Loops lp, Quad header) {
	//Build addresult=POPER(add(indvariable, increment))
	Temp basic=induction.variable;
	Quad addposition=addQuad(hc,(PHI) header,basic,lp.loopIncelements());

	Quad newquad=null;
	Temp[] sources=new Temp[2];
	sources[0]=indvariable;
	sources[1]=increment;
	if (induction.objectsize==null) {
	    //need normal add
	    newquad=new POPER(((LowQuadFactory)addposition.getFactory()),addposition,LQop.IADD,addresult, sources);
	}
	else {
	    //need pointer add
	    newquad=new POPER(((LowQuadFactory)addposition.getFactory()),addposition,LQop.IADD,addresult, sources);
	}
	Quad prev=addposition.prev(0);
	int which_succ=addposition.prevEdge(0).which_succ();
	Quad successor=addposition;
	int which_pred=0;
	Quad.addEdge(prev, which_succ,newquad,0);
	which_succ=0;
	Quad.addEdge(newquad, which_succ, successor, which_pred);
    }
    
    /** <code>initialTemp</code> takes in a <code>Temp</code> t that needs to be a basic
     *  induction variable, and returns a <code>Temp</code> with its initial value. */
    
    Temp initialTemp(HCode hc, PHI q, Temp t, Set loopelements) {
	int j=0;
	for (;j<q.numPhis();j++) {
	    if (q.dst(j)==t) break;
	}
	Temp[] uses=q.src(j);
	Util.assert(uses.length==2);
	Temp initial=null;
	for(int i=0;i<uses.length;i++) {
	    HCodeElement[] sources=ud.defMap(hc,ssitossamap.tempMap(uses[i]));
	    Util.assert(sources.length==1);
	    if (!loopelements.contains(sources[0])) {
		initial=uses[i];
		break;
	    }
	}
	return initial;
    }

    /** <code>addQuad</code> takes in a <code>Temp</code> t that needs to be a basic
     *  induction variable, and returns the <code>Quad</code> that does the adding. */
    
    Quad addQuad(HCode hc, PHI q,Temp t, Set loopelements) {
       	int j=0;
	for (;j<q.numPhis();j++) {
	    if (q.dst(j)==t) break;
	}
	Temp[] uses=q.src(j);
	Util.assert(uses.length==2);
	Temp initial=null;
	for(int i=0;i<uses.length;i++) {
	    HCodeElement[] sources=ud.defMap(hc,ssitossamap.tempMap(uses[i]));
	    Util.assert(sources.length==1);
	    if (loopelements.contains(sources[0])) {
		initial=uses[i];
		break;
	    }
	}
	HCodeElement[] sources=ud.defMap(hc,ssitossamap.tempMap(initial));
	Util.assert(sources.length==1);
	return (Quad)sources[0];
    }

    /** <code>findIncrement</code> finds out how much the basic induction variable is
     *  incremented by.*/

    Temp findIncrement(HCode hc, Temp t, Set loopelements, Quad header) {
	Quad q=addQuad(hc,(PHI) header, t,loopelements);
	HCodeElement []source=ud.defMap(hc,ssitossamap.tempMap(t));
	Util.assert(source.length==1);
	PHI qq=(PHI)source[0];
	Temp[] uses=q.use();
	Temp result=null;

	for (int i=0;i<uses.length;i++) {
	    HCodeElement []sources=ud.defMap(hc,ssitossamap.tempMap(uses[i]));
	    Util.assert(sources.length==1);
	    if (sources[0]!=qq) {
		result=uses[i];
		break;
	    }
	}
	return result;
    }

    /** <code>doLoopinv</code> hoists loop invariants out of the loop.*/

    void doLoopinv(HCode hc, Loops lp,Quad header, WorkSet usedinvariants) {
	WorkSet invariants=new WorkSet(invmap.invariantsMap(hc, lp));
	int linkin;
	Util.assert(((HasEdges)header).pred().length==2);

	//Only worry about headers with two edges
	if (lp.loopIncelements().contains(header.prev(0)))
	    linkin=1;
	else
	    linkin=0;

	Quad loopcaller=header.prev(linkin);
	int which_succ=header.prevEdge(linkin).which_succ();
	Quad successor=header;
	int which_pred=linkin;


	while (!invariants.isEmpty()) {
	    Iterator iterate=invariants.iterator();
	    while (iterate.hasNext()) {
		Quad q=(Quad)iterate.next();
		if (usedinvariants.contains(q)) {
		    iterate.remove();
		    break;
		}
		Temp[] uses=q.use();
		boolean okay=true;
		for (int i=0;i<uses.length;i++) {
		    HCodeElement []sources=ud.defMap(hc,ssitossamap.tempMap(uses[i]));
		    Util.assert(sources.length==1);
		    if (invariants.contains(sources[0])) {
			okay=false;
			break;
		    }
		}
		if (okay) {
		    LoopMap loopmap=new LoopMap(hc,lp,ssitossamap);
		    Quad newquad=q.rename(q.getFactory(), loopmap, loopmap);
		    //we made a good quad now....
		    //Toss it  in the pile
		    Quad.addEdge(loopcaller, which_succ,newquad,0);
		    //Link the old quad away
		    Quad.addEdge(q.prev(0),q.prevEdge(0).which_succ(), q.next(0), q.nextEdge(0).which_pred());
		    usedinvariants.push(q);
      		    //Set up the next link
		    loopcaller=newquad;
		    which_succ=0;	
		    //Need to link to the loop
		    Quad.addEdge(loopcaller, which_succ, successor, which_pred);
		}
	    }
	}
    } 
}
