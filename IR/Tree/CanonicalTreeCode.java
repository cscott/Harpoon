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

public class CanonicalTreeCode extends Code {
    private static final String codename = "canonical-tree";
    private Derivation m_derivation;
    private TypeMap    m_typeMap;
  
    CanonicalTreeCode(TreeCode code, Frame frame) {
	super(code.getMethod(), null, frame);

	ToCanonicalTree translator;

	translator   = new ToCanonicalTree(this.tf, code);
	tree         = translator.getTree();
	m_derivation = translator;
	m_typeMap    = translator;
    }

    private CanonicalTreeCode(HMethod newMethod, Tree tree, Frame frame) {
	super(newMethod, tree, frame);
    }

    public HCode clone(HMethod newMethod, Frame frame) {
	// assumes Frame is immutable
	CanonicalTreeCode tc = new CanonicalTreeCode(newMethod, null, frame); 
	tc.tree = (Tree)this.tree.clone(tc.tf);
	return tc;
    }

    public String getName() { return codename; }

    /**
     * Return a code factory for <code>TreeCode</code>, given a 
     * code factory for either <code>LowQuadNoSSA</code>
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf, 
					   final Frame frame) {
	if (hcf.getCodeName().equals(TreeCode.codename)) {
	    return new HCodeFactory() { 
		public HCode convert(HMethod m) { 
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new CanonicalTreeCode((TreeCode)c, frame);
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
	return codeFactory(TreeCode.codeFactory(frame), frame);
    }

    // obsolete (may not even work with null frame)
    public static void register() { HMethod.register(codeFactory(null)); }

    public DList derivation(HCodeElement hce, Temp t){
	return m_derivation.derivation(hce, t);
    }

    public HClass typeMap(HCode hc, Temp t) {
	// Ignores hc parameter
	return m_typeMap.typeMap(this, t);
    }
}

