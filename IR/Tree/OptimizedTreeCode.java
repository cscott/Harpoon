package harpoon.IR.Tree;

import harpoon.Analysis.Tree.TreeFolding;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
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
 * @version $Id: OptimizedTreeCode.java,v 1.1.2.5 1999-07-30 20:41:35 duncan Exp $
 */
public class OptimizedTreeCode extends Code {
    public static final String codename = "optimized-tree";
    private static final TreeOptimizer[] standard_opts = {
	// Add all standard optimization passes here
	new TreeOptimizer() { 
	    public CanonicalTreeCode optimize(CanonicalTreeCode code) { 
		code = (CanonicalTreeCode)code.clone
		   (code.getMethod(), code.getFrame());
		return (CanonicalTreeCode)new TreeFolding(code).fold();
	    }
	}
    };

    private final Derivation       derivation;
    private final EdgeInitializer  edgeInitializer;
    private final TypeMap          typeMap;
  
    /** Create a new <code>OptimizedTreeCode</code> from a
     *  <code>CanonicalTreeCode</code> object, a <code>Frame</code>,
     *  and a set of optimizations to perform. 
     */
    OptimizedTreeCode(/*final*/ CanonicalTreeCode code, final Frame frame, 
		      final TreeOptimizer[] topts) {
	super(code.getMethod(), null, frame);

	/* Optimize "code" */
	for (int i=0; i<topts.length; i++) { 
	    code = topts[i].optimize(code);
	}
	final CanonicalTreeCode optimizedCode = code;

	this.derivation = new Derivation() { 
	    public DList derivation(HCodeElement hce, Temp t) { 
		return optimizedCode.derivation(hce, t);
	    }
	};
	this.typeMap = new TypeMap() { 
	    public HClass typeMap(HCode hc, Temp t) { 
		return optimizedCode.typeMap(hc, t);
	    }
	};
	this.tree = (Stm)code.getRootElement();

	this.edgeInitializer = new EdgeInitializer();
	this.edgeInitializer.computeEdges();
    }

    private OptimizedTreeCode(HMethod newMethod, Tree tree, Frame frame) {
	super(newMethod, tree, frame);
	final CloningTempMap ctm = 
	    new CloningTempMap
	    (tree.getFactory().tempFactory(), this.tf.tempFactory());
	final CanonicalTreeCode code = 
	    (CanonicalTreeCode)tree.getFactory().getParent();
	this.tree = (Tree)Tree.clone(this.tf, ctm, tree);
	(this.edgeInitializer = new EdgeInitializer()).computeEdges();
	
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
	return new OptimizedTreeCode(newMethod, this.tree, frame); 
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

    public interface TreeOptimizer { 
	public CanonicalTreeCode optimize(CanonicalTreeCode code); 
    }
}










