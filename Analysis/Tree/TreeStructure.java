// TreeStructure.java, created Wed May  5 17:52:43 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.EXP;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * The TreeStructure class is a wrapper around a CanonicalTreeCode that
 * allows the Tree to be manipulated more easily.  This class modifies 
 * the codeview directly, so should be used with caution.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: TreeStructure.java,v 1.1.2.3 1999-08-04 05:52:24 cananian Exp $
 */
public class TreeStructure { 
    private Map structure = new HashMap();

    /** Class constructor. */
    public TreeStructure(harpoon.IR.Tree.Code code) { 
	Util.assert(code.isCanonical());
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
	// Util.assert(!(stm instanceof SEQ)); // otherwise nonsensical
	Util.assert(structure.containsKey(stm));

	// All predecessors in canonical tree form must be SEQs
	SEQ pred  = getPredecessor(stm);
	Stm newPred;

	if (!structure.containsKey(pred)) { // Insert NOP
	    newPred = new EXP
		    (pred.getFactory(), pred, 
		     new CONST(pred.getFactory(), pred, 0));
	    if (pred.left==stm)       pred.left  = newPred;
	    else if (pred.right==stm) pred.right = newPred;
	    else throw new Error("Tree structure has been corrupted!");
	}
	else { 
	    SEQ ppred = getPredecessor(pred);
	    
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

    private void buildTreeStructure(harpoon.IR.Tree.Code code) { 
	TreeStructureVisitor tsv = new TreeStructureVisitor(structure);
	for (Iterator i = code.getElementsI(); i.hasNext();) { 
	    ((Tree)i.next()).visit(tsv);
	}
    }

    // Maps all Stms in the tree to their predecessors
    class TreeStructureVisitor extends TreeVisitor { 
	private Map structure;

	TreeStructureVisitor(Map structure) { 
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
    


