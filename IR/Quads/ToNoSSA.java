package harpoon.IR.Quads;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
 
/**
 * The <code>ToNoSSA</code> class implements the translation between SSA 
 * and No-SSA form.  
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ToNoSSA.java,v 1.1.2.14 1999-07-12 21:59:05 bdemsky Exp $
 */
public class ToNoSSA implements Derivation, TypeMap
{
    private CloningTempMap  m_ctm;
    private Derivation      m_derivation;
    private Quad            m_quads;
    private TypeMap         m_typeMap;
    
    static class DerivationI implements Derivation {
	public DList derivation(HCodeElement hce, Temp t) { return null; }
    }

    public ToNoSSA(QuadFactory newQF, Code code)
    {
	this(newQF, code, new DerivationI());	   
    }

    static class TypeMapI implements TypeMap {
	public HClass typeMap(HCode hc, Temp t) {return null;}
    }
  
    public ToNoSSA(QuadFactory newQF, Code code, Derivation derivation)
    {
	this(newQF, code, derivation, new TypeMapI());
    }

    public ToNoSSA(final QuadFactory newQF, Code code,
		   Derivation derivation, TypeMap typeMap) {
	Util.assert(code.getName().equals("quad-ssa") ||
		    code.getName().equals("low-quad-ssa") ||
		    code.getName().equals("quad-with-try"));
    
	final Hashtable dT = new Hashtable();

	m_ctm   = new CloningTempMap
	    (((Quad)code.getRootElement()).getFactory().tempFactory(),
	     newQF.tempFactory());
	m_quads = translate(newQF, derivation, typeMap, dT, code);
	m_derivation = new Derivation() {
	    public DList derivation(HCodeElement hce, Temp t) {
		return ((t==null)||(hce==null))? null:
		(DList)dT.get(new Tuple(new Object[] { hce, t }));
	    }
	};
	m_typeMap = new TypeMap() {
	    public HClass typeMap(HCode hc, Temp t) {
		Util.assert(t.tempFactory()==newQF.tempFactory());
		Object type = dT.get(t);   // Ignores hc parameter
		if (type instanceof Error) 
		throw (Error)((Error)type).fillInStackTrace();
		else                       
		return (HClass)type;
	    }
	};
    }

    public DList derivation(HCodeElement hce, Temp t)
    { return m_derivation.derivation(hce, t); }

    public Quad getQuads()        { return m_quads; }
    public HClass typeMap(HCode hc, Temp t) { 
	// Ignores HCode parameter
	return m_typeMap.typeMap(hc, t);
    }

    /**
     * Translates the code in the supplied codeview from SSA to No-SSA form, 
     * returning the new root <code>Quad</code>.  The <code>Hashtable</code>
     * parameter is used to maintain derivation information.
     * 
     * @parameter qf    the <code>QuadFactory</code> which will manufacture the
     *                  translated <code>Quad</code>s.
     * @parameter dT    a <code>Hashtable</code> containing the derivation 
     *                  information of the specified codeview.
     * @parameter dT a <code>Hashtable</code> in which to place the updated
     *                  derivation information.  
     * @parameter code  the codeview containing the <code>Quad</code>s to 
     *                  translate.
     */
    private Quad translate(QuadFactory qf, Derivation derivation, 
			   TypeMap typeMap, Hashtable dT, Code code)
    {
	CloningTempMap  ctm;
	NameMap         nm;
	Quad            old_header, qTmp;
	QuadMap         qm;
	LowQuadVisitor  v;

	old_header   = (Quad)code.getRootElement();
	nm           = new NameMap(); 
	qm           = new QuadMap(m_ctm, code, derivation, dT, typeMap);

	// Remove all SIGMAs from the code
	v = new SIGMAVisitor(m_ctm, nm, qf, qm);
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	    ((Quad)e.nextElement()).visit(v);
      
	// Rename variables appropriately to account for the removed SIGMAs
	qm.rename(qf, nm, nm);

	// Connect the edges of these new Quads
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();) {
	    qTmp       = (Quad)e.nextElement();
	    Edge[] el  = qTmp.nextEdge();   
	    for (int i=0; i<el.length; i++)
		Quad.addEdge(qm.get((Quad)el[i].from()),
			     el[i].which_succ(),
			     qm.get((Quad)el[i].to()),
			     el[i].which_pred());
	}

	// Modify this new CFG by emptying PHI nodes
	v = new PHIVisitor(qf, dT, nm);
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	    qm.get((Quad)e.nextElement()).visit(v);

	// Return the head of the new CFG
	return qm.get(old_header);
    }

}

/*
 * Performs the first phase of the transformation to NoSSA form:
 * removing the SIGMAs.  This visitor also serves the purpose of cloning
 * the quads in the codeview, which is necessary because the PHIVisitor
 * will actually modify these quads, and we do not want these effects
 * to propagate outside of the ToNoSSA class.
 */
class SIGMAVisitor extends LowQuadVisitor
{
    private CloningTempMap m_ctm;
    private NameMap        m_nm;
    private QuadFactory    m_qf;
    private QuadMap        m_qm;

    public SIGMAVisitor(CloningTempMap ctm, NameMap nm, 
			QuadFactory qf, QuadMap qm)
    {
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
  
    public void visit(AGET q)    { visit((Quad)q); }
    public void visit(ASET q)    { visit((Quad)q); }
    public void visit(CALL q)    { visit((Quad)q); }
    public void visit(GET q)     { visit((Quad)q); }
    public void visit(HANDLER q) { visit((Quad)q); }
    public void visit(OPER q)    { visit((Quad)q); }
    public void visit(SET q)     { visit((Quad)q); }

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
class PHIVisitor extends LowQuadVisitor
{
    private Hashtable       m_dT;
    private QuadFactory     m_qf;
    private NameMap         m_nm;

    public PHIVisitor(QuadFactory qf, Hashtable dT, NameMap nm)
    {      
	m_dT          = dT;
	m_qf          = qf;
	m_nm          = nm;
    }

    public void visit(Quad q) { }

    public void visit(AGET q)    { visit((Quad)q); }
    public void visit(ASET q)    { visit((Quad)q); }
    public void visit(CALL q)    { visit((Quad)q); }
    public void visit(GET q)     { visit((Quad)q); }
    public void visit(HANDLER q) { visit((Quad)q); }
    public void visit(OPER q)    { visit((Quad)q); }
    public void visit(SET q)     { visit((Quad)q); }
  
    public void visit(LABEL q)
    {
	LABEL label = new LABEL(m_qf, q, q.label(), new Temp[0], q.arity());
	int numPhis = q.numPhis(), arity = q.arity();

	for (int i=0; i<numPhis; i++)
	    for (int j=0; j<arity; j++)
		pushBack(q, i, j);
      
	//removePHIs(q, new LABEL(m_qf, q, q.label(), new Temp[] {}, q.arity()));
	removeTuples(q);  // Updates derivation table

	Quad []prev=q.prev();
	Quad []next=q.next(); Util.assert(next.length==1);
	for(int i=0;i<prev.length;i++) {
	    Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),label,i);
	}
	Quad.addEdge(label,0,next[0],q.nextEdge(0).which_pred());
    }
      
    public void visit(PHI q)
    {
	PHI phi = new PHI(q.getFactory(), q, new Temp[0], q.arity());

	int numPhis = q.numPhis(), arity = q.arity();
	for (int i=0; i<numPhis; i++)
	    for (int j=0; j<arity; j++)
		pushBack(q, i, j);

	//removePHIs(q, new PHI(m_qf, q, new Temp[] {}, q.arity()));
	removeTuples(q);  // Updates derivation table

	Quad []prev=q.prev();
	Quad []next=q.next(); Util.assert(next.length==1);
	for(int i=0;i<prev.length;i++) {
	    Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),phi,i);
	}
	Quad.addEdge(phi,0,next[0],q.nextEdge(0).which_pred());
    }

    private void removePHIs(PHI q, PHI q0)
    {
	Edge[] el;
      
	el = q.prevEdge();
	for (int i=0; i<el.length; i++)
	    Quad.addEdge(q.prev(i), q.prevEdge(i).which_succ(),
			 q0, q.prevEdge(i).which_pred());
      
	el = q.nextEdge();
	for (int i=0; i<el.length; i++) {
	    Quad.addEdge(q0, q.nextEdge(i).which_pred(),
			 q.next(i), q.nextEdge(i).which_succ());
	}
      
    }

    private void removeTuples(Quad q)
    {
	Temp[] tDef = q.def(), tUse = q.use();       
	Tuple t;

	for (int i=0; i<tDef.length; i++) {
	    t = new Tuple(new Object[] { q, tDef[i] });
	    m_dT.remove(t);
	}
	for (int i=0; i<tUse.length; i++) {
	    t = new Tuple(new Object[] { q, tUse[i] });
	    m_dT.remove(t);
	}
    }
  

    private void pushBack(PHI q, int dstIndex, int srcIndex)
    {
	Edge from = q.prevEdge(srcIndex);
	MOVE m    = new MOVE(m_qf, q, q.dst(dstIndex), 
			     q.src(dstIndex, srcIndex));
	Quad.addEdge(q.prev(srcIndex), from.which_succ(), m, 0);
	Quad.addEdge(m, 0, q, from.which_pred());

	// Type information does not change, *but* we need to update
	// the derivation table
	DList dlDst = (DList)m_dT.get(new Tuple(new Object[] { q, m.dst() }));
	DList dlSrc = (DList)m_dT.get(new Tuple(new Object[] { q, m.src() }));
	if (dlDst!=null) m_dT.put(new Tuple(new Object[] { m, m.dst() }), dlDst);
	if (dlSrc!=null) m_dT.put(new Tuple(new Object[] { m, m.src() }), dlSrc);
    }

}


class QuadMap 
{
    private CloningTempMap  m_ctm;
    private Code            m_code;
    private Derivation      m_derivation;
    private Hashtable       m_dT;
    private TypeMap         m_typeMap;

    final private Hashtable h = new Hashtable();

    QuadMap(CloningTempMap ctm, Code code, Derivation derivation, 
	    Hashtable dT, TypeMap typeMap)
    {
	m_ctm         = ctm;
	m_code        = code;
	m_derivation  = derivation;
	m_dT          = dT;
	m_typeMap     = typeMap;
    }

    boolean contains(Quad old)  { return h.containsKey(old); }  

    Quad get(Quad old) { 
	return (Quad)h.get(old);
    }

    void put(Quad qOld, Quad qNew)
    {
	h.put(qOld, qNew);
	updateDTInfo(qOld, qNew);
    }
  
    // usemap not used -- remove redundant parameter
    //
    void rename(QuadFactory qf, TempMap defmap, TempMap usemap)
    {
	for (Enumeration e = ((Hashtable)h.clone()).keys(); 
	     e.hasMoreElements();) {
	    Quad head, key, value, newValue; Tuple tuple;

	    key      = (Quad)e.nextElement();
	    value    = (Quad)h.get(key);	
	    newValue = (Quad)value.rename(qf, defmap, usemap);
	    h.put(key, newValue);
	    for (int i=0; i<2; i++) {
		Temp[] tmps = (i==0)?value.def():value.use();
		for (int j=0; j<tmps.length; j++) {
		    tuple = new Tuple(new Object[] { value, tmps[j] });
		    if (m_dT.containsKey(tuple)) {
			DList dl = DList.rename((DList)m_dT.get(tuple), defmap);
			Temp tmp = (i==0)?newValue.def()[j]:newValue.use()[j];
			m_dT.put(new Tuple(new Object[] { newValue, tmp }), dl);
		    }
		    if (m_dT.containsKey(tmps[j])) {
			m_dT.put(defmap.tempMap(tmps[j]), m_dT.get(tmps[j]));
		    }
		}
	    }
	}
    }
      
    /* UTILITY METHODS FOLLOW */

    private Temp map(Temp t) 
    { return (t==null)?null:m_ctm.tempMap(t); }  
      
    private void updateDTInfo(Quad qOld, Quad qNew)
    {
	Util.assert(qOld!=null && qNew != null);

	DList dl; HClass hc; Temp[] tmps; Temp newTmp;

	for (int j=0; j<2; j++) {
	    tmps = (j==0)?qOld.def():qOld.use();
	    for (int i=0; i<tmps.length; i++) {
		dl = DList.clone(m_derivation.derivation(qOld, tmps[i]), m_ctm);
		if (dl!=null) { // If tmps[i] is a derived ptr, update deriv info.
		    m_dT.put(new Tuple(new Object[] { qNew, map(tmps[i]) }),dl);
		    m_dT.put(map(tmps[i]), 
			     new Error("*** Can't type a derived pointer: " + 
				       map(tmps[i])));
		}
		else { // If the tmps[i] is NOT a derived pointer, assign its type
		    hc = m_typeMap.typeMap(m_code, tmps[i]);
		    if (hc!=null) {
			m_dT.put(map(tmps[i]), hc);
		    }
		}
	    }
	}
    }
}

class NameMap implements TempMap {
    Hashtable h = new Hashtable();
    public Temp tempMap(Temp t) {
	while (h.containsKey(t)) { t = (Temp)h.get(t); }
	return t;
    }
    public void map(Temp Told, Temp Tnew) { h.put(Told, Tnew); }
}


