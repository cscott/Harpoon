package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.ToNoSSA;

import java.util.Enumeration;
import java.util.Hashtable;
    
/**
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: LowQuadNoSSA.java,v 1.1.2.5 1999-02-06 21:54:40 duncan Exp $
 */
public class LowQuadNoSSA extends Code /*which extends harpoon.IR.Quads.Code*/
{
  /** The name of this code view. */
  public static final String codename  = "low-quad-no-ssa";

  /** Creates a <code>LowQuadNoSSA</code> object from a LowQuad object */
  LowQuadNoSSA(LowQuadSSA code)
    {
      super(code.getMethod(), null);
      for (Enumeration e = code.hD.keys(); e.hasMoreElements();) {
	Object next = e.nextElement();
	hD.put(e.nextElement(), code.hD.get(next));
      }
      quads    = ToNoSSA.translate(qf, hD, code); 
    }

  /**
   * Create a new code object given a quadruple representation of the
   * method instructions.
   */
  private LowQuadNoSSA(HMethod method, Quad quads)
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
      if (hcf.getCodeName().equals(LowQuadSSA.codename))
	{
	  return new HCodeFactory() { 
	    public HCode convert(HMethod m) { 
	      HCode c = hcf.convert(m);
	      return (c==null) ? null :
		new LowQuadNoSSA((LowQuadSSA)c);
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
      return codeFactory(LowQuadSSA.codeFactory());
    }

  // obsolete
  public static void register() 
    {
      HMethod.register(codeFactory());
    }
}
