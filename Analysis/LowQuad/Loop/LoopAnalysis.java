// LoopAnalysis.java, created Thu Jun 24 11:45:07 1998 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.UseDef;
import harpoon.Analysis.Loops.LoopFinder;
import harpoon.Util.WorkSet;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.IR.LowQuad.*;
import harpoon.Analysis.Loops.*;
import harpoon.Temp.Temp;
import harpoon.Analysis.SSITOSSAMap;
import harpoon.Temp.TempMap;
import harpoon.IR.Properties.HasEdges;
import harpoon.Analysis.LowQuad.Loop.AllInductions;
import harpoon.Analysis.LowQuad.Loop.BasicInductions;
import harpoon.Analysis.LowQuad.Loop.LoopInvariance;
import harpoon.Analysis.Maps.AllInductionsMap;
import harpoon.Analysis.Maps.BasicInductionsMap;
import harpoon.Analysis.Maps.InvariantsMap;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

/**
 * <code>LoopAnalysis</code> implements <code>AllInductionsMap</code>,
 * <code>BasicInductionsMap</code>, and <code>InvariantsMap</code>.
 * 
 * @author  Brian Demsky
 * @version $Id: LoopAnalysis.java,v 1.1.2.14 1999-09-23 19:01:51 bdemsky Exp $
 */

public class LoopAnalysis implements AllInductionsMap, BasicInductionsMap, InvariantsMap {

    HCode lasthc;
    TempMap tm;
    HashMap aimap, bimap, invmap;
    LoopFinder rtloop;
    UseDef ud;

    /** Creates a <code>LoopAnalysis</code>.  Takes in a TempMap
     that for SSI forms needs to map SSI->SSA.*/
    public LoopAnalysis(TempMap tm) {
	this.tm=tm;
	this.ud=new UseDef();
    }

    /*-----------------------------*/
    // Class state.

    /*---------------------------*/
    // public information accessor methods.

    public Loops rootloop(HCode hc) {
	analyze(hc);
	return rtloop;
    }

    public Map allInductionsMap(HCode hc, Loops lp) {
	analyze(hc);
	return (HashMap) aimap.get(lp.loopEntrances().toArray()[0]);
    }

    public Map basicInductionsMap(HCode hc, Loops lp) {
	analyze(hc);
	return (HashMap) bimap.get(lp.loopEntrances().toArray()[0]);
    }

    public Set invariantsMap(HCode hc, Loops lp) {
	analyze(hc);
	return (Set) invmap.get(lp.loopEntrances().toArray()[0]);
    }


    /** <code>initialTemp</code> takes in a <code>Temp</code> t that needs to be a basic
     *  induction variable, a <code>Loops</code> that is the loop that t is an
     * induction variable in and returns a <code>Temp</code> with its initial value. */
    
    Temp initialTemp(HCode hc, Temp t, Loops lp) {
	Set loopelements=lp.loopIncelements();
	Util.assert(lp.loopEntrances().size()==1,"Loop must have one entrance");
	PHI q=(PHI)(lp.loopEntrances()).toArray()[0];

	int j=0;
	for (;j<q.numPhis();j++) {
	    if (q.dst(j)==t) break;
	}
	Temp[] uses=q.src(j);
	Util.assert(uses.length==2);
	Temp initial=null;
	for(int i=0;i<uses.length;i++) {
	    HCodeElement[] sources=ud.defMap(hc,tm.tempMap(uses[i]));
	    Util.assert(sources.length==1);
	    if (!loopelements.contains(sources[0])) {
		initial=uses[i];
		break;
	    }
	}
	return initial;
    }

    /** <code>findIncrement</code> finds out how much the basic induction variable is
     *  incremented by.*/

    Temp findIncrement(HCode hc, Temp t, Loops lp) {
	Set loopelements=lp.loopIncelements();
	Util.assert(lp.loopEntrances().size()==1,"Loop must have one entrance");
	PHI oheader=(PHI)(lp.loopEntrances()).toArray()[0];
	Quad q=addQuad(hc, oheader, t,loopelements);
	HCodeElement []source=ud.defMap(hc,tm.tempMap(t));
	Util.assert(source.length==1);
	PHI qq=(PHI)source[0];
	Temp[] uses=q.use();
	Temp result=null;

	for (int i=0;i<uses.length;i++) {
	    HCodeElement []sources=ud.defMap(hc,tm.tempMap(uses[i]));
	    Util.assert(sources.length==1);
	    if (sources[0]!=qq) {
		result=uses[i];
		break;
	    }
	}
	return result;
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
	    HCodeElement[] sources=ud.defMap(hc,tm.tempMap(uses[i]));
	    Util.assert(sources.length==1);
	    if (loopelements.contains(sources[0])) {
		initial=uses[i];
		break;
	    }
	}
	HCodeElement[] sources=ud.defMap(hc,tm.tempMap(initial));
	Util.assert(sources.length==1);
	return (Quad)sources[0];
    }

    /*---------------------------*/
    // Analysis code.

    /** Set of analyzed methods. */

    /** Main analysis method. */
    void analyze(HCode hc) {
	if (lasthc!=hc) {
	    invmap=new HashMap();
	    aimap=new HashMap();
	    bimap=new HashMap();
	    lasthc=hc;
	    rtloop=new LoopFinder(hc);
	    analyzetree(hc,rtloop,"");
	}
    } // end analysis.
    
    void analyzetree(HCode hc, Loops lp, String st) {
	WorkSet kids=(WorkSet)lp.nestedLoops();
	Iterator iterate=kids.iterator();

	while (iterate.hasNext()) {
	    analyzetree(hc, (Loops)iterate.next(),st+" ");
	}

	//Find loop invariants
	WorkSet elements=(WorkSet)lp.loopIncelements();
	LoopInvariance invar=new LoopInvariance(tm,hc);
	WorkSet invariants=invar.invariants(elements);

	//Find basic induction variables

	BasicInductions binductor=new BasicInductions(tm,hc);
	HashMap basicinductions=binductor.doInduction(lp,invariants);


	//Find all induction variables
	AllInductions ainductor=new AllInductions(tm,hc);
	HashMap allInductions=ainductor.doAllInductions(lp, invariants, basicinductions);

	//Add to our maps
	aimap.put(lp.loopEntrances().toArray()[0], allInductions);
	bimap.put(lp.loopEntrances().toArray()[0], basicinductions);
	invmap.put(lp.loopEntrances().toArray()[0], invariants);


	//Show the user everything
	//iterate=invariants.iterator();
	//System.out.println(st+"Invariants:");
	//while (iterate.hasNext()) {
	//    System.out.println(st+((Quad)iterate.next()).toString());
	//}
	//iterate=elements.iterator();
	//System.out.println(st+"Noninvariants:");
	//while (iterate.hasNext()) {
	//    System.out.println(st+((Quad)iterate.next()).toString());
	//}
	//iterate=(basicinductions.keySet()).iterator();

	//System.out.println(st+"Basic induction variables:");
	//while (iterate.hasNext()) {
	//    Temp tmp=(Temp) iterate.next();
	//    System.out.println(st+tmp.toString());
	//    System.out.println(st+((Induction)basicinductions.get(tmp)).toString());
	//}
	//iterate=(allInductions.keySet()).iterator();

	//System.out.println(st+"All induction variables:");
	//while (iterate.hasNext()) {
	//    Temp tmp=(Temp) iterate.next();
	//    System.out.println(st+tmp.toString());
	//    System.out.println(st+((Induction)allInductions.get(tmp)).toString());
	//}
    }

    /** <code>doLooptest</code> moves test conditions from original induction variables
     * to new ones whenever possible.*/

    Set doLooptest(HCode hc, Loops lp) {
	//Make sure we have done analysis
	analyze(hc);
	//Create the set of loop elements
	Set elements=lp.loopIncelements();
	WorkSet tests=new WorkSet();
	//Iterate through this set
	Iterator iterate=elements.iterator();

	//create sets of loop invariants and map of induction variables
	//to pass to the visitor
	Set loopinvars=(Set)invmap.get(lp.loopEntrances().toArray()[0]);
	Map allinductions=(Map)aimap.get(lp.loopEntrances().toArray()[0]);

	TestVisitor visitor=new TestVisitor(loopinvars, allinductions, tm,hc, lp, tests);
	//visit the nodes
	while (iterate.hasNext()) {
	    Quad q=(Quad) iterate.next();
	    q.accept(visitor);
	}
	return tests;
    }


    /**<code>TestVisitor</code> does all the magic for changing test conditions.*/

    class TestVisitor extends LowQuadVisitor {
	Set inductvars;
	Set loopinvars;
	Map allinductions;
	TempMap ssitossamap;
	Loops lp;
	HCode hc;
	Set tests;

	//Create TestVisitor, and inform it of everything.
	TestVisitor(Set loopinvars, Map allinductions, TempMap ssitossamap, HCode hc, Loops lp, Set tests) {
	    this.loopinvars=loopinvars;
	    this.allinductions=allinductions;
	    this.inductvars=allinductions.keySet();
	    this.ssitossamap=ssitossamap;
	    this.hc=hc;
	    this.lp=lp;
	    this.tests=tests;
	}

	//Default method...do nothing
	public void visit(Quad q) {/* Do nothing*/}

	//POPER visitor
	//Only look at ICMPEQ, and ICMPGT
	public void visit(harpoon.IR.Quads.AGET q)    {}
	
	public void visit(harpoon.IR.Quads.ASET q)    {}

	public void visit(harpoon.IR.Quads.CALL q)    {}

	public void visit(harpoon.IR.Quads.GET q)     {}

	//      Lets let this error go through...shouldn't see any HANDLERS
	//      in a loop.
	//	public void visit(harpoon.IR.Quads.HANDLER q) {}

	public void visit(harpoon.IR.Quads.SET q)     {}

	public void visit(POPER q) {
	    switch (q.opcode()) {
	    case Qop.ICMPEQ:
	    case Qop.ICMPGT:
		//look at the POPER
		//return new POPER if we can do stuff
		if (lookat(q)) tests.add(q);
		break;
	    default:
	    }
	}

	public void visit(OPER q) {
	    switch (q.opcode()) {
	    case Qop.ICMPEQ:
	    case Qop.ICMPGT:
		//look at the OPER
		//return new OPER if we can do stuff
		if (lookat(q)) tests.add(q);
		break;
	    default:
	    }
	}

	/**<code>lookat</code> examines a test condition.*/
	boolean lookat(OPER q) {
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
	    return (good&&(flag!=-1));
	}
    }

    void forloop(HCode hc, Loops lp) {
	analyze(hc);
	Util.assert(lp.loopEntrances().size()==1,"Loop must have one entrance");	
	Quad header=(Quad)(lp.loopEntrances()).toArray()[0];;
	Set testsopers=doLooptest(hc,lp);
    }

    /*  NOTE:  Assumption is made that no quad can go wrong, otherwise
	 there would have been an explicity test before it...*/
    class ForLoopVisitor extends LowQuadVisitor {
	private WorkSet track;
	private boolean sideeffects;
	private Set testsopers;
	private UseDef ud;
	private HCode hc;
	private Loops lp;
	private TempMap ssitossamap;

	ForLoopVisitor(Set testsopers, HCode hc, UseDef ud, Loops lp, TempMap ssitossamap) {
	    this.track=new WorkSet();
	    this.sideeffects=false;
	    this.testsopers=testsopers;
	    this.ud=ud;
	    this.hc=hc;
	    this.lp=lp;
	    this.ssitossamap=ssitossamap;
	}

	boolean sideEffects() {
	    return sideeffects;
	}

	public void visit(Quad q) {
	    System.out.println("Error in ForLoopVisitor");
	    System.out.println(q.toString()+" unhandled");
	}

	public void visit(AGET q)		{ trackuses(q); }
	public void visit(ALENGTH q)     	{ trackuses(q); }
	public void visit(ANEW q)		{ trackuses(q); }
	//These two may have side effects....
	//Need to complete more complicated analysis on them...
	public void visit(ARRAYINIT q)          { sideeffects(q); }
	public void visit(ASET q)		{ sideeffects(q); }

	//Calls have side efects
	public void visit(CALL q)		{ sideeffects(q); }

	//CJMP stops search
	public void visit(CJMP q)		{
	    //is this a test condition?
	    Temp test=q.test();
	    Quad[] defs=(Quad[])ud.defMap(hc, test);
	    Util.assert(defs.length==1, "We work only with SSA/SSI");
	    if (testsopers.contains(defs[0])) {
		//Need to see if it:
		//1) Is on a basic induction variable!
		//2) That the jump leaves the loop
		//3) None of the trackuses set gets used outside of the loop
		//4) None of the sigma defines gets used outside of the loop
		//5) That the increment of the basic induction variable doesn't
		//get used at any point other than the phi function...
		if (analyzecjmp(q)) {
		    //finished #2, setup track for #3, 4...
		    //See if we have a basic induction varible...
		    OPER testoper=(OPER)defs[0];
		    Map bamap=(Map)bimap.get(lp.loopEntrances().toArray()[0]);
		    int binvarnum=0;
		    for (int i=0;i<testoper.operandsLength();i++) {
			if (bamap.containsKey(ssitossamap.tempMap(testoper.operands(i))))
			    binvarnum++;
			    //have a basic induction variable [#1 finished]
		    }
		    if (binvarnum==1) {
			//Still need to verify track set [#3, #4]
			//Still need to check uses of increment [#5]

		    }
		}
	    }
	}
	boolean analyzecjmp(CJMP q) {
	    boolean exit=false;
	    for (int i=0;i<q.nextLength();i++)
		if (!lp.loopIncelements().contains(q.next(i))) {
		    //we've found the way out...
		    //we only add things in if
		    //they were not generated in front of us...
		    //might create confusing semantic,
		    //but gotta do it to find any for loops at all that
		    //allow lv to escape
		    for(int j=0;j<q.numSigmas();j++)
			if (track.contains(q.src(j)))
			    track.add(q.dst(j,i));
		    exit=true;
		}
	    return exit;
	}

	public void visit(COMPONENTOF q)	{ trackuses(q); }
	public void visit(CONST q)		{ trackuses(q); }

	public void visit(GET q)		{ trackuses(q); }

	//Our friend...
	public void visit(INSTANCEOF q)	        { trackuses(q); }

	public void visit(MONITORENTER q)	{ sideeffects(q); }
	public void visit(MONITOREXIT q)	{ sideeffects(q); }
	public void visit(MOVE q)		{ trackuses(q); }
	public void visit(NEW q)		{ trackuses(q); }

	public void visit(POPER q)              { checkopers(q); }
	public void visit(OPER q)		{ checkopers(q); }

	private void checkopers(OPER q) {
	    switch (q.opcode()) {
	    case Qop.IDIV:
	    case Qop.IREM:
	    case Qop.LDIV:
	    case Qop.LREM:
		sideeffects(q);
		break;
	    default:
		trackuses(q);
		break;
	    }
	}

	//Might want to do something different here....
	//IE..Only add if track.cotnains(src of the phi function)...
	//Not done yet, would complicate things...
	public void visit(PHI q)		{ trackuses(q); }
	public void visit(RETURN q)		{ sideeffects(q); }
	//Have to do more complicated analysis...
	public void visit(SET q)		{ sideeffects(q); }

	public void visit(THROW q)		{ sideeffects(q); }
	public void visit(TYPECAST q)           { trackuses(q); }
	
	public void visit(PCALL q)      { sideeffects(q); }
	public void visit(PGET q)       { trackuses(q); }
	//Need more complicated analysis...
	public void visit(PSET q)       { sideeffects(q); }
	
	// PPTR:
	public void visit(PARRAY q)     { trackuses(q); }
	public void visit(PFIELD q)     { trackuses(q); }
	public void visit(PMETHOD q)    { trackuses(q); }
	// PCONST:
	public void visit(PCONST q)     { trackuses(q); }
	public void visit(PAOFFSET q)   { trackuses(q); }
	public void visit(PFOFFSET q)   { trackuses(q); }
	public void visit(PMOFFSET q)   { trackuses(q); }
	public void visit(PFCONST q)    { trackuses(q); }
	public void visit(PMCONST q)    { trackuses(q); }
	void trackuses(Quad q) {
	    Temp[] defs=q.def();
	    for (int i=0;i<defs.length;i++) {
		track.add(defs[i]);
	    }
	}
	void sideeffects(Quad q) {
	    sideeffects=true;
	}
    }
}



