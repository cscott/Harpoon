package harpoon.IR.LowQuad;

import harpoon.Backend.Maps.FinalMap;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.ToNoSSA;

import java.util.Hashtable;

/**
 * <b>FILL ME IN</b>
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: LowQuadSSA.java,v 1.1.2.3 1999-02-04 22:56:15 cananian Exp $
 */
public class LowQuadSSA extends Code
{
  /** The name of this code view. */
  public static final String codename  = "low-quad-ssa";

  /** Creates a <code>LowQuadNoSSA</code> object from a LowQuad object */
  LowQuadSSA(QuadSSA code)
    {
      this(code,
	   new harpoon.Analysis.QuadSSA.TypeInfo(),
	   new harpoon.Backend.Maps.DefaultFinalMap());
    }
  /** generally this constructor is not good because the TypeMap is forced
   *  to cache (and keep live) data for lots of QuadSSA objects, and thus
   *  wastes memory. */
  private LowQuadSSA(QuadSSA code, TypeMap tym, FinalMap fm) {
    super(code.getMethod(), null);
    quads = Translate.translate((LowQuadFactory)this.qf, code, tym, fm, hD);
  }
  /**
   * Create a new code object given a quadruple representation of the
   * method instructions.
   */
  private LowQuadSSA(HMethod method, Quad quads)
    {
      super(method, quads);
    }

  /**
   * Clone this code representation.  The clone has its own copy of the
   * quad graph.
   */
  public HCode clone(HMethod newMethod)
    {
      LowQuadSSA lqs = new LowQuadSSA(newMethod, null);
      lqs.quads      = Quad.clone(lqs.qf, quads);
      return lqs;
    }

  /**
   * Return the name of this code view.
   * @return the string <code>"low-quad-ssa"</code>
   */
  public String getName() { return codename; }

  /**
   * Return a code factory for <code>LowQuadSSA</code>, given a 
   * code factory for <code>QuadSSA</code>.
   */
  public static HCodeFactory codeFactory(final HCodeFactory hcf)
    {
      if (hcf.getCodeName().equals(QuadSSA.codename)) 
	{
	  return new HCodeFactory() {
	    public HCode convert(HMethod m) {
	      HCode c = hcf.convert(m);
	      return (c==null) ? null :
		new LowQuadSSA((QuadSSA)c);
	    }
	    public void clear(HMethod m) { hcf.clear(m); }
	    public String getCodeName() { return codename; }
	  };
	}
      else if (hcf.getCodeName().equals(LowQuadNoSSA.codename)) 
	{
	  throw new Error("When I get around to rewriting the SSA conversion"+
			  " algorithms, this will work.");
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
      return codeFactory(QuadSSA.codeFactory());
    }

  // obsolete
  public static void register() 
    {
      HMethod.register(codeFactory());
    }
}
