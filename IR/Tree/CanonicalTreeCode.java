// CanonicalTreeCode.java, created Mon Mar 29  0:07:41 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Analysis.Tree.Canonicalize;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
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
 * @version  $Id: CanonicalTreeCode.java,v 1.2 2002-02-25 21:05:30 cananian Exp $
 * 
 */
public class CanonicalTreeCode extends Code {
    private final static boolean useOldCanonicalize = false;
    /** The <code>getName()</code> method return <code>codename</code>,
     *  which is "canonical-tree" for this <code>Code</code>. */
    public  static   final String           codename = "canonical-tree";
    private          final TreeDerivation   treeDerivation;

    /** Create a new <code>CanonicalTreeCode</code> from a
     *  <code>TreeCode</code> object, and a <code>Frame</code>.
     */
    CanonicalTreeCode(TreeCode code, Frame frame) {
	super(code.getMethod(), null, frame);

	if (useOldCanonicalize) {
	    ToCanonicalTree translator;
	    translator   = new ToCanonicalTree(this.tf, code);
	    tree         = translator.getTree();
	    treeDerivation = translator.getTreeDerivation();
	} else {
	    DerivationGenerator dg = (code.getTreeDerivation()==null) ? null :
		new DerivationGenerator();
	    tree = Tree.clone(this.tf, code.tree, (dg==null) ? null :
			      dg.cloneCallback(code.getTreeDerivation()));
	    Canonicalize.simplify((Stm)tree, dg, Canonicalize.RULES);
	    treeDerivation = dg;
	}
	Util.assert(!harpoon.Analysis.Tree.Canonicalize.containsEseq(tree));
    }

    /* Copy constructor, should only be called by the clone() method. */
    private CanonicalTreeCode(HMethod newMethod, Tree tree, Frame frame,
			      TreeDerivation treeDerivation) {
	super(newMethod, tree, frame);
	this.treeDerivation = treeDerivation;
    }

    /** Returns a <code>TreeDerivation</code> object for the
     *  generated <code>Tree</code> form. */
    public TreeDerivation getTreeDerivation() { return treeDerivation; }

    /** 
     * Clone this code representation. The clone has its own
     * copy of the tree structure. 
     */
    public HCodeAndMaps clone(HMethod newMethod, Frame frame) {
	DerivationGenerator dg =
	    (getTreeDerivation()==null) ? null : new DerivationGenerator();
	CanonicalTreeCode tc = new CanonicalTreeCode(newMethod,null,frame,dg);
	return cloneHelper(tc, dg);
    }

    /**
     * Return the name of this code view.
     * @return the string <code>"canonical-tree"</code>.
     */
    public String getName() { return codename; }

    /** @return true */
    public boolean isCanonical() { return true; } 

    /**
     * Return a code factory for <code>CanonicalTreeCode</code>, given a 
     * code factory for <code>TreeCode</code>.
     * <BR> <B>effects:</B> if <code>hcf</code> is a code factory for
     *      <code>TreeCode</code>, then creates and returns a code
     *      factory for <code>CanonicalTreeCode</code>.  Else passes
     *      <code>hcf</code> to
     *      <code>TreeCode.codeFactory()</code>, and reattempts to
     *      create a code factory for <code>CanonicalTreeCode</code> from the
     *      code factory returned by <code>TreeCode</code>.
     * @see TreeCode#codeFactory(HCodeFactory, Frame)
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf, 
					   final Frame frame) {
	if (hcf.getCodeName().equals(TreeCode.codename)) {
	    // note that result will not be serializable unless frame is.
	    return new harpoon.ClassFile.SerializableCodeFactory() { 
		public HCode convert(HMethod m) { 
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new CanonicalTreeCode((TreeCode)c, frame);
		}
		public void clear(HMethod m) { hcf.clear(m); }
		public String getCodeName() { return codename; }
	    };
	} else {
	    HCodeFactory treeCodeHCF=TreeCode.codeFactory(hcf, frame);
	    return codeFactory( treeCodeHCF, frame );
	}
    }
  
    /**
     * Return a code factory for <code>CanonicalTreeCode</code>, 
     * using the default code factory for <code>TreeCode</code>
     */
    public static HCodeFactory codeFactory(final Frame frame) {  
	return codeFactory(TreeCode.codeFactory(frame), frame);
    }
}









