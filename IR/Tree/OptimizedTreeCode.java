// OptimizedTreeCode.java, created Thu Jul  8  2:43:29 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Stm;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * The <code>OptimizedTreeCode</code> codeview is an optimized,
 * canonical representation of Tree form.  It provides a code factory
 * that will generate tree code optimized with a set of standard passes,
 * and a code factory that allows for specifying your own set of optimization
 * passes. 
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: OptimizedTreeCode.java,v 1.2.2.1 2002-03-14 01:57:48 cananian Exp $
 */
public class OptimizedTreeCode extends Code {
    public static final String codename = CanonicalTreeCode.codename;
    private static final TreeOptimizer[] standard_opts = {
	// Add all standard optimization passes here
	/*	new TreeOptimizer() { 
	    public CanonicalTreeCode optimize(CanonicalTreeCode code) { 
		code = (CanonicalTreeCode)code.clone
		   (code.getMethod(), code.getFrame());
		return (CanonicalTreeCode)new TreeFolding(code).fold();
	    }
	    }*/ 
    };
    private final TreeDerivation treeDerivation;
  
    /** Create a new <code>OptimizedTreeCode</code> from a
     *  <code>CanonicalTreeCode</code> object, a <code>Frame</code>,
     *  and a set of optimizations to perform. 
     */
    OptimizedTreeCode(CanonicalTreeCode code, final Frame frame, 
		      final TreeOptimizer[] topts) {
	super(code.getMethod(), null, frame);

	/* Optimize "code" */
	for (int i=0; i<topts.length; i++) { 
	    code = topts[i].optimize(code);
	}
	final CanonicalTreeCode optimizedCode = code;

	this.treeDerivation = optimizedCode.getTreeDerivation();
	this.tree       = (Stm)optimizedCode.getRootElement();

    }

    // copy constructor; should be only called by the clone() method.
    private OptimizedTreeCode(HMethod newMethod, Tree tree, Frame frame,
			      TreeDerivation treeDerivation) {
	super(newMethod, tree, frame);
	this.treeDerivation = treeDerivation;
    }

    /** 
     * Clone this code representation. The clone has its own
     * copy of the tree structure. 
     */
    public HCodeAndMaps clone(HMethod newMethod, Frame frame) {
	DerivationGenerator dg =
	    (getTreeDerivation()==null) ? null : new DerivationGenerator();
	OptimizedTreeCode tc = new OptimizedTreeCode(newMethod,null,frame,dg);
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
     * Return a code factory for <code>OptimizedTreeCode</code>, given a 
     * code factory for <code>CanonicalTreeCode</code>.  This code factory
     * performs a standard set of optimizations on the 
     * <code>CanonicalTreeCode</code>.
     */
    public static HCodeFactory codeFactory(final HCodeFactory hcf, 
					   final Frame frame) { 
	return codeFactory(hcf, frame, standard_opts);
    }

    /**
     * Return a code factory for <code>OptimizedTreeCode</code>, given a 
     * code factory for <code>CanonicalTreeCode</code>, a <code>Frame</code>,
     * and a set of optimizations to perform. 
     */    
    public static HCodeFactory codeFactory(final HCodeFactory hcf, 
					   final Frame frame,
					   final TreeOptimizer[] topts) { 
	if (hcf.getCodeName().equals(CanonicalTreeCode.codename)) {
	    return new HCodeFactory() { 
		public HCode convert(HMethod m) { 
		    HCode c = hcf.convert(m);
		    return (c==null) ? null :
			new OptimizedTreeCode
			((CanonicalTreeCode)c, frame, topts);
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
	return codeFactory(CanonicalTreeCode.codeFactory(frame), frame);
    }

    public TreeDerivation getTreeDerivation() { return treeDerivation; }

    public interface TreeOptimizer { 
	public CanonicalTreeCode optimize(CanonicalTreeCode code); 
    }
}










