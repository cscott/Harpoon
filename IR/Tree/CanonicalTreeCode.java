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
import harpoon.Temp.Label;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.LabelList;
import harpoon.Temp.Temp;
import harpoon.Util.HashSet;
import harpoon.Util.Set;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

public class CanonicalTreeCode extends Code {
    private static final String codename = "canonical-tree";
    private final Derivation derivation;
    private final TypeMap    typeMap;
  
    CanonicalTreeCode(TreeCode code, Frame frame) {
	super(code.getMethod(), null, frame);

	ToCanonicalTree translator;

	translator   = new ToCanonicalTree(this.tf, code);
	tree         = translator.getTree();
	derivation   = translator;
	typeMap      = translator;

	// Compute edges for the Trees in this codeview
	new EdgeInitializer().computeEdges();
    }

    //    private CanonicalTreeCode(HMethod newMethod, Tree tree, Frame frame) {
    //super(newMethod, tree, frame);
    //    }

    public HCode clone(HMethod newMethod, Frame frame) {
	/*
	CanonicalTreeCode tc = new CanonicalTreeCode(newMethod, null, frame); 
	final CloningTempMap ctm = new CloningTempMap
	    (this.tf.tempFactory(), tc.tf.tempFactory());
	tc.tree = (Tree)(Tree.clone(tc.tf, ctm, tree));
	tc.derivation = new Derivation() { 
	    public DList derivation(HCodeElement hce, Temp t) { 
		return derivation.derivation(hce, ctm.tempMap(t));
	    }
	};
	tc.typeMap = new TypeMap() { 
	    public HClass typeMap(HCode hc, Temp t) { 
		return typeMap.typeMap(hc, ctm.tempMap(t));
	    }
	};

	// Correctly update the new Frame's registers
	Temp[] temps = tc.frame.getAllRegisters();
	for (int i=0; i<temps.length; i++) { 
	    temps[i] = ctm.tempMap(this.frame.getAllRegisters()[i]);
	}
	*/
	return null;  //tc;
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
	return derivation.derivation(hce, t);
    }

    public HClass typeMap(HCode hc, Temp t) {
	// Ignores hc parameter
	return typeMap.typeMap(this, t);
    }
}









