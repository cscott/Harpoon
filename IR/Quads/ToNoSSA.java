// ToNoSSA.java, created Wed Feb  3 16:18:56 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Analysis.Maps.TypeMap.TypeNotKnownException;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.LowQuad.PCALL;
import harpoon.IR.LowQuad.LowQuadFactory;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.Quads.Edge;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import harpoon.Util.Default;
import harpoon.Util.DisjointSet;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
 
/**
 * The <code>ToNoSSA</code> class implements the translation between SSA 
 * and No-SSA form.  
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ToNoSSA.java,v 1.1.2.38 2001-09-25 22:57:56 cananian Exp $
 */
public class ToNoSSA
{
    private NameMap         m_ctm;
    private Derivation      m_derivation;
    private Quad            m_quads;
    private AllocationInformationMap m_allocInfoMap;
    
    private static interface SerializableDerivation
	extends Derivation, java.io.Serializable { /* declare only */ }

    private static class NullDerivation implements SerializableDerivation {
	public DList derivation(HCodeElement hce, Temp t) { 
	    Util.assert(hce!=null && t!=null);
	    throw new TypeNotKnownException(hce, t);
	}
	public HClass typeMap(HCodeElement hce, Temp t) { 
	    Util.assert(hce!=null && t!=null); 
	    throw new TypeNotKnownException(hce, t);
	} 
    }
    private static class MapDerivation implements SerializableDerivation {
	final Map dT;
	MapDerivation(Map dT) { this.dT = dT; }
	public DList derivation(HCodeElement hce, Temp t) {
	    Util.assert(hce!=null && t!=null);
	    Util.assert(t.tempFactory() ==
			((Quad)hce).getFactory().tempFactory());
	    Object type = dT.get(Default.pair(hce, t));
	    if (type instanceof HClass) return null;
	    if (type instanceof DList) return (DList) type;
	    throw new TypeNotKnownException(hce, t);
	}
	public HClass typeMap(HCodeElement hce, Temp t) {
	    Util.assert(hce!=null && t!=null);
	    Util.assert(t.tempFactory() ==
			((Quad)hce).getFactory().tempFactory());
	    Object type = dT.get(Default.pair(hce, t));
	    if (type instanceof HClass) return (HClass)type;
	    if (type instanceof DList) return null;
	    throw new TypeNotKnownException(hce, t);
	}
    }

    public ToNoSSA(QuadFactory newQF, Code code)
    {
	this(newQF, code, null, false);
    }
    public ToNoSSA(QuadFactory newQF, Code code, final TypeMap typeMap)
    {
	this(newQF, code, typeMap==null ? null : new SerializableDerivation() {
	    public HClass typeMap(HCodeElement hce, Temp t) {
		// proxy given typeMap
		return typeMap.typeMap(hce, t);
	    }
	    public DList derivation(HCodeElement hce, Temp t) {
		// return null (indicating base pointer) iff typeMap is okay.
		if (typeMap(hce,t)!=null) return null;
		throw new TypeNotKnownException(hce, t);
	    }
	}, typeMap!=null);
    }
    public ToNoSSA(QuadFactory newQF, Code code, Derivation derivation) {
	this(newQF, code, derivation, derivation!=null);
    }
    private ToNoSSA(QuadFactory newQF, Code code, Derivation derivation,
		    boolean validDerivation) {
	Util.assert(code.getName().equals(harpoon.IR.Quads.QuadSSI.codename) ||
		    code.getName().equals(harpoon.IR.LowQuad.LowQuadSSI.codename) ||
		    code.getName().equals(harpoon.IR.Quads.QuadWithTry.codename));
    
	final Map dT = validDerivation ? new HashMap() : null;

	m_allocInfoMap = (code.getAllocationInformation()==null) ? null :
	    new AllocationInformationMap();
	m_ctm   = new NameMap
	    (((Quad)code.getRootElement()).getFactory().tempFactory(),
	     newQF.tempFactory());
	m_quads = translate(newQF, derivation, dT, code);
	m_derivation = validDerivation
	    ? (Derivation) new MapDerivation(dT) 
	    : (Derivation) new NullDerivation();
    }

    public Quad getQuads()        { return m_quads; }
    public Derivation getDerivation() { return m_derivation; }
    public AllocationInformation getAllocationInformation() {
	return m_allocInfoMap;
    }

    /**
     * Translates the code in the supplied codeview from SSx to No-SSA form, 
     * returning the new root <code>Quad</code>.  The <code>Map</code>
     * parameter is used to maintain derivation information.
     * 
     * @param qf    the <code>QuadFactory</code> which will manufacture the
     *                  translated <code>Quad</code>s.
     * @param dT    a <code>Map</code> in which to place the updated
     *                  derivation information.  
     * @param code  the codeview containing the <code>Quad</code>s to 
     *                  translate.
     */
    private Quad translate(QuadFactory qf, Derivation derivation, 
			   Map dT, Code code)
    {
	Quad            old_header, qTmp;
	QuadMap         qm;
	LowQuadVisitor  v;

	old_header   = (Quad)code.getRootElement();
	qm           = new QuadMap(m_ctm, derivation, dT);

	// Merge src/dst of all SIGMAS
	v = new SIGMAMergeVisitor(m_ctm);
	for (Iterator i = code.getElementsI(); i.hasNext();)
	    ((Quad)i.next()).accept(v);

	// Remove all SIGMAs from the code
	v = new SIGMAVisitor(m_ctm, qf, qm);
	for (Iterator i = code.getElementsI(); i.hasNext();)
	    ((Quad)i.next()).accept(v);
      
	// note that there's no need to rename variables to account for
	// removed sigmas, as the variables split by a sigma simply
	// revert to their original type & definition point above the sigma.
	// (and this *is* SSA/SSI form we're converting from: no redefinitions
	// of SIGMA'ed variables elsewhere).

	// Connect the edges of these new Quads
	for (Iterator i = code.getElementsI(); i.hasNext();) {
	    qTmp       = (Quad)i.next();
	    Edge[] el  = qTmp.nextEdge();   
	    for (int n=0; n<el.length; n++)
		Quad.addEdge(qm.get((Quad)el[n].from()),
			     el[n].which_succ(),
			     qm.get((Quad)el[n].to()),
			     el[n].which_pred());
	}

	// Modify this new CFG by emptying PHI nodes
	v = new PHIVisitor(qf, dT);
	for (Iterator i = code.getElementsI(); i.hasNext();)
	    qm.get((Quad)i.next()).accept(v);

	// Update AllocationInformation
	if (m_allocInfoMap!=null) {
	    AllocationInformation oldai = code.getAllocationInformation();
	    for (Iterator it = code.getElementsI(); it.hasNext(); ) {
		Quad oldquad = (Quad) it.next();
		Quad newquad = qm.get(oldquad);
		if (oldquad instanceof ANEW || oldquad instanceof NEW)
		    m_allocInfoMap.transfer(newquad, oldquad, m_ctm, oldai);
	    }
	}

	// Return the head of the new CFG
	return qm.get(old_header);
    }

    /** first touch all the SIGMAs to merge their definitions */
static class SIGMAMergeVisitor extends LowQuadVisitor // this is an inner class
{
    private final NameMap        m_nm;
    public SIGMAMergeVisitor(NameMap nm) {
	super(false/*non-strict*/);
	m_nm = nm;
    }
    public void visit(Quad q) { /* do nothing */ }
    public void visit(SIGMA q) {
	for (int i=0; i<q.numSigmas(); i++)
	    for (int j=0; j<q.arity(); j++)
		m_nm.map(q.dst(i, j), q.src(i));
    }
}
/*
 * Performs the first phase of the transformation to NoSSA form:
 * removing the SIGMAs.  This visitor also serves the purpose of cloning
 * the quads in the codeview, which is necessary because the PHIVisitor
 * will actually modify these quads, and we do not want these effects
 * to propagate outside of the ToNoSSA class.
 */
static class SIGMAVisitor extends LowQuadVisitor // this is an inner class
{
    private CloningTempMap m_ctm;
    private QuadFactory    m_qf;
    private QuadMap        m_qm;

    public SIGMAVisitor(CloningTempMap ctm,
			QuadFactory qf, QuadMap qm)
    {
	super(false/*non-strict*/);
	m_ctm         = ctm;
	m_qf          = qf;
	m_qm          = qm;
    }
  
    public void visit(Quad q)
    {
	Quad qm = (Quad)(q.clone(m_qf, m_ctm));
	m_qm.put(q, qm); 
    }
  
    public void visit(CALL q)
    {
	SIGMA  q0;
	int    arity, numSigmas;
	Temp[] nparams;
      
	nparams    = new Temp[q.paramsLength()];
	for (int i=0; i<nparams.length; i++)
	    nparams[i] = map(q.params(i));
	q0         = new CALL(m_qf, q, q.method(), nparams,
			      (q.retval()!=null)?map(q.retval()):null,
			      map(q.retex()), q.isVirtual(), q.isTailCall(),
			      new Temp[0]);
	m_qm.put(q, q0);
    }

    public void visit(PCALL q) // handle low-quads, too.
    {
	SIGMA  q0;
	int    arity, numSigmas;
	Temp[] nparams;
      
	nparams    = new Temp[q.paramsLength()];
	for (int i=0; i<nparams.length; i++)
	    nparams[i] = map(q.params(i));
	q0         = new PCALL((LowQuadFactory)m_qf, q, map(q.ptr()), nparams,
			      (q.retval()!=null)?map(q.retval()):null,
			      map(q.retex()), new Temp[0],
			      q.isVirtual(), q.isTailCall());
	m_qm.put(q, q0);
    }

    public void visit(CJMP q)
    {
	SIGMA  q0;
	int    arity, numSigmas;
      
	q0         = new CJMP(m_qf, q, map(q.test()), new Temp[] {});
	m_qm.put(q, q0);
    }

    public void visit(SWITCH q)
    {
	SIGMA  q0;
	int    arity, numSigmas;
	int[]  keys;
      
	keys       = new int[q.keysLength()];
	System.arraycopy(q.keys(), 0, keys, 0, q.keysLength());
	q0         = new SWITCH(m_qf, q, map(q.index()), keys, new Temp[] {});
	m_qm.put(q, q0);
    }

    public void visit(TYPESWITCH q)
    {
	SIGMA  q0;
	int    arity, numSigmas;
      
	q0         = new TYPESWITCH(m_qf, q, map(q.index()), q.keys(),
				    new Temp[] {}, q.hasDefault());
	m_qm.put(q, q0);
    }
  
    private Temp map(Temp t) { return (t==null)?null:m_ctm.tempMap(t); }  
}

/**
 * Performs the second phase of the transformation to NoSSA form:
 * the removal of the PHI nodes.  This is done by actually modifying
 * the CFG directly, so it is advisable to use this visitor only
 * on a clone of the actual CFG you wish to translate.  
 */
static class PHIVisitor extends LowQuadVisitor // this is an inner class
{
    private Map             m_dT;
    private QuadFactory     m_qf;

    public PHIVisitor(QuadFactory qf, Map dT)
    {      
	super(false/*non-strict*/);
	m_dT          = dT;
	m_qf          = qf;
    }

    public void visit(Quad q) { }

    public void visit(LABEL q)
    {
	pushBack(q);
	Quad.replace(q, new LABEL(m_qf, q, q.label(), new Temp[0], q.arity()));
    }
      
    public void visit(PHI q)
    {
	pushBack(q);
	Quad.replace(q, new PHI(q.getFactory(), q, new Temp[0], q.arity()));
    }

    private static Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
    private static Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	Quad frm = (Quad) e.from(); int frm_succ = e.which_succ();
	Quad to  = (Quad) e.to();   int to_pred = e.which_pred();
	Quad.addEdge(frm, frm_succ, q, which_pred);
	Quad.addEdge(q, which_succ, to, to_pred);
	return to.prevEdge(to_pred);
    }
    private Edge addMoveAt(Edge e, HCodeElement source, Temp dst, Temp src,
				  Object type) {
	MOVE m = new MOVE(m_qf, source, dst, src);
	if (type!=null) m_dT.put(Default.pair(m, dst), type);
	return addAt(e, m);
    }
    // insert MOVE on edge into PHI & update derivation table.
    private void pushBack(PHI q) {
	Edge[] in = q.prevEdge();
	Edge out = q.nextEdge(0);
	for (int i=0; i<q.numPhis(); i++) {
	    Temp Tdst = q.dst(i);
	    Temp Tt = new Temp(Tdst);
	    Object type = null;
	    if (m_dT!=null) {
		type = m_dT.remove(Default.pair(q, Tdst));
		Util.assert(type!=null, "No type for "+Tdst+" in "+q);
	    }
	    for (int j=0; j<in.length; j++)
		in[j] = addMoveAt(in[j], q, Tt, q.src(i, j), type);
	    out = addMoveAt(out, q, Tdst, Tt, type);
	}
    }
}


static class QuadMap // this is an inner class
{
    private CloningTempMap  m_ctm;
    private Derivation      m_derivation;
    private Map             m_dT;

    final private Map h = new HashMap();

    QuadMap(CloningTempMap ctm, Derivation derivation, Map dT)
    {
	m_ctm         = ctm;
	m_derivation  = derivation;
	m_dT          = dT;
    }

    boolean contains(Quad old)  { return h.containsKey(old); }  

    Quad get(Quad old) { 
	return (Quad)h.get(old);
    }

    void put(Quad qOld, Quad qNew)
    {
	h.put(qOld, qNew);
	if (m_dT!=null) updateDTInfo(qOld, qNew);
    }
  
    /* UTILITY METHODS FOLLOW */

    private Temp map(Temp t) 
    { return (t==null)?null:m_ctm.tempMap(t); }  
      
    private void updateDTInfo(Quad qOld, Quad qNew)
    {
	Util.assert(qOld!=null && qNew != null);

	Temp[] defs = qOld.def();
	for (int i=0; i<defs.length; i++) {
	    List tuple = Default.pair( qNew, map(defs[i]) );
	    HClass hc = m_derivation.typeMap(qOld, defs[i]);
	    if (hc!=null) { // not a derived pointer.
		m_dT.put(tuple, hc);
		continue;
	    } // else, is a derived pointer.
	    DList dl = m_derivation.derivation(qOld, defs[i]);
	    Util.assert(dl!=null, 
			"No type information for "+defs[i]+" in "+qOld);
	    m_dT.put(tuple, dl);
	}
    }
}

static class NameMap extends CloningTempMap {
    private final TempFactory old_tf;
    NameMap(TempFactory old_tf, TempFactory new_tf) {
	super(old_tf, new_tf);
	this.old_tf = old_tf;
    }
    private DisjointSet mergeMap = new DisjointSet();
    private boolean merging=true;
    public Temp tempMap(Temp t) {
	if (merging) merging=false;
	return super.tempMap((Temp)mergeMap.find(t));
    }
    public void map(Temp Told, Temp Tnew) {
	Util.assert(merging); // no merging is valid after we start tempMapping
	Util.assert(Told.tempFactory()==old_tf);
	Util.assert(Tnew.tempFactory()==old_tf);
	mergeMap.union(Told, Tnew);
    }
}

} // close the ToNoSSA class (yes, the indentation's screwed up,
  // but I don't feel like re-indenting all this code) [CSA]
