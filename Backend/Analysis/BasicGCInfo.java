// BasicGCInfo.java, created Wed Jan 26 11:05:45 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@tesuji.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.Instr.RegAlloc.IntermediateCodeFactory;
import harpoon.Analysis.Liveness;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsImpl;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.GCInfo.DLoc;
import harpoon.Backend.Generic.GCInfo.GCPoint;
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
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrVisitor;
import harpoon.IR.Properties.CFGrapher;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.WorkSet;

import java.util.ArrayList;
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
 * @author  Karen K. Zee <kkz@tesuji.lcs.mit.edu>
 * @version $Id: BasicGCInfo.java,v 1.1.2.11 2000-03-02 02:10:23 kkz Exp $
 */
public class BasicGCInfo extends harpoon.Backend.Generic.GCInfo {
    // Maps methods to gc points
    final private Map m = new HashMap();
    // Maps classes to methods that have been processed
    final private Map orderedMethods = new HashMap();
    /** Returns an ordered <code>List</code> of the
	<code>GCPoint</code>s in a given <code>HMethod</code>.
	Returns <code>null</code> if the <code>HMethod</code>
	has not been evaluated for garbage collection purposes.
	Returns an empty <code>List</code> if the 
	<code>HMethod</code> has been evaluated and has been
	found to not contain any GC points.
    */    
    public List gcPoints(HMethod hm) {
	return (List)m.get(hm);
    }
    /** Returns a <code>List</code> of <code>HMethod</code>s
	with the following properties:
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
	return (List)orderedMethods.get(hc);
    }
    // adds a given method to the orderedMethod map
    // used to maintain the orderedMethod map
    private void addToOrderedMethods(HMethod hm) {
	List l = (List)orderedMethods.get(hm.getDeclaringClass());
	if (l != null) {
	    // hm should not yet be in the List
	    Util.assert(!l.contains(hm));
	    // good, add to List
	    l.add(hm);
	} else {
	    // the HClass is not yet in the map
	    // make a new List
	    l = new ArrayList();
	    // add to List
	    l.add(hm);
	    // add new entry to map
	    orderedMethods.put(hm.getDeclaringClass(), l);
	}
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
		// pass 1: liveness and reaching definitions analyses
		LiveTemps ltAnalysis = analyzeLiveness(hc);
		ReachingDefs rdAnalysis = new ReachingDefsImpl(hc, cfger);
		// pass 2: identify backward branches
		Set backEdgeGCPts = identifyBackwardBranches(hc);
		// pass 3: identify GC points
		List gcps = new ArrayList();
		GCPointFinder gcpf = 
		    new GCPointFinder(hm, gcps, ltAnalysis, rdAnalysis, 
				      backEdgeGCPts, hc.getDerivation());
		for(Iterator instrs = hc.getElementsL().iterator();
		    instrs.hasNext(); )
		    ((Instr)instrs.next()).accept(gcpf);
		m.put(hm, gcps);  // add to map
		// force parent codeFactory to rebuild (is this necessary?)
		parent.clear(hm);
		return parent.convert(hm);
	    }
	    // do liveness analysis and return analysis
	    private LiveTemps analyzeLiveness(HCode intermediateCode) {
		// use CFGrapher.DEFAULT for now at Felix's bequest
		// Instrs should graduate to having their own CFGrapher
		BasicBlock.Factory bbFact =
		    new BasicBlock.Factory(intermediateCode, cfger);
		Set liveOnExit = f.getRegFileInfo().liveOnExit();
		LiveTemps ltAnalysis = new LiveTemps(bbFact, liveOnExit);
		// get an iterator for the solver
		Iterator it = bbFact.blockSet().iterator();
		harpoon.Analysis.DataFlow.Solver.worklistSolve(it, ltAnalysis);
		// ltAnalysis should now contain the liveness results we want
		return ltAnalysis;
	    }
	    // identify and return Instrs that come before backward branches 
	    private Set identifyBackwardBranches(HCode intermediateCode) {
		// pass 1: number Instrs
		Map ordering = new HashMap();
		int index = 0;
		for(Iterator instrs=intermediateCode.getElementsL().iterator();
		    instrs.hasNext(); )
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
			for (Iterator edges=cfger.succC(edge.to()).iterator();
			     edges.hasNext(); )
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
	    /** returns <code>TempLocator</code> */
	    public harpoon.Backend.Generic.RegFileInfo.TempLocator
		getTempLocator() { 
		return ((IntermediateCodeFactory)
			parent).getTempLocator(); 
	    }

	    class GCPointFinder extends InstrVisitor {
		protected final List results;
		protected final LiveTemps lt;
		protected final ReachingDefs rd;
		protected final Set s;
		protected final TempLocator tl;
		protected final harpoon.Analysis.Maps.Derivation d;
		protected final HMethod hm; 
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
		public GCPointFinder(HMethod hm, List results, LiveTemps lt, 
				     ReachingDefs rd, Set s, 
				     harpoon.Analysis.Maps.Derivation d) {
		    this.hm = hm;
		    Util.assert(results != null && results.isEmpty());
		    this.results = results;
		    this.lt = lt;
		    this.rd = rd;
		    this.s = s;
		    this.d = d;
		    this.tl = getTempLocator();
		}
		public void visit(InstrCALL c) {
		    // all InstrCALLs are GC points
		    updateGCInfo(c);
		}
		public void visit(Instr i) {
		    // other Instrs are GC points only
		    // if they come before a backward edge
		    if (!s.contains(i)) return;
		    updateGCInfo(i);
		}
		private void updateGCInfo(Instr i) {
		    // add label
		    String str = 
			f.getRuntime().nameMap.mangle(hm, "gcp_"+index++);
		    Label l = new Label(str);
		    InstrLABEL label = f.getInstrBuilder().makeLabel(l, i);
		    // instr should only have one predecessor
		    Util.assert(cfger.pred(i).length == 1);
		    label.insertAt(new InstrEdge(i.getPrev(), i));
		    // conservatively take union of live in and out
		    WorkSet live = new WorkSet();
		    live.addAll(lt.getLiveBefore(i));
		    live.addAll(lt.getLiveAfter(i));
		    // filter out non-pointers and derived pointers
		    Set liveLocs = new HashSet();
		    Map derivedPtrs = new HashMap();
		    StackOffsetLoc[] calleeSaved = 
			new StackOffsetLoc[f.getRegFileInfo().maxRegIndex()];
		    while(!live.isEmpty()) {
			Temp t = (Temp)live.pull();
			Instr[] defPts = 
			    (Instr[])rd.reachingDefs(i, t).toArray();
			// there must be at least one defintion that reaches i
			Util.assert(defPts != null && defPts.length > 0);
			// all of the above defPts should work
			DList ddl = d.derivation(defPts[0], t);
			if (ddl == null) {
			    // try and find its type
			    HClass hclass = d.typeMap(i, t);
			    if (hclass == null) {
				// no derivation, no type means
				// this is a callee-saved register
				BackendDerivation bd = (BackendDerivation)d;
				// find out which register's contents we have
				BackendDerivation.Register reg =
				    bd.calleeSaveRegister(defPts[0], t);
				int rindex = reg.regIndex();
				Set locationSet = tl.locate(t, defPts[0]);
				// this may be a bad assumption, but...
				Util.assert(locationSet.size() == 1);
				for (Iterator it=locationSet.iterator();
				     it.hasNext(); )
				    // another possibly bad assumption
				    calleeSaved[rindex] = 
					(StackOffsetLoc)it.next();
			    } else if (!hclass.isPrimitive())
				// a non-derived pointer: add all 
				// locations where it can be found
				liveLocs.addAll(tl.locate(t, defPts[0]));
			} else
			    // a derived pointer: add to set of derived ptrs
			    derivedPtrs.put(tl.locate(t, defPts[0]), 
					    unroll(ddl, i));
		    }
		    GCPoint gcp = 
			new GCPoint(i, l, derivedPtrs, live, calleeSaved);
		    results.add(gcp);
		}
		private DLoc unroll(DList ddl, Instr instr) {
		    List regLocs = new ArrayList();
		    List regSigns = new ArrayList();
		    List stackLocs = new ArrayList();
		    List stackSigns = new ArrayList();
		    while(ddl != null) {
			Temp base = ddl.base;
			Instr[] defPts = 
			    (Instr[])rd.reachingDefs(instr, base).toArray();
			Util.assert(defPts != null && defPts.length > 0);
			// any of the definition points should work
			CommonLoc[] locs = 
			    (CommonLoc[])tl.locate(base, defPts[0]).toArray();
			Util.assert(locs != null && locs.length > 0);
			// any of the CommonLocs should work
			switch(locs[0].kind()) {
			case StackOffsetLoc.KIND:
			    stackLocs.add(locs[0]); 
			    stackSigns.add(new Boolean(ddl.sign));
			    break;
			case MachineRegLoc.KIND:
			    regLocs.add(locs[0]);
			    regSigns.add(new Boolean(ddl.sign));
			    break;
			default: Util.assert(false);
			ddl = ddl.next;
			}
		    }
		    Util.assert(regLocs.size() == regSigns.size());
		    MachineRegLoc[] regArray = 
			(MachineRegLoc[])regLocs.toArray(new MachineRegLoc[0]);
		    boolean[] regSignArray = new boolean[regSigns.size()];
		    int i=0;
		    for(Iterator it=regSigns.iterator(); it.hasNext(); )
			regSignArray[i++] = 
			    ((Boolean)it.next()).booleanValue();

		    Util.assert(stackLocs.size() == stackSigns.size());
		    StackOffsetLoc[] stackArray =
			(StackOffsetLoc[])stackLocs.toArray(new
							    StackOffsetLoc[0]);
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



