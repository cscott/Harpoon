package harpoon.IR.Quads;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.LowQuadVisitor;
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
 * @version $Id: ToNoSSA.java,v 1.1.2.1 1999-02-03 21:18:57 duncan Exp $
 */
public class ToNoSSA 
{
  /**
   * Translates the code in the supplied codeview from SSA to No-SSA form, 
   * returning the new root <code>Quad</code>.  The <code>Hashtable</code>
   * parameter is used to maintain derivation information.
   */
  public static Quad translate(final QuadFactory qf,
			       final Hashtable hD, 
			       final Code code)
    {
      CloningTempMap     ctm;
      QuadMap            qm;
      QuadNoSSAVisitor   v;
      NameMap            nm;
      Quad               old_header, qTmp;
      
      Util.assert(code.getName().equals("quad-ssa") ||
		  code.getName().equals("low-quad-ssa") ||
		  code.getName().equals("quad-with-try"));
      
      
      old_header = (Quad)code.getRootElement();
      ctm        = new CloningTempMap(old_header.getFactory().tempFactory(), 
				      qf.tempFactory());
      qm         = new QuadMap();
      nm         = new NameMap(); 
      v          = new QuadNoSSAVisitor(ctm, qf, qm, hD, nm);

      for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	{
	  ((Quad)e.nextElement()).visit(v);
	}
      
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
      return qm.getHead(old_header);
    }
}

/**
 * Implements the SSA to No-SSA translation 
 */
class QuadNoSSAVisitor extends LowQuadVisitor
{
  private CloningTempMap  m_ctm;
  private Hashtable       m_dT;
  private QuadFactory     m_qf;
  private QuadMap         m_qm;
  private NameMap         m_nm;

  public QuadNoSSAVisitor(CloningTempMap ctm, QuadFactory qf, 
			  QuadMap qm, Hashtable dT, NameMap nm)
    {      
      m_ctm = ctm;
      m_dT  = dT;
      m_qf  = qf;
      m_qm  = qm;
      m_nm  = nm;
    }
  
  public void visit(Quad q)
    {
      Quad qm = (Quad)(q.clone(m_qf, m_ctm));
      m_qm.put(q, qm, qm);
    }

  public void visit(PHI q)
    {
      PHI   q0;
      int   arity, numPhis;

      q0       = (PHI)(q.clone(m_qf, m_ctm));
      numPhis  = q.numPhis();
      arity    = q.arity();
      
      for (int i = 0; i < numPhis; i++)
	{
	  for (int j = 0; j < arity; j++)
	    {
	      pushBack(q, i, j);
	    }
	  q0.removePhi(0);
	}

      m_qm.put(q, q0, q0);
    }
  
  public void visit(SIGMA q)
    {
      SIGMA  q0;
      int    arity, numSigmas;
      
      q0         = (SIGMA)(q.clone(m_qf, m_ctm));
      numSigmas  = q.numSigmas();
      arity      = q.arity();

      for (int i=0; i<numSigmas; i++)
	{
	  for (int j=0; j<arity; j++)
	    {
	      renameSigma(q, j, i);
	    }
	  q0.removeSigma(0);
	}

      m_qm.put(q, q0, q0);
    }

  private void renameSigma(SIGMA q, int dstIndex, int srcIndex)
    {
      m_nm.map(q.dst(srcIndex, dstIndex), q.src(srcIndex));
    }

  private void pushBack(PHI q, int dstIndex, int srcIndex)
    {
      Quad q0 = new MOVE(m_qf, q, 
			 map(q.dst(dstIndex)), 
			 map(q.src(dstIndex, srcIndex)));
      m_qm.put((Quad)q.prevEdge(srcIndex).from(), q0, q0, false);
    }

  private Temp map(Temp t) 
    {
      return (t==null)?null:m_ctm.tempMap(t);
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

