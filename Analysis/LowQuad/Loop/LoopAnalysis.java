// LoopAnalysis.java, created Thu Jun 24 11:45:07 1998 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.LowQuad.Loop;

import harpoon.Analysis.UseDef;
import harpoon.Analysis.Loops.LoopFinder;
import harpoon.Util.WorkSet;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCode;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.COMPONENTOF;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.LowQuad.POPER;
import harpoon.IR.LowQuad.PCALL;
import harpoon.IR.LowQuad.PGET;
import harpoon.IR.LowQuad.PSET;
import harpoon.IR.LowQuad.PARRAY;
import harpoon.IR.LowQuad.PFIELD;
import harpoon.IR.LowQuad.PMETHOD;
import harpoon.IR.LowQuad.PCONST;
import harpoon.IR.LowQuad.PAOFFSET;
import harpoon.IR.LowQuad.PFOFFSET;
import harpoon.IR.LowQuad.PMOFFSET;
import harpoon.IR.LowQuad.PFCONST;
import harpoon.IR.LowQuad.PMCONST;
import harpoon.IR.LowQuad.LowQuadKind;
import harpoon.Analysis.Loops.LoopFinder;
import harpoon.Analysis.Loops.Loops;
import harpoon.Temp.Temp;
import harpoon.Analysis.SSITOSSAMap;
import harpoon.Temp.TempMap;
import harpoon.IR.Properties.CFGraphable;
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
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopAnalysis.java,v 1.1.2.23 2001-06-17 22:30:19 cananian Exp $
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
	Set loopelements=lp.loopIncElements();
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
	Set loopelements=lp.loopIncElements();
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
	WorkSet elements=(WorkSet)lp.loopIncElements();
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
	Set elements=lp.loopIncElements();
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
	    case Qop.LCMPEQ:
	    case Qop.LCMPGT:
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
	    case Qop.LCMPEQ:
	    case Qop.LCMPGT:
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
    /**<code>forloop</code> takes in an <code>HCode</code> and
     * <code>Loops</code> to analyze.  It returns a null if there is
     * no for loop, or a <code>ForLoopInfo</code> if it finds a for loop.
     * It works on Quads and LowQuads on both SSI and SSA forms.*/

    public ForLoopInfo forloop(HCode hc, Loops lp) {
	analyze(hc);
	Util.assert(lp.loopEntrances().size()==1,"Loop must have one entrance");	
	Quad header=(Quad)(lp.loopEntrances()).toArray()[0];;
	Set testsopers=doLooptest(hc,lp);
	ForLoopVisitor flv=new ForLoopVisitor(testsopers, hc, ud, lp, tm);
	for (Quad ptr=header.next(0);!(flv.sideEffects()||flv.done());ptr=ptr.next(0)) {
	    ptr.accept(flv);
	}
	if (flv.forLoop()) {
	    ForLoopInfo retval=null;
	    OPER test=flv.testCondition();
	    CJMP cjmp=flv.cjmpExit();
	    Temp ivar=flv.inductionVar();
	    Temp increment=findIncrement(hc, ivar, lp);
	    int bindex=flv.bindex();
	    int cjmpedge=flv.cjmpedge();
	    switch (test.opcode()) {
	    case Qop.LCMPEQ:
	    case Qop.ICMPEQ:
		if (cjmpedge==1)
		    retval=new ForLoopInfo(ForLoopInfo.NEQ,ivar,increment,
					   initialTemp(hc, ivar, lp),
					   test.operands(1-bindex),test,cjmp);
		break;
	    case Qop.LCMPGT:
	    case Qop.ICMPGT:
		int cond=0;
		if ((bindex==0)&&(cjmpedge==1))
		    cond=ForLoopInfo.LTE;
		if ((bindex==1)&&(cjmpedge==1))
		    cond=ForLoopInfo.GTE;
		if ((bindex==0)&&(cjmpedge==0))
		    cond=ForLoopInfo.GT;
		if ((bindex==1)&&(cjmpedge==0))
		    cond=ForLoopInfo.LT;
		retval=new ForLoopInfo(cond,ivar,increment,
				       initialTemp(hc, ivar, lp),
				       test.operands(1-bindex),test,cjmp);
		break;
	    default:
	    }
	    return retval;
	}
	else 
	    return null;
    }

    /**<code>ForLoopInfo</code> encapsulated information on a for loop.*/
    public class ForLoopInfo {
	private int testtype;
	private Temp induction;
	private Temp increment;
	private Temp indinitial;
	private Temp indfinal;
	private OPER testquad;
	private CJMP loopexit;
	ForLoopInfo(int testtype,Temp induction, Temp increment,Temp indinitial, Temp indfinal, OPER testoper, CJMP loopexit) {
	    this.testtype=testtype;
	    this.induction=induction;
	    this.indinitial=indinitial;
	    this.indfinal=indfinal;
	    this.testquad=testquad;
	    this.loopexit=loopexit;
	    this.increment=increment;
  	}
	/**<code>testtype</code> tells the type of test used.  This is the
	 * condition to continue in the loop.*/
	public int testtype() {
	    return testtype;
	}
	/**<code>increment</code> gives the temp with the loop invariant
	 * value to increment the induction variable by.*/
	public Temp increment() {
	    return increment;
	}
	/**<code>induction</code> gives the temp with the induction variable
	 * of the for loop.*/
	public Temp induction() {
	    return induction;
	}
	/**<code>indInitial</code> gives the initial value of the induction
	 * variable.*/
	public Temp indInitial() {
	    return indinitial;
	}
	/**<code>indFinal</code> gives the final value of the induction
	 * variable.*/
	public Temp indFinal() {
	    return indfinal;
	}
	/**<code>testOPER</code> gives the <code>OPER</code> containing the
	 * for loop test condition.*/
	public OPER testOPER() {
	    return testquad;
	}
	/**<code>loopExit</code> gives the <code>CJMP</code> that is the
	 * first exit of the loop.*/
	public CJMP loopExit() {
	    return loopexit;
	}
	/** != case */
	public final static int NEQ = 0;
	/** < case */
	public final static int LT = 1;
	/** <= case */
	public final static int LTE = 2;
	/** > case */
	public final static int GT = 3;
	/** >= case */
	public final static int GTE = 4;
	/** <code>toString</code> returns a string representation of the
	 * for loop.*/
	public String toString() {
	    String ttype=null;
	    switch(testtype) {
	    case NEQ:
		ttype="!=";
		break;
	    case LT:
		ttype="<";
		break;
	    case LTE:
		ttype="<=";
		break;
	    case GT:
		ttype=">";
		break;
	    case GTE:
		ttype=">=";
		break;
	    default:
	    }
	    return new String(induction+"="+indinitial+
			      ";"+induction+ttype+indfinal+";"
			      +induction+"+="+increment);
	}
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
	private boolean analysisdone;
	private Temp inductionvar;
	private OPER testcondition;
	private int bindex;
	private int cjmpedge;
	private CJMP cjmp;

	ForLoopVisitor(Set testsopers, HCode hc, UseDef ud, Loops lp, TempMap ssitossamap) {
	    this.track=new WorkSet();
	    this.sideeffects=false;
	    this.testsopers=testsopers;
	    this.ud=ud;
	    this.hc=hc;
	    this.lp=lp;
	    this.ssitossamap=ssitossamap;
	    this.analysisdone=false;
	    this.inductionvar=null;
	    this.testcondition=null;
	    this.cjmpedge=-1;
	}

	int cjmpedge() {
	    return cjmpedge;
	}

	boolean done() {
	    return analysisdone;
	}

	boolean sideEffects() {
	    return sideeffects;
	}

	boolean forLoop() {
	    return (analysisdone&&(inductionvar!=null));
	}

	CJMP cjmpExit() {
	    return cjmp;
	}

	OPER testCondition() {
	    return testcondition;
	}
	
	Temp inductionVar() {
	    return inductionvar;
	} 

	int bindex() {
	    return bindex;
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
	    Temp binvar=null;
	    if (testsopers.contains(defs[0])) {
		//Need to see if it:
		//1) Is on a basic induction variable!
		//2) That the jump leaves the loop
		//3) None of the trackuses set gets used outside of the loop
		//4) None of the sigma defines gets used outside of the loop
		//5) That the increment of the basic induction variable doesn't
		//get used at any point other than the phi function...
		int exit=analyzecjmp(q);
		if (exit!=-1) {
		    //finished #2, setup track for #3, 4...
		    //See if we have a basic induction varible...
		    OPER testoper=(OPER)defs[0];
		    Map bamap=(Map)bimap.get(lp.loopEntrances().toArray()[0]);
		    for (int i=0;i<testoper.operandsLength();i++) {
			if (bamap.containsKey(ssitossamap.tempMap(testoper.operands(i)))) {
			    binvar=ssitossamap.tempMap(testoper.operands(i));
			    bindex=i;
			}
			    //have a basic induction variable [#1 finished]
		    }
		    if (binvar!=null) {
			//Still need to verify track set [#3, #4]
			//Still need to check uses of increment [#5]
			PHI header=(PHI)(lp.loopEntrances()).toArray()[0];
			OPER addquad=(OPER)addQuad(hc, header,binvar, lp.loopIncElements());			
			Temp nextinc=addquad.dst();
			boolean otheruse=false;
			WorkSet tocheck=new WorkSet();
			WorkSet done=new WorkSet();
			tocheck.add(nextinc);
			boolean good=true;
			while(good&&(!tocheck.isEmpty())) {
			    Temp tuse=(Temp)tocheck.pop();
			    done.add(tuse);
			    HCodeElement[] niuses=ud.useMap(hc, tuse);
			    for (int i=0;i<niuses.length;i++) 
				if (niuses[i]!=header) {
				    int kind=((Quad)niuses[i]).kind();
				    if ((kind==QuadKind.CJMP)||
					(kind==QuadKind.SWITCH)||
					(kind==QuadKind.TYPESWITCH)) {
					SIGMA sigma=(SIGMA)niuses[i];
					for(int j=0;j<sigma.numSigmas();j++)
					    if(sigma.src(j)==tuse) {
						for(int k=0;k<sigma.arity();k++)
						    if (!done.contains(sigma.dst(j,k)))
							tocheck.add(sigma.dst(j,k));
					    }
				    } else if(kind==QuadKind.CALL) {
					CALL csigma=(CALL)niuses[i];
					for(int l=0;l<csigma.paramsLength();l++)
					    if (csigma.params(i)==tuse) {
						good=false;
						break;
					    }
					if (good)
					    for(int j=0;j<csigma.numSigmas();j++)
						if(csigma.src(j)==tuse) {
						    for(int k=0;k<csigma.arity();k++)
							if (!done.contains(csigma.dst(j,k)))
							    tocheck.add(csigma.dst(j,k));
						}
				    } else if(kind==LowQuadKind.PCALL) {
					PCALL psigma=(PCALL)niuses[i];
					for(int l=0;l<psigma.paramsLength();l++)
					    if (psigma.params(i)==tuse) {
						good=false;
						break;
					    }
					if (good)
					    for(int j=0;j<psigma.numSigmas();j++)
						if(psigma.src(j)==tuse) {
						    for(int k=0;k<psigma.arity();k++)
							if (!done.contains(psigma.dst(j,k)))
							    tocheck.add(psigma.dst(j,k));
						}
				    } else if(kind==QuadKind.PHI) {
					PHI phi=(PHI)niuses[i];
					for (int j=0;j<phi.numPhis();j++)
					    for (int k=0;k<phi.numPhis();k++) {
						if (tuse==phi.src(j,k)) {
						    if (!done.contains(phi.dst(j)))
						    tocheck.add(phi.dst(j));
						}
					    }
				    } else
					good=false;
				    
				}
			}
			if (good)
			    //condition #5 satisfied
			    if (checktracks()) {
				inductionvar=binvar;
				cjmpedge=exit;
				testcondition=testoper;
				cjmp=q;
			    }
			//conditions 3&4 satisfied
		    }
		}
	    }
	    analysisdone=true;
	}

	boolean checktracks() {
	    Iterator trackiterate=track.iterator();
	    boolean go=true;
	    Set loopset=lp.loopIncElements();
	    while (trackiterate.hasNext()&&go) {
		Temp temptocheck=(Temp)trackiterate.next();
		HCodeElement[] uses=ud.useMap(hc, temptocheck);
		for (int i=0;i<uses.length;i++) {
		    if (!loopset.contains(uses[i])) {
			go=false;
			break;
		    }
		}
	    }
	    return go;
	}

	int analyzecjmp(CJMP q) {
	    int exit=-1;
	    for (int i=0;i<q.nextLength();i++)
		if (!lp.loopIncElements().contains(q.next(i))) {
		    //we've found the way out...
		    //we only add things in if
		    //they were not generated in front of us...
		    //might create confusing semantics,
		    //but gotta do it to find any for loops at all that
		    //allow lv to escape
		    for(int j=0;j<q.numSigmas();j++)
			if (track.contains(q.src(j)))
			    track.add(q.dst(j,i));
		    exit=i;
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

	//See if we have division by 0
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



