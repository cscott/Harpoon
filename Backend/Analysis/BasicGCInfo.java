// BasicGCInfo.java, created Wed Jan 26 11:05:45 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.Instr.IgnoreSpillUseDefer;
import harpoon.Analysis.Instr.RegAlloc.IntermediateCode;
import harpoon.Analysis.Instr.RegAlloc.IntermediateCodeFactory;
import harpoon.Analysis.Liveness;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsAltImpl;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.GCInfo.DLoc;
import harpoon.Backend.Generic.GCInfo.GCPoint;
import harpoon.Backend.Generic.GCInfo.WrappedMachineRegLoc;
import harpoon.Backend.Generic.GCInfo.WrappedStackOffsetLoc;
import harpoon.Backend.Generic.RegFileInfo.CommonLoc;
import harpoon.Backend.Generic.RegFileInfo.MachineRegLoc;
import harpoon.Backend.Generic.RegFileInfo.StackOffsetLoc;
import harpoon.Backend.Generic.RegFileInfo.TempLocator;
import harpoon.Backend.Maps.BackendDerivation;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrCALL;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrJUMP;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrVisitor;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>BasicGCInfo</code> selects as GC points all
 * call sites and backward branches.
 * 
 * @author  Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: BasicGCInfo.java,v 1.1.2.24 2001-11-08 00:24:05 cananian Exp $
 */
public class BasicGCInfo extends harpoon.Backend.Generic.GCInfo {
    // Maps methods to gc points
    final private Map m = new HashMap();
    // Maps classes to methods that have been processed
    final private Map orderedMethods = new HashMap();
    /** Returns an ordered, unmodifiable <code>List</code> of the
	<code>GCPoint</code>s in a given <code>HMethod</code>.
	Returns <code>null</code> if the <code>HMethod</code>
	has not been evaluated for garbage collection purposes.
	Returns an empty <code>List</code> if the 
	<code>HMethod</code> has been evaluated and has been
	found to not contain any GC points.
    */    
    public List gcPoints(HMethod hm) {
	return Collections.unmodifiableList((List)m.get(hm));
    }
    /** Returns an ordered, unmodifiable <code>List</code> of 
	<code>HMethod</code>s with the following properties:
	- The declaring class of the <code>HMethod</code> is
	<code>HClass</code>.
	- The <code>convert</code> method of the 
	<code>IntermediateCodeFactory</code> has been invoked
	on all the <code>HMethod</code>s in the <code>List</code>. 
	The <code>IntermediateCodeFactory</code> referred
	to here is the one returned by the <code>codeFactory</code>
	method of <code>this</code>. Returns null if the given 
	<code>HClass</code> does not declare any methods on which 
	<code>convert</code> has been invoked.
	- The <code>HMethod</code>s are ordered according to the
	order in which the <code>convert</code> method was invoked.
    */
    public List getOrderedMethods(HClass hc) {
	final List result = (List)orderedMethods.get(hc);
	if (result == null) return null;
	return Collections.unmodifiableList(result);
    }
    // adds a given method to the orderedMethod map
    // used to maintain the orderedMethod map
    private void addToOrderedMethods(HMethod hm) {
	List l = (List)orderedMethods.get(hm.getDeclaringClass());
	if (l != null) {
	    // hm should not yet be in the List
	    Util.assert(!l.contains(hm));
	} else {
	    // the HClass is not yet in the map; make a new List
	    l = new ArrayList();
	    // add new entry to map
	    orderedMethods.put(hm.getDeclaringClass(), l);
	}
	l.add(hm);
    }
    /** Returns an IntermediateCodeFactory that inserts
	<code>InstrLABEL</code>s at garbage collection points
	and stores the information needed by the garbage
	collector in <code>this</code>.
	<BR> <B>requires:</B> The <code>parentFactory</code>
	     in <code>Instr</code> form.
    */
    public IntermediateCodeFactory 
	codeFactory(final IntermediateCodeFactory parentFactory, 
		    final Frame frame)
    { 
	return new IntermediateCodeFactory() {
	    protected final HCodeFactory parent = parentFactory;
	    protected final Frame f = frame;
	    protected final CFGrapher cfger = CFGrapher.DEFAULT;
	    protected final Map hce2label = new HashMap();
	    public HCode convert(HMethod hm) {
		// preserve ordering information
		addToOrderedMethods(hm);
		harpoon.IR.Assem.Code hc = 
		    (harpoon.IR.Assem.Code)parent.convert(hm);
		if (hc == null) {
		    // need to map method to empty List
		    // of GC points to indicate that the 
		    // method has been processed, but no
		    // GC points were found
		    m.put(hm, new ArrayList());
		    return null;
		}
		List hceList = hc.getElementsL();
		UseDefer ud = new IgnoreSpillUseDefer();
		// pass 1: liveness and reaching definitions analyses
		LiveTemps ltAnalysis = analyzeLiveness(hc, ud);
		ReachingDefs rdAnalysis = 
		    new ReachingDefsAltImpl(hc, cfger, ud);
		// pass 2: identify backward branches
		Set backEdgeGCPts = identifyBackwardBranches(hc);
		// pass 3: identify GC points
		List gcps = new ArrayList();
		// clear map before going into GCPointFinder
		hce2label.clear();
		// GCPointFinder gcpf = new GCPointFinder
		// (hm, hc, gcps, ltAnalysis, rdAnalysis, backEdgeGCPts, 
		// hc.getDerivation());
		// For now, ignoring back edges.
		GCPointFinder gcpf = 
		    new GCPointFinder(hm, hc, gcps, ltAnalysis, rdAnalysis, 
				      new HashSet(), hc.getDerivation(), ud);
		for(Iterator instrs = hc.getElementsL().iterator();
		    instrs.hasNext(); )
		    ((Instr)instrs.next()).accept(gcpf);
		// put labels in
		for(Iterator instrs = hce2label.keySet().iterator();
		    instrs.hasNext(); ) {
		    Instr i = (Instr)instrs.next();
		    InstrLABEL label = (InstrLABEL)hce2label.get(i);
		    // instr should only have one successor
		    Util.assert(cfger.succ(i).length == 1);
		    // insert label
		    label.layout(i, i.getNext());
		}
		m.put(hm, gcps);  // add to map
		return hc;
	    }
	    // do liveness analysis and return analysis
	    private LiveTemps analyzeLiveness(HCode intermediateCode,
					      UseDefer ud) {
		// use CFGrapher.DEFAULT for now at Felix's bequest
		// Instrs should graduate to having their own CFGrapher
		BasicBlock.Factory bbFact =
		    new BasicBlock.Factory(intermediateCode, cfger);
		Set liveOnExit = f.getRegFileInfo().liveOnExit();
		LiveTemps ltAnalysis = new LiveTemps(bbFact, liveOnExit, ud);
		// get an iterator for the solver
		Iterator it = bbFact.blockSet().iterator();
		harpoon.Analysis.DataFlow.Solver.worklistSolve(it, ltAnalysis);
		// ltAnalysis should now contain the liveness 
		// results we want
		return ltAnalysis;
	    }
	    // identify and return Instrs that come before 
	    // backward branches 
	    private Set identifyBackwardBranches(HCode intermediateCode) {
		// pass 1: number Instrs
		Map ordering = new HashMap();
		int index = 0;
		for(Iterator instrs = intermediateCode.getElementsL().
			iterator(); instrs.hasNext(); )
		    ordering.put(instrs.next(), new Integer(index++));
		// pass 2: identify backward branches
		Set results = new HashSet();
		// work list of edges that need to be examined
		// start with the successors of the root Instr
		HCodeElement root = intermediateCode.getRootElement();
		Worklist toProcess = new WorkSet(cfger.succC(root));
		while(!toProcess.isEmpty()) {
		    HCodeEdge edge = (HCodeEdge)toProcess.pull();
		    Integer from = (Integer)ordering.get(edge.from());
		    Integer to = (Integer)ordering.get(edge.to());
		    Util.assert(from != null && to != null);
		    if (from.intValue() > to.intValue())
			results.add(edge.from());
		    else {
			Util.assert(from.intValue() != to.intValue());
			// add to work list the successor edges
			for (Iterator edges = cfger.succC(edge.to()).
				 iterator(); edges.hasNext(); )
			    toProcess.push(edges.next());
		    }
		}
		return results;
	    }
	    public String getCodeName() {
		return parent.getCodeName(); // should i have my own name?
	    }
	    public void clear(HMethod hm) {
		parent.clear(hm);
		m.remove(hm); // remove from map
	    }
	    class GCPointFinder extends InstrVisitor {
		protected final List results;
		protected final LiveTemps lt;
		protected final ReachingDefs rd;
		protected final Set s;
		protected final TempLocator tl;
		protected final harpoon.Analysis.Maps.Derivation d;
		protected final HMethod hm;
		protected final HCode hc;
		protected final UseDefer ud;
		protected int index = 0;
		/** Creates a <code>GCPointFinder</code> object.
		    @param results
		    an empty <code>List</code> for storing
		    the resulting <code>GCPoint</code> objects
		    @param lt
		    the completed <code>LiveTemps</code> analysis
		    @param s
		    the <code>Set</code> of <code>Instr</code>s
		    that occur before a backward branch
		*/
		public GCPointFinder(HMethod hm, HCode hc, List results, 
				     LiveTemps lt, 
				     ReachingDefs rd, Set s, 
				     harpoon.Analysis.Maps.Derivation d,
				     UseDefer ud) {
		    this.hm = hm;
		    this.hc = hc;
		    Util.assert(results != null && results.isEmpty());
		    this.results = results;
		    this.lt = lt;
		    this.rd = rd;
		    this.s = s;
		    this.d = d;
		    this.tl = ((IntermediateCode)hc).getTempLocator();
		    this.ud = ud;
		}
		public void visit(InstrCALL c) {
		    // all InstrCALLs are GC points
		    if (c.getTargets().size() == 0) {
			// native calls fall through
			Util.assert
			    (c.canFallThrough, 
			     "InstrCALL with no targets must fall through.");
			updateGCInfo(c, null);
		    } else {
			List targets = c.getTargets();
			// non-native calls have a return
			// address and an exception address
			Util.assert
			    (targets.size() == 2,
			     "InstrCALL with targets must have regular"+
			     " and exceptional return addresses.");
			updateGCInfo(c, (Label)targets.get(0));
		    }
		}
		public void visit(InstrJUMP j) {
		    // InstrJUMPs are GC points only if
		    // they come before a backward edge
		    if (!s.contains(j)) return;
		    List targets = j.getTargets();
		    Util.assert(targets.size() == 1,
				"Multiple targets for InstrJUMP.");
		    updateGCInfo(j, (Label)targets.get(0));
		}
		public void visit(Instr i) {
		    if (!s.contains(i)) return;
		    // Instrs are GC points only if
		    // they come before a backward edge,
		    // in which case they must have a
		    // conditional target
		    Util.assert
			(i.canFallThrough,
			 "Cannot fall through non-jump,"+
			 " non-call Instr before a backward edge.");
		    List targets = i.getTargets();
		    Util.assert
			(targets.size() == 1,
			 "No target for Instr before a backward edge.");
		    updateGCInfo(i, (Label)targets.get(0));
		    // alternatively:
		    // updateGCInfo(i, null);
		}
		private void updateGCInfo(Instr i, Label l) {
		    // add label if one is not already provided
		    if (l == null) {
			String str = 
			    f.getRuntime().getNameMap().mangle(hm, "gcp_"+index++);
			l = new Label(str);
			InstrLABEL label = f.getInstrBuilder().makeLabel(l, i);
			hce2label.put(i, label);
		    }
		    // we want the live temps going into the instr
		    WorkSet live = new WorkSet();
		    live.addAll(lt.getLiveBefore(i));
		    // filter out non-pointers and derived pointers
		    Set liveLocs = new HashSet();
		    Map derivedPtrs = new HashMap();
		    Map calleeSaved = new HashMap();
		    while(!live.isEmpty()) {
			Temp t = (Temp)live.pull();
			//System.out.println(t.toString()+" is live.");
			Util.assert(i != null, 
				    "Cannot pass null instruction"+
				    " to reaching definitions analysis");
			Util.assert(t != null, 
				    "Cannot pass null temporary"+
				    " to reaching definitions analysis");
			Iterator defPtsit = 
			    rd.reachingDefs(i, t).iterator();
			// there must be at least one defintion 
			// that reaches i
			Util.assert(defPtsit.hasNext(), "Cannot find"+
				    " definition of "+t.toString()+" at "+
				    i.toString());
			Instr defPt = (Instr)defPtsit.next();
			// all of the above defPts should work
			DList ddl = d.derivation(defPt, t);
			if (ddl == null) {
			    // try and find its type
			    HClass hclass = d.typeMap(defPt, t);
			    if (hclass == null) {
				// no derivation, no type means
				// this is a callee-saved register
				BackendDerivation bd = (BackendDerivation)d;
				// find out which register's contents we have
				BackendDerivation.Register reg =
				    bd.calleeSaveRegister(defPt, t);
				Set locationSet = tl.locate(t, defPt);
				// the following may be a bad assumption
				Util.assert(locationSet.size() == 1);
				for (Iterator it=locationSet.iterator();
				     it.hasNext(); ) {
				    calleeSaved.put(reg, (CommonLoc)it.next());
				}
			    } else if (!hclass.isPrimitive())
				// a non-derived pointer: add all 
				// locations where it can be found
				liveLocs.addAll(tl.locate(t, defPt));
			} else
			    // a derived pointer: add to set of derived ptrs
			    derivedPtrs.put(tl.locate(t, defPt), 
					    unroll(ddl, i));
		    }
		    GCPoint gcp = 
			new GCPoint(i, l, derivedPtrs, liveLocs, calleeSaved);
		    results.add(gcp);
		}
		private DLoc unroll(DList ddl, Instr instr) {
		    List regLocs = new ArrayList();
		    List regSigns = new ArrayList();
		    List stackLocs = new ArrayList();
		    List stackSigns = new ArrayList();
		    while(ddl != null) {
			Temp base = ddl.base;
			// System.out.println("doing reachingDefs"+
			//		   " instr:"+instr+
			//		   " base:"+base);
			Collection c1 = rd.reachingDefs(instr, base);
			Util.assert(c1 != null);
			Util.assert(c1.size() >0);
			Instr[] defPts = (Instr[]) 
			    c1.toArray(new Instr[c1.size()]);
			// any of the definition points should work
			Collection c2 = tl.locate(base, defPts[0]);
			Util.assert(c2 != null && c2.size() > 0);
			CommonLoc[] locs = (CommonLoc[])
			    c2.toArray(new CommonLoc[c2.size()]);
			// any of the CommonLocs should work
			switch(locs[0].kind()) {
			case StackOffsetLoc.KIND:
			    WrappedStackOffsetLoc wsol = new 
				WrappedStackOffsetLoc((StackOffsetLoc)locs[0]);
			    stackLocs.add(wsol); 
			    stackSigns.add(new Boolean(ddl.sign));
			    break;
			case MachineRegLoc.KIND:
			    WrappedMachineRegLoc wmrl = new 
				WrappedMachineRegLoc((MachineRegLoc)locs[0]);
			    regLocs.add(wmrl);
			    regSigns.add(new Boolean(ddl.sign));
			    break;
			default: Util.assert(false);
			}
			ddl = ddl.next; // FSK moved this outside switch
		    }
		    Util.assert(regLocs.size() == regSigns.size());
		    WrappedMachineRegLoc[] regArray = 
			(WrappedMachineRegLoc[])regLocs.toArray
			(new WrappedMachineRegLoc[0]);
		    boolean[] regSignArray = new boolean[regSigns.size()];
		    int i=0;
		    for(Iterator it=regSigns.iterator(); it.hasNext(); )
			regSignArray[i++] = 
			    ((Boolean)it.next()).booleanValue();
		    Util.assert(stackLocs.size() == stackSigns.size());
		    WrappedStackOffsetLoc[] stackArray =
			(WrappedStackOffsetLoc[])stackLocs.toArray
			(new WrappedStackOffsetLoc[0]);
		    boolean[] stackSignArray = new boolean[stackSigns.size()];
		    int j=0;
		    for(Iterator it=stackSigns.iterator(); it.hasNext(); )
			stackSignArray[j++] = 
			    ((Boolean)it.next()).booleanValue();
		
		    return new DLoc(regArray, stackArray, 
				    regSignArray, stackSignArray); 
		}
	    }
	};
    } // codeFactory
} // class





