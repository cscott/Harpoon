package harpoon.IR.LowQuad;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.ToNoSSA;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;

import java.util.Enumeration;
import java.util.Hashtable;
    
/**
 * <blink><b>FILL ME IN</b></blink>
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: LowQuadNoSSA.java,v 1.1.2.11 1999-08-03 23:53:19 pnkfelix Exp $
 */
public class LowQuadNoSSA extends Code /*which extends harpoon.IR.Quads.Code*/
{
  private Derivation m_derivation;
  private TypeMap    m_typeMap;

  /** The name of this code view. */
  public static final String codename  = "low-quad-no-ssa";

  /** Creates a <code>LowQuadNoSSA</code> object from a LowQuad object */
  LowQuadNoSSA(LowQuadSSA code)
    {
      super(code.getMethod(), null);
      
      ToNoSSA translator;
      
      translator   = new ToNoSSA(qf, code, code, code);
      quads        = translator.getQuads();
      m_derivation = translator;
      m_typeMap    = translator;
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
	} else {
	    //throw new Error("don't know how to make " + codename +
	    //	" from " + hcf.getCodeName());
	    return codeFactory(LowQuadSSA.codeFactory(hcf));
	}
    }
  
  /**
   * Return a code factory for <code>LowQuadNoSSA</code>, using the default
   * code factory for <code>LowQuadSSA</code>
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

  public DList derivation(HCodeElement hce, Temp t)
    {
      return m_derivation.derivation(hce, t);
    }

  public HClass typeMap(HCode hc, Temp t)
    {
      // Ignores hc parameter
      return m_typeMap.typeMap(this, t);
    }
}
