package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.LowQuadNoSSA;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.Temp.Temp;

public class TreeCode extends Code
{
  public static final String codename = "tree";
  private Derivation m_derivation;
  private TypeMap    m_typeMap;
  
  TreeCode(LowQuadNoSSA code, Frame topframe)
    {
      super(code.getMethod(), null, topframe);

      ToTree translator;

      translator   = new ToTree(this.tf, code, frame);
      tree         = translator.getTree();
      m_derivation = translator;
      m_typeMap    = translator;
    }

  private TreeCode(HMethod newMethod, Tree tree, Frame topframe)
    {
      super(newMethod, tree, topframe);
    }

  public HCode clone(HMethod newMethod, Frame frame) {
    // assumes Frame is immutable
    TreeCode tc = new TreeCode(newMethod, null, frame); 
    tc.tree = (Tree)this.tree.clone(tc.tf);
    return tc;
  }

  public String getName() { return codename; }

  /**
   * Return a code factory for <code>TreeCode</code>, given a 
   * code factory for either <code>LowQuadNoSSA</code>
   */
  public static HCodeFactory codeFactory(final HCodeFactory hcf, 
					 final Frame frame)
    {
      if (hcf.getCodeName().equals(LowQuadNoSSA.codename))
	{
	  return new HCodeFactory() { 
	    public HCode convert(HMethod m) { 
	      HCode c = hcf.convert(m);
	      return (c==null) ? null :
		new TreeCode((LowQuadNoSSA)c, frame);
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
   * code factory for <code>LowQuadNoSSA</code>
   */
  public static HCodeFactory codeFactory(final Frame frame) {  
    return codeFactory(LowQuadNoSSA.codeFactory(), frame);
  }

  // obsolete
  public static void register() { 
    HMethod.register
      (codeFactory(new harpoon.Backend.Generic.DefaultFrame())); 
  }

  public DList derivation(HCodeElement hce, Temp t){
    return m_derivation.derivation(hce, t);
  }

  public HClass typeMap(HCode hc, Temp t) {
    // Ignores hc parameter
    return m_typeMap.typeMap(this, t);
  }
}

