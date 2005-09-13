// IntraProc.java, created Tue Jul  5 12:45:35 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import java.io.PrintWriter;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HMember;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;

import harpoon.Temp.Temp;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadVisitor;

import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.FOOTER;

import jpaul.Constraints.Var;
import jpaul.Constraints.ConstraintSystem;
import jpaul.Constraints.SolAccessor;
import jpaul.Constraints.SolReader;
import jpaul.Constraints.Constraint;
import jpaul.Constraints.LtConstraint;
import jpaul.Constraints.CtConstraint;

import jpaul.Graphs.DiGraph;

import jpaul.DataStructs.SetFactory;
import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;

import jpaul.Misc.MCell;

import net.cscott.jutil.DisjointSet;

import harpoon.Util.Util;

import harpoon.Analysis.PA2.Mutation.MutationAnalysis;

/**
 * <code>IntraProc</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: IntraProc.java,v 1.6 2005-09-13 19:26:28 salcianu Exp $
 */
public class IntraProc {
    
    public IntraProc(HMethod hm, boolean flowSensitivity, PointerAnalysis pa) {
	this.hm  = hm;
	this.hcf = pa.getCodeFactory();
	this.nodeRep = pa.getNodeRep();
	this.flowSensitivity  = flowSensitivity;
	this.interProcConsGen = new InterProcConsGen(this, pa);
	
	HCode hcode = hcf.convert(hm);
	footer = getFooter(hcode);
	
	if(Flags.SHOW_INTRA_PROC_CONSTRAINTS) {
	    System.out.println("Pointer analysis constraints for \"" + hm + "\"");
	}

	createVars(hcode);
	
	Collection<Constraint> cons = new LinkedList<Constraint>();
	
	for(Object hce : hcode.getElementsL()) {
	    Quad q = (Quad) hce;
	    
	    newCons.clear();
	    generateIncomingCons(q);
	    cons.addAll(newCons);
	    
	    if(Flags.SHOW_INTRA_PROC_CONSTRAINTS) {
		System.out.println("\nQuad: " + Util.code2str(q));
		System.out.println("  Incoming cons: " + newCons);
	    }
	    
	    newCons.clear();
	    q.accept(consVis);
	    addDefaultCons(q);
	    cons.addAll(newCons);
	    
	    if(Flags.SHOW_INTRA_PROC_CONSTRAINTS) {
		System.out.println("  Other cons: " + newCons);
	    }
	}
	
	this.cs = new ConstraintSystem(cons);
    }

    private final InterProcConsGen interProcConsGen;

    private final HMethod hm;
    private final FOOTER footer;

    private FOOTER getFooter(HCode hcode) {
	assert hcode != null : " hm = " + hm;
	HCodeElement[] leaves = hcode.getLeafElements();
	for(HCodeElement hce : leaves) {
	    if(hce instanceof FOOTER) {
		return (FOOTER) hce;
	    }
	}
	assert false : "no FOOTER found ...";
	return null; // should never happen
    }

    InterProcAnalysisResult solve() {
	return GraphOptimizations.trimUnreachable(fullSolve(), nodeRep.getParamNodes(hm));
    }


    public FullAnalysisResult fullSolve() {
	final SolReader sr = this.cs.solve();

	final PAEdgeSet eomI = PAUtil.fixNullM((PAEdgeSet) sr.get(preIVar(footer)));
	final PAEdgeSet eomO = PAUtil.fixNullM((PAEdgeSet) sr.get(O));
	Set<PANode> preEomDirGblEsc = PAUtil.fixNullM((Set<PANode>) sr.get(eVar()));

	final Set<PANode> eomAllGblEsc = 
	    DSFactories.nodeSetFactory.create
	    (DiGraph.union(eomI, eomO).
	     transitiveSucc(DSUtil.iterable2coll
			    (DSUtil.unionIterable
			     (preEomDirGblEsc,
			      Collections.<PANode>singleton(nodeRep.getGlobalNode())))));

	final Set<PANode> eomDirGblEsc = DSFactories.nodeSetFactory.create(eomAllGblEsc);
	//System.out.println("eVar() = " + eVar() + " has value " + eomDirGblEsc);

	FullAnalysisResult far = new FullAnalysisResult() {
	    public PAEdgeSet preI(Quad q)  {
		return PAUtil.fixNullM((PAEdgeSet) sr.get(preIVar(q)));
	    }
	    public PAEdgeSet postI(Quad q) { 
		return PAUtil.fixNullM((PAEdgeSet) sr.get(postIVar(q)));
	    }
	    
	    public PAEdgeSet eomI() { return eomI; }
	    public PAEdgeSet eomO() { return eomO; }

	    public Set<PANode> preEsc(Quad q) { 
		return PAUtil.fixNullM((Set<PANode>) sr.get(preFVar(q)));
	    }
	    public Set<PANode> postEsc(Quad q) { 
		return PAUtil.fixNullM((Set<PANode>) sr.get(postFVar(q)));
	    }

	    public Set<PANode> eomDirGblEsc() { return eomDirGblEsc; }
	    public Set<PANode> eomAllGblEsc() { return eomAllGblEsc; }

	    public Set<PANode> lv(Temp t)  { 
		return PAUtil.fixNullM((Set<PANode>) sr.get(lVar(t)));
	    }

	    public Set<PANode> ret() {
		return PAUtil.fixNullM((Set<PANode>) sr.get(vRet));
	    }
	    public Set<PANode> ex()  {
		return PAUtil.fixNullM((Set<PANode>) sr.get(vEx));
	    }

	    public Set<Pair<PANode,HField>> eomWrites() {
		Set<Pair<PANode,HField>> writes = (Set<Pair<PANode,HField>>) sr.get(vWrites());

		return 
		(writes == null) ?
		Collections.<Pair<PANode,HField>>emptySet() :
		writes;
	    }

	};

	if(Flags.SHOW_INTRA_PROC_RESULTS)
	    debug(new PrintWriter(System.out), far);

	return far;
    }
    
    private final HCodeFactory     hcf;
    private final NodeRepository   nodeRep;
    private final boolean          flowSensitivity;

    public  final ConstraintSystem cs;

    NodeRepository getNodeRep() { return nodeRep; }
    Linker getLinker() { return hm.getDeclaringClass().getLinker(); }
    
    private Map<Quad,IVar> quad2preIVar;
    private Map<Quad,IVar> quad2postIVar;
    private IVar FII;
    
    IVar preIVar(Quad q)  {
	if(flowSensitivity) {
	    return quad2preIVar.get(q);
	}
	else {
	    return FII;
	}
    }
    IVar postIVar(Quad q) { 
	if(flowSensitivity) {
	    return quad2postIVar.get(q); 
	}
	else {
	    return FII;
	}
    }


    private final Map<Temp,LVar> temp2LVar = new HashMap<Temp,LVar>();
    
    LVar lVar(Temp t)  { return temp2LVar.get(t); }


    // F variables store sets of nodes that escape from the analyzed method
    // (eager computation of the escape info, as opposed to on-demand)
    private Map<Quad,FVar> quad2preFVar  = new HashMap<Quad,FVar>();
    private Map<Quad,FVar> quad2postFVar = new HashMap<Quad,FVar>();
    private FVar FIF;

    FVar preFVar(Quad q)  { 
	if(flowSensitivity) {
	    return quad2preFVar.get(q); 
	}
	else {
	    return FIF;
	}
    }
    FVar postFVar(Quad q) { 
	if(flowSensitivity) {
	    return quad2postFVar.get(q); 
	}
	else {
	    return FIF;
	}
    }

    private OVar O = new OVar();
    private EVar E = new EVar();
    private LVar vRet = new LVar();
    private LVar vEx  = new LVar();

    OVar oVar() { assert O != null; return O; }
    EVar eVar() { assert E != null; return E; }


    private WVar vWrites = new WVar();
    WVar vWrites() { return vWrites; }


    private void createVars(HCode hcode) {
	if(flowSensitivity) {
	    quad2preIVar  = new HashMap<Quad,IVar>();
	    quad2postIVar = new HashMap<Quad,IVar>();
	    quad2preFVar  = new HashMap<Quad,FVar>();
	    quad2postFVar = new HashMap<Quad,FVar>();
	}
	else {
	    FII = new IVar();
	    FIF = new FVar();
	}

	for(Object hce : hcode.getElementsL()) {
	    Quad q = (Quad) hce;

	    for(Temp t : q.def()) {
		temp2LVar.put(t, new LVar());
	    }

	    if(flowSensitivity) {
		quad2preIVar.put(q, new IVar());
		quad2postIVar.put(q, new IVar());
		quad2preFVar.put(q, new FVar());
		quad2postFVar.put(q, new FVar());
	    }
	}
    }


    private final Collection<Constraint> newCons = new LinkedList<Constraint>();

    private void generateIncomingCons(Quad q) {
	for(Quad prev : q.prev()) {
	    newCons.add(new LtConstraint(postIVar(prev), preIVar(q)));
	    newCons.add(new LtConstraint(postFVar(prev), preFVar(q)));
	}
    }


    private QuadVisitor consVis = new QuadVisitor() {

	public void visit(PHI q) {
	    for(int i = 0; i < q.numPhis(); i++) {
		Var dst = lVar(q.dst(i));
		for(int j = 0; j < q.arity(); j++) {
		    Var src = lVar(q.src(i,j));
		    newCons.add(new LtConstraint(src, dst));
		}
	    }
	}

	public void visit(METHOD q) {
	    Temp[]  params = q.def();

	    List<HClass> pTypes = PAUtil.getParamTypes(hm);
	    assert params.length == pTypes.size();

	    Collection<PANode> pNodes = nodeRep.createParamNodes(hm, pTypes);

	    int k = 0;
	    for(PANode pnode : pNodes) {
		if(pnode != null) {
		    newCons.add(new CtConstraint(Collections.<PANode>singleton(pnode),
						 lVar(params[k])));
		}
		k++;
	    }
	}

	public void visit(NEW q) {
	    PANode node = nodeRep.getInsideNode(q, q.hclass());
	    newCons.add(new CtConstraint(Collections.singleton(node),
					 lVar(q.dst())));
	}
	    
	public void visit(ANEW q) {
	    PANode node = nodeRep.getInsideNode(q, q.hclass());
	    newCons.add(new CtConstraint(Collections.singleton(node),
					 lVar(q.dst())));
	}
	    
	public void visit(MOVE q) {
	    Var vs = lVar(q.src());
	    Var vd = lVar(q.dst());
	    newCons.add(new LtConstraint(vs, vd));
	}

	public void visit(SET q) {
	    if(Flags.RECORD_WRITES) {
		recordWrites(q.objectref(), PAUtil.getUniqueField(q.field()));
	    }
	    
	    // assignments to primitive fields (e.g., ints) are irrelevant for the pointer analysis
	    if(q.field().getType().isPrimitive()) return;

	    HField hf = PAUtil.getUniqueField(q.field());

	    if(q.isStatic()) {
		processStaticStore(q, q.src(), hf);
	    }
	    else {
		processStore(q, q.objectref(), q.src(), hf);
	    }
	}

	private void recordWrites(Temp dst, HField hf) {
	    LVar vd = (dst == null) ? null : lVar(dst);
	    if((vd != null) && (hf.getDeclaringClass() != null)) {
		// for instance fields, filter out the nodes whose
		// type prevent them from having hf as a field
		LVar vdFiltered = new LVar();
		newCons.add(new TypeFilterConstraint(vd, hf.getDeclaringClass(), vdFiltered));
		vd = vdFiltered;
	    }
	    newCons.add(new WriteConstraint(vd, hf, vWrites()));
	}


	public void visit(ASET q) {
	    if(Flags.RECORD_WRITES) {
		recordWrites(q.src(),
			     PAUtil.getArrayField(PAUtil.getLinker(q)));
	    }
	    // arrays of primitives (e.g., ints) are irrelevant
	    if(!q.type().isPrimitive()) {
		Linker linker = PAUtil.getLinker(q);
		processStore(q, q.objectref(), q.src(),
			     PAUtil.getArrayField(linker));
	    }
	}

	private void processStaticStore(Quad q, Temp vd, HField hf) {
	    newCons.add(new LtConstraint(lVar(vd), eVar()));
	    newCons.add(new LtConstraint(preFVar(q), postFVar(q)));

	    // preFVar(q) should be a special input: dependencies, but no re-computation
	    newCons.add(new StaticStoreConstraintF(lVar(vd), preIVar(q), preFVar(q), postFVar(q)));
	}

	private void processStore(Quad q, Temp vs, Temp vd, HField hf) {
	    assert (vs != null) && (vd != null);
	    IntraProc.addStoreConstraints(q, lVar(vs), hf, lVar(vd), IntraProc.this, newCons);
	}

	public void visit(GET q) {
	    // assignments to primitive fields (e.g., ints) are irrelevant
	    if(q.field().getType().isPrimitive()) return;

	    HField hf = PAUtil.getUniqueField(q.field());

	    if(q.isStatic()) {
		processStaticLoad(q, q.dst(), hf);
	    }
	    else {
		processLoad(q, q.dst(), q.objectref(), hf);
	    }
	}

	public void visit(AGET q) {
	    if(!q.type().isPrimitive()) {
		Linker linker = q.getFactory().getMethod().getDeclaringClass().getLinker();
		processLoad(q, q.dst(), q.objectref(), PAUtil.getArrayField(linker));
	    }
	}

	private void processStaticLoad(Quad q, Temp vd, HField hf) {
	    newCons.add(new CtConstraint(Collections.<PANode>singleton(nodeRep.getGlobalNode()),
					 lVar(vd)));
	}

	private void processLoad(Quad q, Temp vd, Temp vs, HField hf) {
	    IntraProc.addLoadConstraints(q, lVar(vd), lVar(vs), hf, IntraProc.this, newCons);
	}

	public void visit(RETURN q) {
	    if(q.retval() != null) {
		newCons.add(new LtConstraint(lVar(q.retval()), vRet));
	    }
	}

	public void visit(THROW q) {
	    if(q.throwable() != null) {
		newCons.add(new LtConstraint(lVar(q.throwable()), vEx));
	    }
	}


	public void visit(CALL q) {
	    newCons.addAll(interProcConsGen.treatCALL(q));
	}

	private Collection<Var> trim(Collection<Var> coll) {
	    Collection<Var> res = new LinkedList<Var>();
	    for(Var v : coll) {
		if(v != null) res.add(v);
	    }
	    return res;
	}

	public void visit(Quad q) { }  // do nothing
    };


    // Flow sensitive (post) vars that are not assigned by the
    // newly generated constraints are assumed to propagate
    // directly from the input.  Introduce constraints to enforce
    // this.
    private void addDefaultCons(Quad q) {
	Collection<Var> out = new HashSet<Var>();
	for(Constraint c : newCons) {
	    out.addAll(c.out());
	}
	addDefaultCons(preIVar(q), postIVar(q), out);
	//addDefaultCons(preE(q), postE(q), out);
	addDefaultCons(preFVar(q), postFVar(q), out);
    }

    private void addDefaultCons(Var pre, Var post, Collection<Var> out) {
	if(!out.contains(post))
	    newCons.add(new LtConstraint(pre, post));
    }

    /*
      // cheesy constraint that reflects only the dependencies, without any action
      private class TestConstraint implements Constraint {
      public TestConstraint(Collection<Var> in, Collection<Var> out) {
      this.in = in;
      this.out = out;
      }
      private final Collection<Var> in;
      private final Collection<Var> out;

      public Collection<Var> in()  { return in; }
      public Collection<Var> out() { return out; }
	    
      public void action(SolAccessor sa) { }
	    
      public Constraint rewrite(DisjointSet uf) {
      Collection<Var> newIn  = new LinkedList<Var>();
      Collection<Var> newOut = new LinkedList<Var>();
      for(Var v : in) {
      newIn.add((Var) uf.find(v));
      }
      for(Var v : out) {
      newOut.add((Var) uf.find(v));
      }
      return new TestConstraint(newIn, newOut);
      }

      public String toString() {
      return in.toString() + " -> " + out.toString();
      }
      }
    */
    

    private void debug(final PrintWriter pw, final FullAnalysisResult far) {
	uf = cs.debugGetVarUnification();
	pw.println("IntraProc analysis results for \"" + hm + "\"");
	final HCode hcode = hcf.convert(hm);

	final MCell<EdgeSetVar> lastIVar = new MCell<EdgeSetVar>(null);
	final MCell<NodeSetVar> lastFVar = new MCell<NodeSetVar>(null);

	hcode.print
	    (pw,
	     new HCode.PrintCallback() {
		private boolean printedBefore = false;
		public void printBefore(PrintWriter pw, HCodeElement hce) {
		    Quad q = (Quad) hce;
		    if(q.prevLength() <= 1) return;
		    if(printedBefore && !far.preEsc(q).isEmpty() && !far.preI(q).isEmpty())
			pw.println();
		    if(flowSensitivity) {
			printSet(pw, "  preEsc = ", preFVar(q), far.preEsc(q), lastFVar);
			printEdges(pw, preIVar(q), far.preI(q), "  ", lastIVar);
		    }		    
		}
		public void printAfter(PrintWriter pw, HCodeElement hce) {
		    Quad q = (Quad) hce;
		    printedBefore = false;
		    for(Temp t : q.def()) {
			printedBefore |= printSet(pw, "  " + t + " --> ", null, far.lv(t), null);
		    }
		    if(flowSensitivity) {
			printedBefore |= printSet(pw, "  postEsc = ", postFVar(q), far.postEsc(q), lastFVar);
			printedBefore |= printEdges(pw, postIVar(q), far.postI(q), "  ", lastIVar);
		    }
		}
	    });

	// For flow-insensitive analysis, it is pointless to print the result after each instruction;
	// Instead, we print it once, at the end of the method.
	if(!flowSensitivity) {
	    printSet(pw, "(Flow-insensitive) Esc = ", null, far.postEsc(footer), null);
	    printEdges(pw, "(Flow-insensitive) set of inside edges:", null, far.postI(footer), "", null);
	}

	printEdges(pw, "Full (flow-insensitive) set of outside edges:", null, far.eomO(), "O: ", null);
	printSet(pw, "returned nodes  = ", null, far.ret(), null);
	printSet(pw, "thrown nodes    = ", null, far.ex(), null);
	printSet(pw, "DirGblEsc nodes = ", null, far.eomAllGblEsc(), null);
	printSet(pw, "AllGblEsc nodes = ", null, far.eomAllGblEsc(), null);

	if(Flags.RECORD_WRITES) {
	    System.out.println("Mutated abstract nodes:" + far.eomWrites());
	}

	pw.println();
	pw.flush();
	uf = null;
    }

    private Map<Var,Var> uf;

    private boolean printEdges(PrintWriter pw, EdgeSetVar v, PAEdgeSet edges, String indent,
			       MCell<EdgeSetVar> lastPrintedVar) {
	return printEdges(pw, null, v, edges, indent, lastPrintedVar);
    }

    private boolean printEdges(PrintWriter pw, String preText, EdgeSetVar v, PAEdgeSet edges, String indent,
			       MCell<EdgeSetVar> lastPrintedVar) {
	if((v != null) && (lastPrintedVar != null)) {
	    EdgeSetVar reprOfV = uf.containsKey(v) ? ((EdgeSetVar) uf.get(v)) : v;
	    if((lastPrintedVar.value != null) && (lastPrintedVar.value.equals(reprOfV)))
		return false;
	    lastPrintedVar.value = reprOfV;
	}

	if(!edges.isEmpty()) {
	    if(preText != null) pw.println(preText);
	    edges.print(pw, indent);
	    pw.println();
	    return true;
	}
	return false;
    }

    private boolean printSet(PrintWriter pw, String preText, NodeSetVar v, Set set, MCell<NodeSetVar> lastPrintedFVar) {
	if((v != null) && (lastPrintedFVar != null)) {
	    NodeSetVar reprOfV = uf.containsKey(v) ? ((NodeSetVar) uf.get(v)) : v;
	    if((lastPrintedFVar.value != null) && (lastPrintedFVar.value.equals(reprOfV)))
		return false;
	    lastPrintedFVar.value = reprOfV;
	}

	if(!set.isEmpty()) {
	    pw.print(preText);
	    pw.println(set);
	    return true;
	}
	return false;
    }


    static void addLoadConstraints(Quad q, LVar vd, LVar vs, HField hf,
				   IntraProc intraProc,
				   Collection<Constraint> newCons) {
	newCons.add(new LoadConstraint1(vd, vs, hf, intraProc.preIVar(q)));
	newCons.add(new LoadConstraint2(vd, vs, hf,
					intraProc.preFVar(q),
					intraProc.oVar(),
					q,
					intraProc.getNodeRep()));
    }

    static void addStoreConstraints(Quad q, LVar vs, HField hf, LVar vd,
				    IntraProc intraProc,
				    Collection<Constraint> newCons) {
	newCons.add(new LtConstraint(intraProc.preFVar(q),
				     intraProc.postFVar(q)));
	newCons.add(new LtConstraint(intraProc.preIVar(q),
				     intraProc.postIVar(q)));
	newCons.add(new StoreConstraintF(vs, vd,
					 intraProc.preIVar(q),
					 intraProc.preFVar(q),
					 intraProc.postFVar(q)));
	newCons.add(new StoreConstraint(vs, hf, vd,
					intraProc.postIVar(q)));
    }

}
