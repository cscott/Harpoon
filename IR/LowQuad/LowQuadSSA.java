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
import harpoon.Util.Util;

import java.util.Hashtable;

/**
 * <blink><b>FILL ME IN</b></blink>
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: LowQuadSSA.java,v 1.1.2.12 1999-07-13 20:53:56 bdemsky Exp $
 */
public class LowQuadSSA extends Code
{
  private Derivation  m_derivation;
  private TypeMap     m_typeMap;

  /** The name of this code view. */
  public static final String codename  = "low-quad-ssa";

  /** Creates a <code>LowQuadNoSSA</code> object from a LowQuad object */
  LowQuadSSA(final QuadSSA code)
    {
      super(code.getMethod(), null);
      final Hashtable dT = new Hashtable();
      final Hashtable tT = new Hashtable();
      final TypeMap tym = new harpoon.Analysis.QuadSSA.TypeInfo();
      FinalMap fm = new harpoon.Backend.Maps.DefaultFinalMap();
      quads = Translate.translate((LowQuadFactory)qf, code, tym, fm, dT, tT);
      
      m_derivation = new Derivation() {
	public DList derivation(HCodeElement hce, Temp t) {
	  return ((hce==null)||(t==null))?null:(DList)dT.get(t);
	}
      };
      final LowQuadFactory lqf =  // javac bug workaround to let qf be
	  (LowQuadFactory) qf;    // visible in anonymous TypeMap below.
      m_typeMap = new TypeMap() { 
	public HClass typeMap(HCode hc, Temp t) { 
	  Util.assert(lqf.tempFactory()==t.tempFactory());
	  Object type = tT.get(t);   // Ignores hc parameter
	  if (type instanceof Error) 
	    throw (Error)((Error)type).fillInStackTrace();
	  else
	    return (HClass)type;
	}
      };
    }

  /**
   * Create a new code object given a quadruple representation of the
   * method instructions.
   */
  protected LowQuadSSA(HMethod method, Quad quads)
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
    return m_derivation.derivation(hce, t);
  }

  public HClass typeMap(HCode hc, Temp t) {
    // Ignores hc parameter
    return m_typeMap.typeMap(this, t);
  }
}
