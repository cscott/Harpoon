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
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
 
/**
 * The <code>ToNoSSA</code> class implements the translation between SSA 
 * and No-SSA form.  
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ToNoSSA.java,v 1.1.2.36 2000-11-16 00:12:17 cananian Exp $
 */
public class ToNoSSA
{
    private CloningTempMap  m_ctm;
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
	    Object type = dT.get(new Tuple(new Object[] { hce, t }));
	    if (type instanceof HClass) return null;
	    if (type instanceof DList) return (DList) type;
	    throw new TypeNotKnownException(hce, t);
	}
	public HClass typeMap(HCodeElement hce, Temp t) {
	    Util.assert(hce!=null && t!=null);
	    Util.assert(t.tempFactory() ==
			((Quad)hce).getFactory().tempFactory());
	    Object type = dT.get(new Tuple(new Object[] { hce, t }));
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
	m_ctm   = new CloningTempMap
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
     * Translates the code in the supplied codeview from SSA to No-SSA form, 
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
	NameMap         nm;
	Quad            old_header, qTmp;
	QuadMap         qm;
	LowQuadVisitor  v;

	old_header   = (Quad)code.getRootElement();
	nm           = new NameMap(); 
	qm           = new QuadMap(m_ctm, derivation, dT);

	// Remove all SIGMAs from the code
	v = new SIGMAVisitor(m_ctm, nm, qf, qm);
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
	v = new PHIVisitor(qf, dT, nm);
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
    private NameMap        m_nm;
    private QuadFactory    m_qf;
    private QuadMap        m_qm;

    public SIGMAVisitor(CloningTempMap ctm, NameMap nm, 
			QuadFactory qf, QuadMap qm)
    {
	super(false/*non-strict*/);
	m_ctm         = ctm;
	m_nm          = nm;
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
	numSigmas  = q.numSigmas();
	arity      = q.arity();

	for (int i=0; i<numSigmas; i++)
	    for (int j=0; j<arity; j++)
		renameSigma(q, j, i);

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
	numSigmas  = q.numSigmas();
	arity      = q.arity();

	for (int i=0; i<numSigmas; i++)
	    for (int j=0; j<arity; j++)
		renameSigma(q, j, i);

	m_qm.put(q, q0);
    }

    public void visit(CJMP q)
    {
	SIGMA  q0;
	int    arity, numSigmas;
      
	q0         = new CJMP(m_qf, q, map(q.test()), new Temp[] {});
	numSigmas  = q.numSigmas();
	arity      = q.arity();

	for (int i=0; i<numSigmas; i++)
	    for (int j=0; j<arity; j++)
		renameSigma(q, j, i);

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
	numSigmas  = q.numSigmas();
	arity      = q.arity();

	for (int i=0; i<numSigmas; i++)
	    for (int j=0; j<arity; j++)
		renameSigma(q, j, i);

	m_qm.put(q, q0);
    }

    public void visit(TYPESWITCH q)
    {
	SIGMA  q0;
	int    arity, numSigmas;
      
	q0         = new TYPESWITCH(m_qf, q, map(q.index()), q.keys(),
				    new Temp[] {}, q.hasDefault());
	numSigmas  = q.numSigmas();
	arity      = q.arity();

	for (int i=0; i<numSigmas; i++)
	    for (int j=0; j<arity; j++)
		renameSigma(q, j, i);

	m_qm.put(q, q0);
    }
  
    private Temp map(Temp t) { return (t==null)?null:m_ctm.tempMap(t); }  

    private void renameSigma(SIGMA q, int dstIndex, int srcIndex) 
    { m_nm.map(map(q.dst(srcIndex, dstIndex)), map(q.src(srcIndex))); }
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
    private NameMap         m_nm;

    public PHIVisitor(QuadFactory qf, Map dT, NameMap nm)
    {      
	super(false/*non-strict*/);
	m_dT          = dT;
	m_qf          = qf;
	m_nm          = nm;
    }

    public void visit(Quad q) { }

    public void visit(LABEL q)
    {
	LABEL label = new LABEL(m_qf, q, q.label(), new Temp[0], q.arity());

	int numPhis = q.numPhis(), arity = q.arity();
	for (int i=0; i<numPhis; i++)
	    for (int j=0; j<arity; j++)
		pushBack(q, i, j); // Adds moves & updates derivation table
      
	Quad.replace(q, label);
    }
      
    public void visit(PHI q)
    {
	PHI phi = new PHI(q.getFactory(), q, new Temp[0], q.arity());

	int numPhis = q.numPhis(), arity = q.arity();
	for (int i=0; i<numPhis; i++) {
	    for (int j=0; j<arity; j++)
		pushBack(q, i, j); // Adds moves & updates derivation table
	    // remove unneeded derivation table entry for q.dst(i)
	    if (m_dT!=null)
		m_dT.remove(new Tuple(new Object[] { q, q.dst(i) }));
	}

	Quad.replace(q, phi);
    }

    // insert MOVE on edge into PHI & update derivation table.
    private void pushBack(PHI q, int dstIndex, int srcIndex)
    {
	Edge e    = q.prevEdge(srcIndex);
	MOVE m    = new MOVE(m_qf, q, q.dst(dstIndex), 
			     q.src(dstIndex, srcIndex));
	Quad.addEdge((Quad)e.from(), e.which_succ(), m, 0);
	Quad.addEdge(m, 0, (Quad)e.to(), e.which_pred());

	if (m_dT != null) { // skip this if we're not maintaining type info
	    // update the derivation table to reflect the new def points.
	    Tuple oldtup = new Tuple(new Object[] { q, m.dst() });
	    Object type = m_dT.get(oldtup);
	    Util.assert(type!=null, "No type for "+m.dst()+" in "+q);
	    Tuple newtup = new Tuple(new Object[] { m, m.dst() });
	    m_dT.put(newtup, type);
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
	    Tuple tuple = new Tuple(new Object[] { qNew, map(defs[i]) });
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

static class NameMap implements TempMap { // this is an inner class
    Map h = new HashMap();
    public Temp tempMap(Temp t) {
	while (h.containsKey(t)) { t = (Temp)h.get(t); }
	return t;
    }
    public void map(Temp Told, Temp Tnew) { h.put(Told, Tnew); }
}

} // close the ToNoSSA class (yes, the indentation's screwed up,
  // but I don't feel like re-indenting all this code) [CSA]