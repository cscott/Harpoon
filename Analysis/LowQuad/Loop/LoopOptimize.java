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
//import harpoon.Analysis.QuadSSA.DeadCode;

import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 * <code>LoopOptimize</code> optimizes the code after <code>LoopAnalysis</code>.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: LoopOptimize.java,v 1.1.2.10 1999-07-02 19:26:52 bdemsky Exp $
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

    public LoopOptimize(LoopAnalysis lanal, TempMap ssitossamap) {
	this(lanal,lanal,lanal,lanal, ssitossamap);
    }

    /** Returns a code factory that uses LoopOptimize. */
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

    public void optimize(final HCode hc) {
    
	//Want defmap of temps in my invariants list, not new temps...
	//Force generation of defmap
	//Need to fix this!!!

	Temp []dummy=ud.allTemps(hc);	


	// actual traversal code.
	Loops lp=loopanal.rootloop(hc);
	WorkSet kids=(WorkSet) lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext())
	    recursetree(hc, (Loops)iterate.next(), new WorkSet());
	//      Put this in soon
	//       	DeadCode.optimize(hc);
    }

    void recursetree(HCode hc, Loops lp, WorkSet usedinvariants) {
	if (lp.loopEntrances().size()==1) {
	    HCodeElement hce=(HCodeElement)(lp.loopEntrances()).toArray()[0];
	    if (((HasEdges)hce).pred().length==2) {
		doLoopinv(hc, lp,(Quad)hce, usedinvariants);
		doLoopind(hc, lp,(Quad)hce, usedinvariants);
	    }
	    else System.out.println("More than one entrance.");
	} else
	    System.out.println("Multiple or No  entrance loop in LoopOptimize!");
	WorkSet kids=(WorkSet) lp.nestedLoops();
	Iterator iterate=kids.iterator();
	while (iterate.hasNext())
	    recursetree(hc, (Loops)iterate.next(),usedinvariants);
    }

    void doLoopind(HCode hc, Loops lp,Quad header,WorkSet usedinvariants) {
	Map basmap=bimap.basicInductionsMap(hc,lp);
	Map allmap=aimap.allInductionsMap(hc,lp);
	WorkSet basic=new WorkSet(basmap.keySet());
	WorkSet complete=new WorkSet(allmap.keySet());
	LoopMap loopmap=new LoopMap(hc,lp,ssitossamap);

	Iterator iterate=complete.iterator();

	
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


	while (iterate.hasNext()) {
	    Temp indvariable=(Temp) iterate.next();
	    Induction induction=(Induction) allmap.get(indvariable);
	    if (induction.pointerindex) {
		iterate.remove();
	    } else {
		//Non pointer index...
		//We have a derived induction variable...
		Temp consttemp=null;
		Temp initial=initialTemp(hc,(PHI)header, induction.variable, lp.loopIncelements());
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
    
    /** initialTemp takes in a <code>Temp</code> t that needs to be a basic
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

    /** <code>findIncrement</code>*/

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







