 package harpoon.IR.LowQuad;

import harpoon.Backend.Maps.FinalMap;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.ToNoSSA;
import harpoon.Temp.Temp;

import java.util.Hashtable;

/**
 *
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: LowQuadSSA.java,v 1.1.2.7 1999-02-08 17:25:09 duncan Exp $
 */
public class LowQuadSSA extends Code
{
  private Hashtable m_hD        = new Hashtable();
  private TypeMap   m_typeMap;

  /** The name of this code view. */
  public static final String codename  = "low-quad-ssa";

  /** Creates a <code>LowQuadNoSSA</code> object from a LowQuad object */
  LowQuadSSA(QuadSSA code)
    {
      super(code.getMethod(), null);
      TypeMap tym = new harpoon.Analysis.QuadSSA.TypeInfo();
      FinalMap fm = new harpoon.Backend.Maps.DefaultFinalMap();
      quads = Translate.translate((LowQuadFactory)qf, code, tym, fm, m_hD);
      m_typeMap = tym;
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

  // implement derivation.
  public DList derivation(HCodeElement hce, Temp t) {
    // ignore HCodeElement: this is SSA form.
    return (DList) m_hD.get(t);
  }

  public HClass typeMap(Temp t) {
    return m_typeMap.typeMap(this, t);
  }
}
