package harpoon.IR.Quads;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
 
/**
 * The <code>ToNoSSA</code> class implements the translation between SSA 
 * and No-SSA form.  
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ToNoSSA.java,v 1.1.2.4 1999-02-06 22:15:05 duncan Exp $
 */
public class ToNoSSA 
{
  public static Quad translate(QuadFactory qf, Code code)
    {
      return translate(qf, new Hashtable(), code);
    }

  /**
   * Translates the code in the supplied codeview from SSA to No-SSA form, 
   * returning the new root <code>Quad</code>.  The <code>Hashtable</code>
   * parameter is used to maintain derivation information.
   * 
   * @parameter qf    the <code>QuadFactory</code> which will manufacture the
   *                  translated <code>Quad</code>s.
   * @parameter hD    a <code>Hashtable</code> containing the derivation 
   *                  information of the specified codeview.
   * @parameter code  the codeview containing the <code>Quad</code>s to 
   *                  translate.
   */
  public static Quad translate(QuadFactory qf, Hashtable hD, Code code)
    {
      Util.assert(code.getName().equals("quad-ssa") ||
		  code.getName().equals("low-quad-ssa") ||
		  code.getName().equals("quad-with-try"));

      
      CloningTempMap     ctm;
      SIGMAVisitor       sigmaVisitor;
      NameMap            nm;
      PHIVisitor         phiVisitor;
      Quad               old_header, qTmp;
      QuadMap            qm;

      
      old_header   = (Quad)code.getRootElement();
      ctm          = new CloningTempMap(old_header.getFactory().tempFactory(), 
					qf.tempFactory());
      nm           = new NameMap(); 
      qm           = new QuadMap();
      sigmaVisitor = new SIGMAVisitor(ctm, nm, qf, qm);
      
      for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	((Quad)e.nextElement()).visit(sigmaVisitor);
      
      qm.rename(qf, nm, nm);
      
      for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
        {
          qTmp       = (Quad)e.nextElement();
          Edge[] el  = qTmp.nextEdge();   
          for (int i=0; i<el.length; i++)
            Quad.addEdge(qm.getFoot((Quad)el[i].from()),
                         el[i].which_succ(),
                         qm.getHead((Quad)el[i].to()),
                         el[i].which_pred());
        }

      phiVisitor = new PHIVisitor(qf, hD, nm);
      for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	qm.getFoot((Quad)e.nextElement()).visit(phiVisitor);

      return qm.getHead(old_header);
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
      m_ctm = ctm;
      m_nm  = nm;
      m_qf  = qf;
      m_qm  = qm;
    }
  
  public void visit(Quad q)
    {
      Quad qm = (Quad)(q.clone(m_qf, m_ctm));
      m_qm.put(q, qm, qm);
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

      m_qm.put(q, q0, q0);
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

      m_qm.put(q, q0, q0);
    }
  
  private Temp map(Temp t) 
    {
      return (t==null)?null:m_ctm.tempMap(t);
    }  

  private void renameSigma(SIGMA q, int dstIndex, int srcIndex)
    {
      m_nm.map(map(q.dst(srcIndex, dstIndex)), map(q.src(srcIndex)));
    }
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
      m_dT  = dT;
      m_qf  = qf;
      m_nm  = nm;
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
      int numPhis = q.numPhis(), arity = q.arity();
      
      for (int i=0; i<numPhis; i++)
	for (int j=0; j<arity; j++)
	  pushBack(q, i, j);
      
      removePHIs(q, new LABEL(m_qf, q, q.label(), new Temp[] {}, q.arity()));
    }
      
  public void visit(PHI q)
    {
      int numPhis = q.numPhis(), arity = q.arity();
      
      for (int i=0; i<numPhis; i++)
	for (int j=0; j<arity; j++)
	  pushBack(q, i, j);

      removePHIs(q, new PHI(m_qf, q, new Temp[] {}, q.arity()));
    }

  private void removePHIs(PHI q, PHI q0)
    {
      Edge[] el;
      
      el = q.prevEdge();
      for (int i=0; i<el.length; i++)
	Quad.addEdge(q.prev(i), q.prevEdge(i).which_succ(),
		     q0, q.prevEdge(i).which_pred());
      
      el = q.nextEdge();
      for (int i=0; i<el.length; i++)
	Quad.addEdge(q0, q.nextEdge(i).which_pred(),
		     q.next(i), q.nextEdge(i).which_succ());
      
    }

  private void pushBack(PHI q, int dstIndex, int srcIndex)
    {
      // Must insert a MOVE between the PHI and its previous Quad

      Edge from = q.prevEdge(srcIndex);
      MOVE m = new MOVE(m_qf, q, 
			q.dst(dstIndex), 
			q.src(dstIndex, srcIndex));
      Quad.addEdge(q.prev(srcIndex), from.which_succ(), m, 0);
      Quad.addEdge(m, 0, q, from.which_pred());
    }

}


class QuadMap 
{
  final private Hashtable h = new Hashtable();

  void rename(QuadFactory qf, TempMap defmap, TempMap usemap)
    {
      Vector v = null;
      for (Enumeration e = h.keys(); e.hasMoreElements();)
	{
	  Quad head, key; Quad[] ql;

	  key  = (Quad)e.nextElement();
	  ql   = (Quad[])h.get(key);	  
	  if (ql[0] == ql[1])
	    {
	      head = ql[0].rename(qf, defmap, usemap);
	      h.put(key, new Quad[] { head, head });
	    }
	  else
	    {
	      if (v==null) 
		v = new Vector();
	      else 
		v.removeAllElements();
	      head = ql[0];
	      try {
		while (true) {
		  v.addElement(head.rename(qf, defmap, usemap));
		  head = head.next(0);
		} 
	      }
	      catch (NullPointerException err) { }
	      ql = new Quad[v.size()];
	      v.copyInto(ql);
	      Quad.addEdges(ql);
	      h.put(key, new Quad[] { ql[0], ql[ql.length-1] });
	    }
	}
    }
      
  void put(Quad old, Quad new_header, Quad new_footer) 
    { put(old, new_header, new_footer, true); }
      
  void put(Quad old, Quad new_header, Quad new_footer, boolean prepend) 
    {
      Quad[] q = (Quad[])h.get(old);

      if (q != null)
	{
	  if (prepend)
	    {
	      Quad.addEdge(new_footer, 0, q[0], 0);
	      new_footer = q[1];
	    }
	  else
	    {
	      Quad.addEdge(q[1], 0, new_header, 0);
	      new_header = q[0];
	    }
	}
      h.put(old, new Quad[] { new_header, new_footer });
    }

  
  Quad getHead(Quad old) 
    {
      Quad[] ql = (Quad[])h.get(old); return (ql==null)?null:ql[0];
    }
  
  Quad getFoot(Quad old) 
    {
      Quad[] ql = (Quad[])h.get(old); return (ql==null)?null:ql[1];
    }
  
  boolean contains(Quad old) { return h.containsKey(old); }
}


class NameMap implements TempMap
{
  Hashtable h = new Hashtable();
  public Temp tempMap(Temp t)
    {
      while (h.containsKey(t))
	{
	  t = (Temp)h.get(t);
	}
      return t;
    }

  public void map(Temp Told, Temp Tnew) { h.put(Told, Tnew); }
  public String toString() { return h.toString(); }
}

