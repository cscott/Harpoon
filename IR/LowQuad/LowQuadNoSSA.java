package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.LowQuadFactory;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.Quads.CloningTempMap;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.SIGMA;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * <code>LowQuad.LowQuadNoSSA</code> is a code view which generates Quads
 * not in SSA form.  In other words, all <code>PHI</code> and <code>SIGMA</code> 
 * nodes are mere placeholders, their meaning being represented explicitly 
 * in the code.  Note that the testing on this class has been minimal, so it
 * may still contain bugs.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: LowQuadNoSSA.java,v 1.1.2.1 1999-02-03 06:02:14 duncan Exp $
 */
public class LowQuadNoSSA extends Code /*which extends harpoon.IR.Quads.Code*/
{
  /** The name of this code view. */
  public static final String codename  = "low-quad-no-ssa";

  /** Creates a <code>LowQuadNoSSA</code> object from a LowQuad object */
  LowQuadNoSSA(Code lowQuad)
    {
      super(lowQuad.getMethod(), null);
      hD       = (Hashtable)lowQuad.hD.clone();
      quads    = translate((LowQuadFactory)qf, lowQuad); 
    }

  /** Creates a <code>LowQuadNoSSA</code> object from a QuadSSA object */
  LowQuadNoSSA(harpoon.IR.Quads.QuadSSA quadSSA)
    {
      this(new Code(quadSSA));
    }

  /**
   * Create a new code object given a quadruple representation of the
   * method instructions.
   */
  protected LowQuadNoSSA(HMethod method, Quad quads)
    {
      super(method, quads);
    }

  /**
   * Clone this code representation.  The clone has its own copy of the
   * quad graph.
   */
  public HCode  clone(HMethod newMethod)
    {
      LowQuadNoSSA lqns = new LowQuadNoSSA(newMethod, null);
      lqns.quads        = Quad.clone(lqns.qf, quads);
      return lqns;
    }

  /**
   * Return the name of this code view.
   * @return the string <code>"low-quad-no-ssa"</code>
   */
  public String getName() { return codename; }

  /**
   * Return a code factory for <code>LowQuadNoSSA</code>, given a 
   * code factory for either <code>harpoon.IR.LowQuad.Code</code>, or
   * <code>QuadSSA</code>.
   */
  public static HCodeFactory codeFactory(final HCodeFactory hcf)
    {
      if (hcf.getCodeName().equals(QuadSSA.codename)) 
	{
	  return new HCodeFactory() {
	    public HCode convert(HMethod m) {
	      HCode c = hcf.convert(m);
	      return (c==null) ? null :
		new LowQuadNoSSA((QuadSSA)c);
	    }
	    public void clear(HMethod m) { hcf.clear(m); }
	    public String getCodeName() { return codename; }
	  };
	}
      else if (hcf.getCodeName().equals(harpoon.IR.LowQuad.Code.codename))
	{
	  return new HCodeFactory() { 
	    public HCode convert(HMethod m) { 
	      HCode c = hcf.convert(m);
	      return (c==null) ? null :
		new LowQuadNoSSA((harpoon.IR.LowQuad.Code)c);
	    }
	    public void clear(HMethod m) { hcf.clear(m); }
	    public String getCodeName() { return codename; }
	  };
	}
      else 
	throw new Error("don't know how to make " + codename +
			" from " + hcf.getCodeName());
    }
  
  /**
   * Return a code factory for <code>LowQuadNoSSA</code>, using the default
   * code factory for <code>harpoon.IR.LowQuad.Code</code>
   */
  public static HCodeFactory codeFactory()
    {  
      return codeFactory(harpoon.IR.LowQuad.Code.codeFactory());
    }

  /**
   * Performs the translation from SSA to No-SSA form, returning the
   * new root Quad.
   */
  protected Quad translate(final LowQuadFactory lqf,
			   final harpoon.IR.Quads.Code code)
    {
      CloningTempMap        ctm;
      LowQuadMap            lqm;
      LowQuadNoSSAVisitor   v;
      NameMap               nm;
      Quad                  old_header, qTmp;


      Util.assert(code.getName().equals("quad-ssa") ||
		  code.getName().equals("low-quad"));
      
      
      old_header = (Quad)code.getRootElement();
      ctm        = new CloningTempMap(old_header.getFactory().tempFactory(), 
				      lqf.tempFactory());
      lqm        = new LowQuadMap();
      nm         = new NameMap(); 
      v          = new LowQuadNoSSAVisitor(ctm, lqf, lqm, hD, nm);

      for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	{
	  ((Quad)e.nextElement()).visit(v);
	}
      
      lqm.rename(lqf, nm, nm);
      for (Enumeration e = code.getElementsE(); e.hasMoreElements();)
	{
	  qTmp       = (Quad)e.nextElement();
	  Edge[] el  = qTmp.nextEdge();	  
	  for (int i=0; i<el.length; i++)
	    Quad.addEdge(lqm.getFoot((Quad)el[i].from()),
			 el[i].which_succ(),
			 lqm.getHead((Quad)el[i].to()),
			 el[i].which_pred());
	}
      return lqm.getHead(old_header);
    }
}

/**
 * Implements the SSA to No-SSA translation 
 */
class LowQuadNoSSAVisitor extends LowQuadVisitor
{
  private CloningTempMap  m_ctm;
  private Hashtable       m_dT;
  private LowQuadFactory  m_lqf;
  private LowQuadMap      m_lqm;
  private NameMap         m_nm;

  public LowQuadNoSSAVisitor(CloningTempMap ctm, LowQuadFactory lqf, 
			     LowQuadMap lqm, Hashtable dT, NameMap nm)
    {      
      m_ctm = ctm;
      m_dT  = dT;
      m_lqf = lqf;
      m_lqm = lqm;
      m_nm  = nm;
    }
  
  public void visit(Quad q)
    {
      Quad qm = (Quad)(q.clone(m_lqf, m_ctm));
      m_lqm.put(q, qm, qm);
    }

  public void visit(PHI q)
    {
      PHI   q0;
      int   arity, numPhis;

      q0       = (PHI)(q.clone(m_lqf, m_ctm));
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

      m_lqm.put(q, q0, q0);
    }
  
  public void visit(SIGMA q)
    {
      SIGMA  q0;
      int    arity, numSigmas;
      
      q0         = (SIGMA)(q.clone(m_lqf, m_ctm));
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

      m_lqm.put(q, q0, q0);
    }

  private void renameSigma(SIGMA q, int dstIndex, int srcIndex)
    {
      m_nm.map(q.dst(srcIndex, dstIndex), q.src(srcIndex));
    }

  private void pushBack(PHI q, int dstIndex, int srcIndex)
    {
      Quad q0 = new MOVE(m_lqf, q, 
			 map(q.dst(dstIndex)), 
			 map(q.src(dstIndex, srcIndex)));
      m_lqm.put((Quad)q.prevEdge(srcIndex).from(), q0, q0, false);
    }

  private Temp map(Temp t) 
    {
      return (t==null)?null:m_ctm.tempMap(t);
    }  
}


class LowQuadMap 
{
  final private Hashtable h = new Hashtable();

  void rename(LowQuadFactory lqf, TempMap defmap, TempMap usemap)
    {
      Vector v;
      for (Enumeration e = h.keys(); e.hasMoreElements();)
	{
	  Quad head, key; Quad[] ql;

	  key  = (Quad)e.nextElement();
	  ql   = (Quad[])h.get(key);	  
	  if (ql[0] == ql[1])
	    {
	      head = ql[0].rename(lqf, defmap, usemap);
	      h.put(key, new Quad[] { head, head });
	    }
	  else
	    {
	      v     = new Vector();
	      head  = ql[0];
	      try {
		while (true) {
		  v.addElement(head.rename(lqf, defmap, usemap));
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

