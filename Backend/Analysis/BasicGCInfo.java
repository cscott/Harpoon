// BasicGCInfo.java, created Wed Jan 26 11:05:45 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@tesuji.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.Instr.RegAlloc.IntermediateCodeFactory;
import harpoon.Analysis.Liveness;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.GCInfo.GCPoint;
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
 * @version $Id: BasicGCInfo.java,v 1.1.2.4 2000-02-01 16:42:40 pnkfelix Exp $
 */
public class BasicGCInfo extends harpoon.Backend.Generic.GCInfo {
    
    /** Returns an IntermediateCodeFactory that inserts
	<code>InstrLABEL</code>s at garbage collection points
	and stores the information needed by the garbage
	collector in the <code>BasicGCInfo</code> object.
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
		HCode hc = parent.convert(hm);
		if (hc == null) return null;
		// pass 1: liveness analysis
		LiveTemps ltAnalysis = analyzeLiveness(hc);
		// pass 2: identify backward branches
		Set instrsBeforeBackEdges = identifyBackwardBranches(hc);
		// pass 3: identify GC points
		List gcps = new ArrayList();
		GCPointFinder gcpf = new GCPointFinder(hm, gcps, ltAnalysis, 
						       instrsBeforeBackEdges,
						       getDerivation());
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
		HCodeElement hce = intermediateCode.getRootElement();
		// use CFGrapher.DEFAULT for now at Felix's bequest
		// Instrs should graduate to having their own CFGrapher
		BasicBlock root = 
		    (new BasicBlock.Factory(hce, cfger)).getRoot();
		Iterator it = root.blocksIterator();
		Set liveOnExit = f.getRegFileInfo().liveOnExit();
		LiveTemps ltAnalysis = new LiveTemps(it, liveOnExit);
		// get a new iterator for the solver
		it = root.blocksIterator();
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
	    public harpoon.Analysis.Maps.Derivation getDerivation() {
		return ((IntermediateCodeFactory)parent).getDerivation();
	    }
	    class GCPointFinder extends InstrVisitor {
		protected final List results;
		protected final LiveTemps lt;
		protected final Set s;
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
				     Set s, 
				     harpoon.Analysis.Maps.Derivation d) {
		    this.hm = hm;
		    Util.assert(results != null && results.isEmpty());
		    this.results = results;
		    this.lt = lt;
		    this.s = s;
		    this.d = d;
		}
		public void visit(InstrCALL c) {
		    updateGCInfo(c);
		}
		public void visit(Instr i) {
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
		    Set live = new HashSet();
		    Map derivedPtrs = new HashMap();
		    live.addAll(lt.getLiveBefore(i));
		    live.addAll(lt.getLiveAfter(i));
		    // KKZ note to self: here's another good
		    // place to filter out non-pointers...
		    // filter out derived pointers
		    Worklist toProcess = new WorkSet(live);
		    while(!toProcess.isEmpty()) {
			Temp t = (Temp)toProcess.pull();
			harpoon.Analysis.Maps.Derivation.DList ddl = 
			    d.derivation(i, t);
			if (ddl == null) continue;
			Util.assert(live.remove(t));
			derivedPtrs.put(t, ddl);
		    }
		    GCPoint gcp = new GCPoint(i, l, derivedPtrs, live);
		    results.add(gcp);
		}
	    }
	};
    } // codeFactory
} // class


