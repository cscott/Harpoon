package harpoon.Analysis.Tree;

import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The TreeStructure class is a wrapper around a CanonicalTreeCode that
 * allows the Tree to be manipulated more easily.  This class modifies 
 * the codeview directly, so should be used with caution.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: TreeStructure.java,v 1.1.2.1 1999-04-05 21:52:43 duncan Exp $
 */
public class TreeStructure { 
    private Hashtable structure = new Hashtable();
    
    /** Class constructor. */
    public TreeStructure(CanonicalTreeCode code) { 
	buildTreeStructure(code);
    }

    /** Returns the direct predecessor of the specified <code>Stm</code> */
    public SEQ getPredecessor(Stm stm) { 
	return (SEQ)structure.get(stm);
    }

    /** Removes the specified <code>Stm</code> from this 
     *  <code>TreeStructure</code>.  Fails if <code>stm</code> is 
     *  a <code>SEQ</code> object, as the meaning of the resulting 
     *  tree would be undefined. */
    public void remove(Stm stm) { 
        Util.assert(structure.containsKey(stm));  
	Util.assert(structure.containsKey(structure.get(stm)));
	Util.assert(!(stm instanceof SEQ)); // otherwise nonsensical

	// All predecessors in canonical tree form must be SEQs
	SEQ pred  = getPredecessor(stm);
	SEQ ppred = getPredecessor(pred);
	Stm newPred;

	// "pred" will be replaced with its other branch 
	if (pred.left==stm) 
	    newPred = pred.right;
	else if (pred.right==stm) 
	    newPred = pred.left;
	else 
	    throw new Error("Tree structure has been corrupted!");

	if (ppred.left==pred)
	    ppred.left = newPred;
	else if (ppred.right==pred)
	    ppred.right = newPred;
	else
	    throw new Error("Tree structure has been corrupted!");

	structure.remove(stm);
	structure.put(newPred, ppred);
    }
    
    /** Replaces <code>sOld</code> with <code>sNew</code> in this 
     *  <code>TreeStructure</code>.  Neither parameter may be a
     *  <code>SEQ</code>, as the meaning of the resulting tree would
     *  be undefined. */
    public void replace(Stm sOld, Stm sNew) { 
	Util.assert(!(sOld instanceof SEQ));
	Util.assert(!(sNew instanceof SEQ));

	SEQ pred = getPredecessor(sOld);
	
	if (pred.left==sOld)
	    pred.left = sNew;
	else if (pred.right==sOld)
	    pred.right = sNew;
	else
	    throw new Error("Tree structure is corrupt.");

	structure.put(sNew, pred);
	structure.remove(sOld);
    }

    private void buildTreeStructure(CanonicalTreeCode code) { 
	TreeStructureVisitor tsv = new TreeStructureVisitor(structure);
	for (Enumeration e = code.getElementsE(); e.hasMoreElements();) { 
	    ((Tree)e.nextElement()).visit(tsv);
	}
    }

    // Maps all Stms in the tree to their predecessors
    class TreeStructureVisitor extends TreeVisitor { 
	private Hashtable structure;

	TreeStructureVisitor(Hashtable structure) { 
	    this.structure = structure;
	}

	public void visit(Tree tree) { 
	    throw new Error("No defaults here.");
	}

	public void visit(Exp exp) { /* Do nothing for Exps */ } 
	
	public void visit(Stm stm) { /* Only SEQs impact tree structure */ }

	public void visit(SEQ seq) { 
	    structure.put(seq.left, seq);
	    structure.put(seq.right, seq);
	}
    }
}
    


