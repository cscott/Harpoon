// LoopOptimize.java, created Thu Jun 24 11:41:44 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.LQop;
import harpoon.IR.LowQuad.LowQuadFactory;
import harpoon.IR.LowQuad.LowQuadSSI;
import harpoon.IR.LowQuad.PAOFFSET;
import harpoon.IR.LowQuad.POPER;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Properties.CFGraphable;
import harpoon.Analysis.UseDef;
import harpoon.Analysis.Loops.Loops;
import harpoon.Analysis.LowQuad.Loop.LoopAnalysis;
import harpoon.Analysis.LowQuad.Loop.Induction;
import harpoon.Analysis.Quads.SSIToSSAMap;
import harpoon.Analysis.LowQuad.Loop.LoopMap;
import harpoon.Analysis.Maps.AllInductionsMap;
import harpoon.Analysis.Maps.BasicInductionsMap;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.InvariantsMap;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;
import harpoon.Temp.TempMap;
import harpoon.Temp.Temp;
import harpoon.Analysis.Quads.DeadCode;
import harpoon.Analysis.LowQuad.Loop.WorkTempMap;
import harpoon.Analysis.LowQuad.Loop.MyLowQuadSSI;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 * <code>LoopOptimize</code> optimizes the code after <code>LoopAnalysis</code>.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopOptimize.java,v 1.3.2.1 2002-02-27 08:31:36 cananian Exp $
 */
public final class LoopOptimize {

    AllInductionsMap aimap;
    BasicInductionsMap bimap;
    InvariantsMap invmap;
    LoopAnalysis loopanal;
    TempMap ssitossamap;
    UseDef ud;
    boolean changed;

    /** Creates an <code>LoopOptimize</code>. */
    public LoopOptimize(AllInductionsMap aimap,BasicInductionsMap bimap,InvariantsMap invmap, LoopAnalysis loopanal, TempMap ssitossamap) {
	this.aimap=aimap;
	this.bimap=bimap;
	this.invmap=invmap;
	this.loopanal=loopanal;
	this.ssitossamap=ssitossamap;
	ud=new UseDef();
	changed=false;
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
		if (hc!=null) {
		    SSIToSSAMap ssitossa=new SSIToSSAMap(hc);
		    return (new LoopOptimize(new LoopAnalysis(ssitossa),ssitossa)).optimize(hc);
		} else
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

    public HCode optimize(final HCode hc) {
    
	//Want defmap of temps in my invariants list, not new temps...
	//Force generation of defmap
	//Need to fix this!!!

	Temp []dummy=ud.allTemps(hc);	

	LowQuadSSI hcnew=new MyLowQuadSSI((LowQuadSSI)hc);

	// We start at the root loop, and recurse down each of its subloops.

	Loops lp=loopanal.rootloop(hc);
	WorkSet kids=(WorkSet) lp.nestedLoops();
	Iterator iterate=kids.iterator();
	//hcnew.print(new java.io.PrintWriter(System.out, true));
	while (iterate.hasNext())
	    recursetree(hc, (MyLowQuadSSI) hcnew, (Loops)iterate.next(), new WorkSet());

	//hcnew.print(new java.io.PrintWriter(System.out, true));
	if (changed) {
	    //We've likely broken SSI invariants...EVIL SSI!
	    //Lets fix them...
	    //As soon as someone gives me something to do that...
	    //After doing optimizations we need to clean up any deadcode...
	    


	    MyLowQuadNoSSA hctemp=new MyLowQuadNoSSA(hcnew);
	    hcnew=new LowQuadSSI(hctemp);
	    //DeadCode.optimize(hcnew, null/*throw away AllocationInformation*/);
	}
	

	//hcnew.print(new java.io.PrintWriter(System.out, true));
	return hcnew;
    }


    /** <code>recursetree</code> recurses down the nested loop tree.*/
    void recursetree(HCode hc, MyLowQuadSSI hcnew, Loops lp, WorkSet usedinvariants) {
	
	//We only treat loops with one entrance currently.  We only recognize
	//loops with one entrance, so this isn't a limitation.
	if (lp.loopEntrances().size()==1) {
	    Quad header=(Quad)(lp.loopEntrances()).toArray()[0];
	    if (header.prev().length==2) {
		//Hoist loop invariants.  The workset usedinvariants
		//keeps track of invariants we have moved, so we don't try again.

		doLoopinv(hc, hcnew, lp, header, usedinvariants);

		//Create induction variables.  Header has to be updated since
		//we most likely create new header nodes.
		Set newinds=doLoopind(hc, hcnew, lp,header, usedinvariants);
		//Move loop test conditions from the original induction variable
		//to new ones
		doLooptestmove(hc, hcnew, lp, header, newinds, usedinvariants);
	    }
	    else System.out.println("Multiple back edges.");
	} else
	    System.out.println("Multiple or No  entrance loop in LoopOptimize!");

	//Look for child loops.  If there are any, recurse to them.

	WorkSet kids=(WorkSet) lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext())
	    recursetree(hc, hcnew, (Loops)iterate.next(),usedinvariants);
    }

    /** <code>doLooptest</code> moves test conditions from original induction variables
     * to new ones whenever possible.*/

    void doLooptestmove(HCode hc, MyLowQuadSSI hcnew, Loops lp,Quad header, Set newinds, Set usedinvariants) {
	//Create the set of loop elements
	Set tests=loopanal.doLooptest(hc, lp);
	Iterator iterate=tests.iterator();

	//create sets of loop invariants and map of induction variables
	//to pass to the visitor
	Set loopinvars=invmap.invariantsMap(hc,lp);
	Map allinductions=aimap.allInductionsMap(hc,lp);

	TestMover mover=new TestMover(newinds, loopinvars, allinductions, header, ssitossamap,hc, hcnew, lp);

	//visit the nodes
	while (iterate.hasNext()) {
	    Quad q=(Quad) iterate.next();

	    //make sure that our node hasn't been used...
	    //Isn't required [should be caught by the fact
	    //that loop invariant nodes don't rely on induction variables.]
	    if (!usedinvariants.contains(q))
		mover.consider((POPER)q);
	}
	if (mover.changed())
	    changed=true;
    }


    /**<code>TestMover</code> does all the magic for changing test conditions.*/

    class TestMover {
	Set inductvars;
	Set newinductvars;
	Set loopinvars;
	Map allinductions;
	Quad header;
	TempMap ssitossamap;
	Loops lp;
	HCode hc;
	MyLowQuadSSI hcnew;
	boolean changed;

	//Create TestMover, and inform it of everything.
	TestMover(Set newinductvars, Set loopinvars, Map allinductions, Quad header, TempMap ssitossamap, HCode hc, MyLowQuadSSI hcnew, Loops lp) {
	    this.newinductvars=newinductvars;
	    this.loopinvars=loopinvars;
	    this.allinductions=allinductions;
	    this.inductvars=allinductions.keySet();
	    this.header=header;
	    this.ssitossamap=ssitossamap;
	    this.hc=hc;
	    this.hcnew=hcnew;
	    this.lp=lp;
	    changed=false;
	}

	public boolean changed() {
	    return changed;
	}

	//POPER visitor
	//Only look at ICMPEQ, and ICMPGT

	public void consider(POPER q) {
	    switch (q.opcode()) {
	    case Qop.ICMPGT:
	    case Qop.ICMPEQ:
		POPER replace=lookat(q);
		if (replace!=null) {
		    //Put the new POPER in its place
		    Quad qnew=hcnew.quadMap(q);
		    Quad.addEdge(qnew.prev(0), qnew.prevEdge(0).which_succ(), replace,0);
		    Quad.addEdge(replace, 0, qnew.next(0), qnew.nextEdge(0).which_pred());
		    changed=true;
		}
		break;
	    default:
	    }
	}

	/**<code>lookat</code> examines a test condition, to see how we should replace it.*/
	POPER lookat(POPER q) {
	    Temp[] operands=q.operands();
	    assert operands.length==2;
	    boolean good=true;
	    int flag=-1;
	    POPER newpoper=null;

	    //Loop through the operands
	    //we need one induction variable
	    //and one loop invariant

	    for (int i=0;i<operands.length;i++)
		if (inductvars.contains(ssitossamap.tempMap(operands[i])))
		    flag=i;


	    //if we found these, look for possible replacement induction variables
 
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
		
		if (t.variable()!=induction.variable()) 
		    continue;
		if (t.copied)
		    continue;

		//skipmessy cases
		if ((!induction.constant())||(!t.constant()))
		    continue;

		if ((induction.intmultiplier()!=1)&&(induction.intmultiplier()!=-1))
		    if ((t.intmultiplier()!=induction.intmultiplier())&&(t.intmultiplier()!=-induction.intmultiplier()))
			continue;
		if (induction.objectsize!=null) {
		    if ((t.objectsize!=induction.objectsize)||(t.intmultiplier()!=induction.intmultiplier()))
			continue;
		}
		    
		//Found something that passes the policy

		Temp initial=operands[1-flag];
		//Call the method to build us a new compare
		//System.out.println("Compare statement "+q);
		//System.out.println("induction="+induction+"  t="+t);
		newpoper=movecompare(hcnew, header, initial, induction, indvar,t,q, flag, new LoopMap(hc,lp,ssitossamap));
		break;
	    }
	    
	    return newpoper;
	}
    }

    /** <code>movecompare</code> builds a new compare statement.  The new compared 
     *  statement uses the derived induction variable t instead of the
     *  original induction.*/

    POPER movecompare(MyLowQuadSSI hcnew, Quad oheader, Temp oinitial, Induction induction, Temp oindvar, Induction t, POPER oq, int flag, TempMap loopmap) {

	//Set up pointers for linking in nodes for providing new test constant
	Quad header=hcnew.quadMap(oheader);
	Temp initial=hcnew.tempMap(loopmap.tempMap(oinitial));
	Temp indvar=hcnew.tempMap(oindvar);
	POPER q=(POPER)hcnew.quadMap(oq);

	QuadInserter addquad=new QuadInserter(header.prev(0), header.prevEdge(0).which_succ(),header, 0);

	if (induction.offset()!=0) {
	    //Add -bc term
	    Quad newquad;
	    Temp newtemp=new Temp(initial.tempFactory(),initial.name());
	    if (induction.objectsize!=null) {
		Temp newtempx=new Temp(initial.tempFactory(),initial.name());
		newquad=new CONST(header.getFactory(),header, newtempx, 
				  new Integer(-induction.offset()), HClass.Int);
		hcnew.addType(newtempx, HClass.Int);
		addquad.insert(newquad);
		newquad=new PAOFFSET(((LowQuadFactory)header.getFactory()),header,
				     newtemp,induction.objectsize, newtempx);
		hcnew.addType(newtemp, HClass.Int);
	    }
	    else {
		newquad=new CONST(header.getFactory(),header, newtemp, 
				  new Integer(-induction.offset()), HClass.Int);
		hcnew.addType(newtemp, HClass.Int);
	    }

	    Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
	    Temp[] sources=new Temp[2];
	    sources[0]=newtemp;
	    sources[1]=initial;
	    addquad.insert(newquad);

	    if (induction.objectsize==null) {
		newquad=new POPER(((LowQuadFactory)header.getFactory()),header,
				  Qop.IADD,newtemp2, sources);
		hcnew.addType(newtemp2, HClass.Int);
	    }
	    else {
		newquad=new POPER(((LowQuadFactory)header.getFactory()),header,
				  LQop.PADD,newtemp2, sources);
		if (hcnew.derivation(header, initial)==null)
		    hcnew.addType(newtemp2, hcnew.typeMap(null,initial));
		else
		    hcnew.addType(newtemp2, 
				  null);
	    }
	    hcnew.addDerivation(newtemp2, 
				Derivation.DList.clone(hcnew.derivation(newquad, initial)));
	    addquad.insert(newquad);
	    initial=newtemp2;
	}
	
	
	if (!induction.pointeroffset.isEmpty()) {
	    Iterator pointers=induction.pointeroffset.iterator();
	    while (pointers.hasNext()) {
		Object[] ptrobject=(Object[])pointers.next();
		boolean positive=((Boolean)ptrobject[1]).booleanValue();
		Temp term=hcnew.tempMap(loopmap.tempMap((Temp) ptrobject[0]));
		Temp[] sources=new Temp[1];
		Temp newtemp=null;
		if (positive) {
		    sources[0]=term;
		    newtemp=new Temp(initial.tempFactory(),initial.name());
		    if (hcnew.derivation(header, term)==null)
			hcnew.addType(newtemp, hcnew.typeMap(null, term));
		    else
			hcnew.addType(newtemp, 
				      null);
		    Quad newquad=new POPER(((LowQuadFactory)header.getFactory()),
					   header,LQop.PNEG,newtemp, sources);
		    hcnew.addDerivation(newtemp, negate(hcnew.derivation(header, term)));
		    addquad.insert(newquad);
		}   else {
		    newtemp=term;
		}

		Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		sources=new Temp[2];
		sources[0]=newtemp;
		sources[1]=initial;
		Quad newquad=new POPER(((LowQuadFactory)header.getFactory()),header,LQop.PADD,newtemp2, sources);
		Derivation.DList merged=merge(hcnew.derivation(newquad,sources[0]),hcnew.derivation(newquad,sources[1]));
		if (merged==null)
		    hcnew.addType(newtemp2,HClass.Int);
		else
		    hcnew.addType(newtemp2, 
				  null);
		hcnew.addDerivation(newtemp2, merged);
		addquad.insert(newquad);
		initial=newtemp2;
	    }
	}
	
	if ((t.intmultiplier()/induction.intmultiplier())!=1) {
	    if (induction.objectsize==null) {
		//We have an integer
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Quad newquad=new CONST(header.getFactory(),header, newtemp,
				       new Integer(t.intmultiplier()/induction.intmultiplier()),
				       HClass.Int);
		hcnew.addType(newtemp, HClass.Int);
		addquad.insert(newquad);
		Temp[] sources=new Temp[2];
		sources[0]=newtemp;
		sources[1]=initial;		
		Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		newquad=new POPER(((LowQuadFactory)header.getFactory()),header,
				  Qop.IMUL,newtemp2, sources);
		hcnew.addType(newtemp2, HClass.Int);
		addquad.insert(newquad);
		initial=newtemp2;
	    } else {
		//flip the sign
		//the ratio has to be =-1
		assert (t.intmultiplier()/induction.intmultiplier())==-1;
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Temp[] sources=new Temp[1];
		sources[0]=initial;
		Quad newquad=new POPER(((LowQuadFactory)header.getFactory()),header,
				       LQop.PNEG,newtemp, sources);
		if (hcnew.derivation(newquad, initial)==null)
		    hcnew.addType(newtemp, hcnew.typeMap(null, initial));
		else
		    hcnew.addType(newtemp, 
				  null);
		hcnew.addDerivation(newtemp, negate(hcnew.derivation(newquad, initial)));
		addquad.insert(newquad);
		initial=newtemp;
	    }
	}

		    
	if ((t.objectsize!=null)&&(induction.objectsize==null)) {
	    //Calculate pointer if necessary
	    Temp newtemp=new Temp(initial.tempFactory(),initial.name());
	    Quad newquad=new PAOFFSET(((LowQuadFactory)header.getFactory()),header,
				      newtemp,t.objectsize, initial);
	    hcnew.addType(newtemp, HClass.Int);
	    addquad.insert(newquad);
	    initial=newtemp;
	}

	if (t.offset()!=0) {
	    if (t.objectsize==null) {
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Quad newquad=new CONST(header.getFactory(),header, newtemp, 
				       new Integer(t.offset()), HClass.Int);
		hcnew.addType(newtemp, HClass.Int);
		addquad.insert(newquad);
		Temp[] sources=new Temp[2];
		sources[0]=newtemp;
		sources[1]=initial;
		Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		hcnew.addType(newtemp2, HClass.Int);
		newquad=new POPER(((LowQuadFactory)header.getFactory()),header,
				  Qop.IADD,newtemp2, sources);
		addquad.insert(newquad);
		initial=newtemp2;
	    } else {
		//ADD Constant oper
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		hcnew.addType(newtemp, HClass.Int);
		Quad newquad=new CONST(header.getFactory(),header, newtemp,
				       new Integer(t.offset()), HClass.Int);
		addquad.insert(newquad);

		//ADD PAOFFSET oper
		Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
		hcnew.addType(newtemp2, HClass.Int);
		newquad=new PAOFFSET(((LowQuadFactory)header.getFactory()),header,
				     newtemp2,t.objectsize, newtemp);
		addquad.insert(newquad);

		//Add PADD oper
		Temp[] sources=new Temp[2];
		sources[0]=newtemp2;
		sources[1]=initial;
		Temp newtemp3=new Temp(initial.tempFactory(),initial.name());
		if (hcnew.derivation(newquad, newtemp3)==null)
		    hcnew.addType(newtemp3, hcnew.typeMap(null,initial));
		else
		    hcnew.addType(newtemp3,
				  null);
		newquad=new POPER(((LowQuadFactory)header.getFactory()),header,LQop.PADD,newtemp3, sources);
		hcnew.addDerivation(newtemp3, hcnew.derivation(newquad, newtemp3));
		addquad.insert(newquad);
		initial=newtemp3; 
	    }
	}

	if (!t.pointeroffset.isEmpty()) {
	    Iterator pointers=t.pointeroffset.iterator();
	    while (pointers.hasNext()) {
		Object[] ptrobject=(Object[])pointers.next();
		boolean positive=((Boolean)ptrobject[1]).booleanValue();
		Temp term=hcnew.tempMap(loopmap.tempMap((Temp) ptrobject[0]));
		Temp[] sources=new Temp[2];

		if (!positive) {
		    //have to flip sign
		    sources[0]=term;
		    term=new Temp(initial.tempFactory(),initial.name());
		    if (hcnew.derivation(header, term)==null)
			hcnew.addType(term, hcnew.typeMap(null, sources[0]));
		    else
			hcnew.addType(term, 
				      null);
		    Quad newquad=new POPER(((LowQuadFactory)header.getFactory()),
					   header,LQop.PNEG,term, sources);
		    hcnew.addDerivation(term, negate(hcnew.derivation(header, term)));
		    addquad.insert(newquad);
		}

		sources[0]=term;
		sources[1]=initial;
		Temp newtemp=new Temp(initial.tempFactory(),initial.name());
		Quad newquad=new POPER(((LowQuadFactory)header.getFactory()),
				       header,LQop.PADD,newtemp, sources);

		//Make new derivation list
		Derivation.DList merged=merge(hcnew.derivation(newquad,sources[0]),
					      hcnew.derivation(newquad,sources[1]));
		//Update type info
		if (merged==null)
		    hcnew.addType(newtemp,HClass.Int);
		else
		    hcnew.addType(newtemp, 
				  null);
		//Update derivation info
		hcnew.addDerivation(newtemp, merged);
		addquad.insert(newquad);
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
	if ((t.intmultiplier()/induction.intmultiplier())>0) {
	    //Keep same order
	    sources[flag]=indvar;
	    sources[1-flag]=initial;
	} else {
	    //Flip order
	    sources[flag]=initial;
	    sources[1-flag]=indvar;
	}
	if (addquad.changed())
	    changed=true;
	return new POPER(((LowQuadFactory)header.getFactory()),header,opcode,q.dst(), sources);
    }


    Set doLoopind(HCode hc, MyLowQuadSSI hcnew, Loops lp,Quad oheader,WorkSet usedinvariants) {
	Quad header=hcnew.quadMap(oheader);

	Map basmap=bimap.basicInductionsMap(hc,lp);
	Map allmap=aimap.allInductionsMap(hc,lp);
	WorkSet basic=new WorkSet(basmap.keySet());
	WorkSet complete=new WorkSet(allmap.keySet());

	//Copy allmap
	LoopMap loopmap=new LoopMap(hc,lp,ssitossamap);
	Iterator iterate=complete.iterator();

      	int linkin;
	assert ((CFGraphable)header).pred().length==2;
	//Only worry about headers with two edges
	if (lp.loopIncElements().contains(header.prev(0)))
	    linkin=1;
	else
	    linkin=0;


	while (iterate.hasNext()) {
	    Temp indvariable=(Temp) iterate.next();
	    Induction induction=(Induction) allmap.get(indvariable);
	    Temp[] headuses=oheader.use();
	    boolean skip=false;

	    //Check to see if this induction is just the next iteration of the basic
	    //induction variable...if so, skip it!
	    for(int l=0;l<headuses.length;l++)
		if (ssitossamap.tempMap(headuses[l])==indvariable) {
		    skip=true;
		    break;
		}
	    if (indvariable==induction.variable())
		skip=true;

	    if (induction.pointerindex||skip) {
		//Don't deal with induction variables that are already pointers,
		iterate.remove();
	    } else {
		//Non pointer index...
		//We have a derived induction variable...

		//Create consttemp with larger scope, so we can use the const Quad twice
		Temp[] consttemp=new Temp[induction.depth()];

		//Use old header here...
		Temp initial=loopanal.initialTemp(hc, induction.variable(), lp);
		initial=hcnew.tempMap(initial);

		QuadInserter addquad=new QuadInserter(header.prev(linkin), header.prevEdge(linkin).which_succ(), header, linkin);

		int count=0;
		for (Induction.IntMultAdd integers=induction.bottom();integers!=null;integers=integers.parent()) {
		    if (integers.intmultiplier()!=1) {
			//Add integer multiplication
			//Create constant temp with integer
			consttemp[count]=new Temp(initial.tempFactory(),initial.name());
			hcnew.addType(consttemp[count], HClass.Int);
			Quad newquad=new CONST(header.getFactory(),header, consttemp[count], 
					       new Integer(integers.intmultiplier()), HClass.Int);
			addquad.insert(newquad);

			Temp[] sources=new Temp[2];
			sources[0]=consttemp[count];
			sources[1]=initial;
			Temp newtemp=new Temp(initial.tempFactory(),initial.name());
			newquad=new POPER(((LowQuadFactory)header.getFactory()),header,Qop.IMUL,
					  newtemp, sources);
			hcnew.addType(newtemp, HClass.Int);
			addquad.insert(newquad);
			initial=newtemp;
			count++;
		    }
		
		    if (integers.offset()!=0) {
			//Add constant integer offset in
			Temp newtemp=new Temp(initial.tempFactory(),initial.name());
			hcnew.addType(newtemp, HClass.Int);
			Quad newquad=new CONST(header.getFactory(),header,newtemp, 
					       new Integer(integers.offset()), HClass.Int);
			addquad.insert(newquad);
			
			//Add in IADD oper
			Temp[] sources=new Temp[2];
			sources[0]=newtemp;
			sources[1]=initial;
			Temp newtemp2=new Temp(initial.tempFactory(),initial.name());
			newquad=new POPER(((LowQuadFactory)header.getFactory()),header,Qop.IADD,newtemp2, sources);
			hcnew.addType(newtemp2, hcnew.typeMap(null, initial));
			addquad.insert(newquad);
			initial=newtemp2;
		    }
		    if (integers.loopinvariant()!=null) {
			Temp operand=integers.loopinvariant();
			if (!integers.invariantsign()) {
			    Temp[] sources=new Temp[1];
			    sources[0]=hcnew.tempMap(loopmap.tempMap(operand));
			    Temp newtemp=new Temp(initial.tempFactory(), initial.name());
			    Quad newquad=new POPER(((LowQuadFactory)header.getFactory()), header, LQop.PNEG, newtemp, sources);
			    hcnew.addType(newtemp, HClass.Int);
			    addquad.insert(newquad);
			    operand=newtemp;
			}
			Quad newquad=null;
			Temp[] sources=new Temp[2];
		        sources[0]=initial;
			sources[1]=operand;
			Temp newtemp=new Temp(initial.tempFactory(), initial.name());
			if (integers.multiply())
			    newquad=new POPER(((LowQuadFactory)header.getFactory()), header, LQop.IMUL, newtemp, sources);
			else
			   newquad=new POPER(((LowQuadFactory)header.getFactory()), header, LQop.IADD, newtemp, sources); 
			hcnew.addType(newtemp, hcnew.typeMap(null, initial));
			addquad.insert(newquad);
			initial=newtemp;
		    }
		}


		if (induction.objectsize!=null) {
		    //add array dereference
		    Temp newtemp=new Temp(initial.tempFactory(),indvariable.name());
		    Quad newquad=new PAOFFSET(((LowQuadFactory)header.getFactory()),header,newtemp,induction.objectsize, initial);
		    hcnew.addType(newtemp, HClass.Int);
		    addquad.insert(newquad);
		    initial=newtemp;
		}

		if (!induction.pointeroffset.isEmpty()) {
		    Iterator pointers=induction.pointeroffset.iterator();
		    while (pointers.hasNext()) {
			Object[] ptrobject=(Object []) pointers.next();
			boolean positive=((Boolean)ptrobject[1]).booleanValue();
			Temp term=hcnew.tempMap(loopmap.tempMap((Temp) ptrobject[0]));
			Temp[] sources=new Temp[1];
			Temp newtemp=null;
			if (!positive) {
			    sources[0]=term;
			    newtemp=new Temp(initial.tempFactory(),initial.name());
			    if (hcnew.derivation(header, term)==null)
				hcnew.addType(newtemp, hcnew.typeMap(null, term));
			    else
				hcnew.addType(newtemp, 
					      null);
			    Quad newquad=new POPER(((LowQuadFactory)header.getFactory()),
						   header,LQop.PNEG,newtemp, sources);
			    hcnew.addDerivation(newtemp, negate(hcnew.derivation(header, term)));
			    addquad.insert(newquad);
			}   else {
			    newtemp=term;
			}
			sources=new Temp[2];
			sources[0]=newtemp;
			sources[1]=initial;
			newtemp=new Temp(initial.tempFactory(),indvariable.name());
			Quad newquad=new POPER(((LowQuadFactory)header.getFactory()),header,LQop.PADD,newtemp, sources);

			Derivation.DList merged=merge(hcnew.derivation(newquad,sources[0]),hcnew.derivation(newquad,sources[1]));
			if (merged==null)
			    hcnew.addType(newtemp,HClass.Int);
			else
			    hcnew.addType(newtemp, 
					  null);
			//System.out.println("Creating derivation: "+newtemp+" : "+merged);
			hcnew.addDerivation(newtemp, merged);
			addquad.insert(newquad);
			initial=newtemp;
		    }
		}


		//and calculate the increment size.. [done]

		Temp increment=hcnew.tempMap(loopmap.tempMap(loopanal.findIncrement(hc, induction.variable(), lp)));


		    //Need to do multiply...

		count=0;
		for (Induction.IntMultAdd integers=induction.bottom();integers!=null;integers=integers.parent()) {
		    if (integers.intmultiplier()!=1) {
			//Add multiplication
			Temp[] sources=new Temp[2];
			sources[0]=consttemp[count++];
			sources[1]=increment;
			Temp newtemp=new Temp(increment.tempFactory(),increment.name());
			Quad newquad=new POPER(((LowQuadFactory)header.getFactory()),header,Qop.IMUL,newtemp, sources);
			hcnew.addType(newtemp,HClass.Int);
			addquad.insert(newquad);
			increment=newtemp;
		    }

		    if ((integers.loopinvariant()!=null)&&(integers.multiply())) {
			Temp operand=integers.loopinvariant();
			if (!integers.invariantsign()) {
			    Temp[] sources=new Temp[1];
			    sources[0]=operand;
			    Temp newtemp=new Temp(initial.tempFactory(), initial.name());
			    Quad newquad=new POPER(((LowQuadFactory)header.getFactory()), header, LQop.PNEG, newtemp, sources);
			    hcnew.addType(newtemp, HClass.Int);
			    addquad.insert(newquad);
			    operand=newtemp;
			}
			Quad newquad=null;
			Temp[] sources=new Temp[2];
		        sources[0]=initial;
			sources[1]=operand;
			Temp newtemp=new Temp(initial.tempFactory(), initial.name());
			if (integers.multiply())
			    newquad=new POPER(((LowQuadFactory)header.getFactory()), header, LQop.IMUL, newtemp, sources);
			else
			    newquad=new POPER(((LowQuadFactory)header.getFactory()), header, LQop.IADD, newtemp, sources); 
			hcnew.addType(newtemp, hcnew.typeMap(null, initial));
			addquad.insert(newquad);
			initial=newtemp;
		    }
		}



		
		if (induction.objectsize!=null) {
		    //Integer induction variable		    
		    //add array dereference
		    Temp newtemp=new Temp(increment.tempFactory(),increment.name());
		    Quad newquad=new PAOFFSET(((LowQuadFactory)header.getFactory()),header,newtemp,induction.objectsize, increment);
		    hcnew.addType(newtemp, HClass.Int);
		    addquad.insert(newquad);
		    increment=newtemp;
		}
		
		//delete the original definition
		HCodeElement[] sources=ud.defMap(hc,indvariable);
		assert sources.length==1;
		Quad delquad=(Quad)sources[0];

		//Mark it used....wouldn't want to try to hoist this into existance in the future...
		usedinvariants.push(delquad);

		//Get the right quad
		delquad=hcnew.quadMap(delquad);
		Quad.addEdge(delquad.prev(0),delquad.prevEdge(0).which_succ(), delquad.next(0), delquad.nextEdge(0).which_pred());
		changed=true;

		//Now we need to add phi's
		Temp addresult=new Temp(initial.tempFactory(), initial.name());
      		header=handlePHI((PHI) oheader, indvariable,initial, addresult,hc, hcnew, lp);
		hcnew.addQuadMapping(oheader,header);
		//and add the add operands...
		makeADD(induction, addresult, indvariable,increment,hc, hcnew, lp,oheader);

		Derivation.DList merged=merge(hcnew.derivation(header,indvariable),hcnew.derivation(header,initial));
		if (merged==null)
		    hcnew.addType(addresult,HClass.Int);
		else
		    hcnew.addType(addresult, 
				  null);
		//System.out.println("Creating derivation2: "+addresult+" : "+merged);
		hcnew.addDerivation(addresult, merged);

		//Fix derivation out of phi function...just need to choose one of the branches and we are good...
		//System.out.println("Creating derivationX: "+hcnew.tempMap(indvariable)+" : "+merged);

		hcnew.addDerivation(hcnew.tempMap(indvariable), Derivation.DList.clone(merged));
		if (addquad.changed())
		    changed=true;
	    }
	}
	return complete;
    }


    class QuadInserter {
	Quad loopcaller;
	int which_succ;
	Quad successor;
	int which_pred;
	boolean changed;

	QuadInserter(Quad loopcaller, int which_succ, Quad successor, int which_pred) {
	    this.loopcaller=loopcaller;
	    this.which_succ=which_succ;
	    this.successor=successor;
	    this.which_pred=which_pred;
	    changed=false;
	}
	public boolean changed() {
	    return changed;
	}

	void insert(Quad newquad) {
	    Quad.addEdge(loopcaller, which_succ,newquad,0);
	    loopcaller=newquad; which_succ=0;
	    Quad.addEdge(loopcaller, which_succ, successor, which_pred);	
	    changed=true;
	}
    }


    Quad handlePHI(PHI phi, Temp indvariable, Temp initial, Temp addresult, HCode hc, MyLowQuadSSI hcnew, Loops lp) {
	//Add phi to PHI that looks like
	//indvariable=phi(initial, addresult)
	PHI phin=(PHI) hcnew.quadMap(phi);
	Temp[][] newsrc=new Temp[phin.numPhis()+1][phin.arity()];
	Temp[] newdst=new Temp[phin.numPhis()+1];
	int entrance=-1;
	assert phin.arity()==2;

	for (int i=0;i<phin.arity();i++) 
	    //want old phi here
	    if (!lp.loopIncElements().contains(phi.prev(i))) {
		entrance=i;
		break;
	    }
	assert entrance!=-1;


	for (int philoop=0;philoop<phin.numPhis();philoop++) {
	    newdst[philoop]=phin.dst(philoop);
	    for (int arityloop=0;arityloop<phin.arity();arityloop++) {
		newsrc[philoop][arityloop]=phin.src(philoop,arityloop);
	    }
	}

	newdst[phin.numPhis()]=hcnew.tempMap(indvariable);
	for (int j=0;j<phin.arity();j++) {
	    if (j==entrance) 
		newsrc[phin.numPhis()][j]=initial;
	    else
		newsrc[phin.numPhis()][j]=addresult;
	}
	PHI newphi=new PHI(phin.getFactory(), phin, newdst, newsrc,phin.arity());
	//Link our successor in
	Quad.addEdge(newphi,0,phin.next(0),phin.nextEdge(0).which_pred());
	//Link our predecessors in
	for (int k=0;k<phi.arity();k++) {
	    Quad.addEdge(phin.prev(k),phin.prevEdge(k).which_succ(),newphi,k);
	}
	changed=true;
	return newphi;
    }

    void makeADD(Induction induction, Temp addresult, Temp indvariable, Temp increment, HCode hc, MyLowQuadSSI hcnew, Loops lp, Quad oheader) {
	//Build addresult=POPER(add(indvariable, increment))
	Temp basic=induction.variable();
	Quad addposition=hcnew.quadMap(loopanal.addQuad(hc,(PHI) oheader,basic,lp.loopIncElements()));

	Quad newquad=null;
	Temp[] sources=new Temp[2];
	sources[0]=hcnew.tempMap(indvariable);
	sources[1]=increment;

	if (induction.objectsize==null) {
	    //need normal add
	    newquad=new POPER(((LowQuadFactory)addposition.getFactory()),addposition,LQop.IADD,addresult, sources);
	}
	else {
	    //need pointer add
	    newquad=new POPER(((LowQuadFactory)addposition.getFactory()),addposition,LQop.IADD,addresult, sources);
	}

	//link in new add
	Quad prev=addposition.prev(0);
	int which_succ=addposition.prevEdge(0).which_succ();
	Quad successor=addposition;
	int which_pred=0;
	Quad.addEdge(prev, which_succ,newquad,0);
	which_succ=0;
	Quad.addEdge(newquad, which_succ, successor, which_pred);
	changed=true;
    }





    //**********************************
    /** <code>doLoopinv</code> hoists loop invariants out of the loop.*/

    void doLoopinv(HCode hc, MyLowQuadSSI hcnew, Loops lp,Quad oheader, WorkSet usedinvariants) {
	WorkSet invariants=new WorkSet(invmap.invariantsMap(hc, lp));
	int linkin;
	Quad header=hcnew.quadMap(oheader);

	assert ((CFGraphable)header).pred().length==2;
	//Only worry about headers with two edges
	if (lp.loopIncElements().contains(header.prev(0)))
	    linkin=1;
	else
	    linkin=0;

	QuadInserter addquad=new QuadInserter(header.prev(linkin),header.prevEdge(linkin).which_succ(),header,linkin);


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
		    assert sources.length==1;
		    if (invariants.contains(sources[0])) {
			okay=false;
			break;
		    }
		}
		if (okay) {
		    LoopMap loopmap=new LoopMap(hc,lp,ssitossamap);
		    WorkTempMap worktmp=new WorkTempMap();
		    Quad newq=hcnew.quadMap(q);
		    for (int i=0;i<uses.length;i++) {
			worktmp.associate(hcnew.tempMap(uses[i]),hcnew.tempMap(loopmap.tempMap(uses[i])));
		    }
		    Temp[] def=newq.def();
		    for (int i=0;i<def.length;i++) {
			worktmp.associate(def[i],def[i]);
			Derivation.DList parents=((harpoon.IR.LowQuad.Code)hc).getDerivation().derivation(q, q.def()[i]);
			//Have to rewrite derivation since we moved quad...SSI..
			if (parents!=null) {
			    Derivation.DList nderiv=Derivation.DList.rename(parents, loopmap);
			    nderiv=Derivation.DList.rename(nderiv, hcnew.tempMap);
			    //System.out.println("Rewriting "+q.def()[i]+": "+parents+"->"+nderiv);
			    hcnew.addDerivation(hcnew.tempMap(q.def()[i]),nderiv);
			}
		    }
		    Quad newquad=newq.rename(newq.getFactory(), worktmp, worktmp);
		    //we made a good quad now....
		    //Toss it  in the pile
		    addquad.insert(newquad);
		    //Link the old quad away
		    Quad.addEdge(newq.prev(0),newq.prevEdge(0).which_succ(), newq.next(0), newq.nextEdge(0).which_pred());
		    usedinvariants.push(q);
		    iterate.remove();
		    changed=true;
		}
	    }
	}
	if (addquad.changed())
	    changed=true;
    }

    private Derivation.DList negate(Derivation.DList dlist) {
	if (dlist==null) return null;
	else
	  return new Derivation.DList(dlist.base,
			   !dlist.sign,
			   negate(dlist.next));
    }

    private Derivation.DList merge(Derivation.DList dlist1, Derivation.DList dlist2) {
	if (dlist1==null)
	    return dlist2.clone(dlist2);
	else 
	    return new Derivation.DList(dlist1.base, dlist1.sign, merge(dlist1.next,dlist2));
    }
}
