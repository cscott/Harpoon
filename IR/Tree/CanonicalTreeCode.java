package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * The <code>CanonicalTreeCode</code> codeview is the same as 
 * the <Code>TreeCode</code> codeview, except for the fact that it 
 * does not allow <code>ESEQ</code> objects to be part of its representation.
 * There is seldom a compelling reason not to use the canonical tree view,
 * as <code>ESEQ</code>s complicate analysis, while providing no real benefits.
 *
 * The <code>CanonicalTreeCode</code> is based around Andrew Appel's 
 * canonical tree form.
 * 
 * @author   Duncan Bryce <duncan@lcs.mit.edu>
 * @version  $Id: CanonicalTreeCode.java,v 1.1.2.6 1999-07-27 18:48:58 duncan Exp $
 * 
 */
public class CanonicalTreeCode extends Code {
    public  static   final String           codename = "canonical-tree";
    private          final Derivation       derivation;
    private          final EdgeInitializer  edgeInitializer;
    private          final TypeMap          typeMap;

    /** Create a new <code>CanonicalTreeCode</code> from a
     *  <code>TreeCode</code> object, and a <code>Frame</code>.
     */
    CanonicalTreeCode(TreeCode code, Frame frame) {
	super(code.getMethod(), null, frame);

	ToCanonicalTree translator;

	translator   = new ToCanonicalTree(this.tf, code);
	tree         = translator.getTree();
	derivation   = translator;
	typeMap      = translator;

	// Compute edges for the Trees in this codeview
	(edgeInitializer = new EdgeInitializer()).computeEdges();
    }

    /* Copy constructor, should only be called by the clone() method. */
    private CanonicalTreeCode(HMethod newMethod, Tree tree, Frame frame) {
	super(newMethod, tree, frame);
	final CloningTempMap ctm = 
	    new CloningTempMap
	    (tree.getFactory().tempFactory(), this.tf.tempFactory());
	final CanonicalTreeCode code = 
	    (CanonicalTreeCode)tree.getFactory().getParent();
	this.tree = (Tree)Tree.clone(this.tf, ctm, tree);
	(this.edgeInitializer = new EdgeInitializer()).computeEdges();

	// Must update the temps in your frame when you clone the tree form
	// Failure to do this causes an inconsistency between the new temps
	// created for the new frame, and the frame's registers mapped
	// using ctm in Tree.clone(). 
	Temp[] oldTemps = tree.getFactory().getFrame().getAllRegisters();
	Temp[] newTemps = this.tf.getFrame().getAllRegisters();
	for (int i=0; i<oldTemps.length; i++) 
	    newTemps[i] = oldTemps[i]==null?null:ctm.tempMap(oldTemps[i]);
	
	this.derivation = new Derivation() { 
	    public DList derivation(HCodeElement hce, Temp t) { 
		return code.derivation(hce, t==null?null:ctm.tempMap(t));
	    }
	};

	this.typeMap    = new TypeMap() { 
	    public HClass typeMap(HCode hc, Temp t) { 
		return code.typeMap(hc, t==null?null:ctm.tempMap(t));
	    }
	};
	
    }

    /** 
     * Clone this code representation. The clone has its own
     * copy of the tree structure. 
     */
    public HCode clone(HMethod newMethod, Frame frame) {
	return new CanonicalTreeCode(newMethod, this.tree, frame); 
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"canonical-tree"</code>.
     */
    public String getName() { return codename; }

    /** @return true */
    public boolean isCanonical() { return true; } 

    /** 
     * Recomputes the control-flow graph exposed through this codeview
     * by the <code>HasEdges</code> interface of its elements.  
     * This method should be called whenever the tree structure of this
     * codeview is modified. 
     */
    public void recomputeEdges() { edgeInitializer.computeEdges(); }

    /**
     * Return a code factory for <code>CanonicalTreeCode</code>, given a 
     * code factory for <code>TreeCode</code>
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
     * Return a code factory for <code>CanonicalTreeCode</code>, 
     * using the default code factory for <code>TreeCode</code>
     */
    public static HCodeFactory codeFactory(final Frame frame) {  
	return codeFactory(TreeCode.codeFactory(frame), frame);
    }

    // obsolete (may not even work with null frame)
    public static void register() { HMethod.register(codeFactory(null)); }

    /**
     * Implementation of the <code>Derivation</code> interface.
     */
    public DList derivation(HCodeElement hce, Temp t){
	return derivation.derivation(hce, t);
    }

    /**
     * Implementation of the <code>Typemap<code> interface.
     */
    public HClass typeMap(HCode hc, Temp t) {
	// Ignores hc parameter
	return typeMap.typeMap(this, t);
    }
}









