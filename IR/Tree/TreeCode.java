// TreeCode.java, created Mon Feb 15 12:53:14 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu> 
// Licensed under the terms of the GNU GPL; see COPYING for details.
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
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/*
 * The <code>TreeCode</code> codeview exposes a tree-based representation.
 * This codeview serves primarily as an intermediate translation phase between 
 * <code>LowQuadNoSSA</code> and <code>CanonicalTreeCode</code>.  
 * In general, most analyses will be simpler in <code>CanonicalTreeCode</code>
 * because it has no <code>ESEQ</code>s.  
 * 
 * The tree form is based around Andrew Appel's tree form.  
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu> 
 * @version $Id: TreeCode.java,v 1.1.2.14 1999-08-04 06:31:00 cananian Exp $
 * 
 */
public class TreeCode extends Code {
    public  static   final   String     codename = "tree";
    private        /*final*/ Derivation derivation;
    private        /*final*/ TypeMap    typeMap;
  
    /** Create a new <code>TreeCode</code> from a
     *  <code>LowQuadNoSSA</code> object, and a <code>Frame</code>.
     */
    TreeCode(LowQuadNoSSA code, Frame topframe) {
	super(code.getMethod(), null, topframe);

	ToTree translator;

	translator = new ToTree(this.tf, code);
	tree       = translator.getTree();
	derivation = translator;
	typeMap    = translator;
    }

    private TreeCode(HMethod newMethod, Tree tree, Frame topframe) {
	super(newMethod, tree, topframe);
    }

    /** 
     * Clone this code representation. The clone has its own
     * copy of the tree structure. 
     */
    public HCode clone(HMethod newMethod, Frame frame) {
	TreeCode             tc  = new TreeCode(newMethod, null, frame); 
	final CloningTempMap ctm = new CloningTempMap
	    (this.tf.tempFactory(), tc.tf.tempFactory());
	Util.assert(tf.getFrame().regTempFactory() == this.getFrame().regTempFactory());


	tc.tree = (Tree)(Tree.clone(tc.tf, ctm, tree));
	tc.derivation = new Derivation() { 
	    public DList derivation(HCodeElement hce, Temp t) { 
		return this.derivation(hce, t==null?null:ctm.tempMap(t));
	    }
	};

	tc.typeMap    = new TypeMap() { 
	    public HClass typeMap(HCode hc, Temp t) { 
		return this.typeMap(hc, t==null?null:ctm.tempMap(t));
	    }
	};

	return tc;
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"tree"</code>.
     */
    public String getName() { return codename; }

    /** @return false */
    public boolean isCanonical() { return false; } 

    /** 
     * This operation is not supported in non-canonical tree forms.
     * @exception UnsupportedOperationException always.
     */
    public void recomputeEdges() { throw new UnsupportedOperationException(); }

    /**
     * Returns a code factory for <code>TreeCode</code>, given a 
     * code factory for <code>LowQuadNoSSA</code>.
     * <BR> <B>effects:</B> if <code>hcf</code> is a code factory for
     *      <code>LowQuadNoSSA</code>, then creates and returns a code
     *      factory for <code>TreeCode</code>.  Else passes
     *      <code>hcf</code> to
     *      <code>LowQuadNoSSA.codeFactory()</code>, and reattempts to
     *      create a code factory for <code>TreeCode</code> from the
     *      code factory returned by <code>LowQuadNoSSA</code>.
     * @see LowQuadNoSSA#codeFactory(HCodeFactory)
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf, 
					   final Frame frame) {
	if (hcf.getCodeName().equals(LowQuadNoSSA.codename)) {
	    return new HCodeFactory() { 
		public HCode convert(HMethod m) { 
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new TreeCode((LowQuadNoSSA)c, frame);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else {
	    //   throw new Error("don't know how to make " + codename +
	    //	    " from " + hcf.getCodeName());
	    HCodeFactory lqnossaHCF = LowQuadNoSSA.codeFactory(hcf);
	    return codeFactory(lqnossaHCF, frame);
	}
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
	    (codeFactory(new harpoon.Backend.StrongARM.SAFrame())); 
    }

    /**
     * Implementation of the <code>Derivation</code> interface.
     */
    public DList derivation(HCodeElement hce, Temp t) {
	return derivation.derivation(hce, t);
    }

    /**
     * Implementation of the <code>TypeMap</code> interface.
     */
    public HClass typeMap(HCode hc, Temp t) {
	// Ignores hc parameter
	return typeMap.typeMap(this, t);
    }
}
