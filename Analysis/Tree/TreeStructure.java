// TreeStructure.java, created Mon Apr  5 17:52:43 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.IR.Tree.Code; 
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CJUMP; 
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATUM; 
import harpoon.IR.Tree.ESEQ; 
import harpoon.IR.Tree.Exp; 
import harpoon.IR.Tree.ExpList; 
import harpoon.IR.Tree.EXP; 
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.JUMP; 
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM; 
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE; 
import harpoon.IR.Tree.NAME; 
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.OPER; 
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.SEQ; 
import harpoon.IR.Tree.Stm; 
import harpoon.IR.Tree.TEMP; 
import harpoon.IR.Tree.THROW;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.IR.Tree.UNOP; 
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.HashSet; 
import java.util.Iterator;
import java.util.Map;
import java.util.Set; 
import java.util.Stack; 

/**
 * The TreeStructure class is a wrapper around a CanonicalTreeCode that
 * allows the Tree to be manipulated more easily.  This class modifies 
 * the codeview directly, so should be used with caution.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: TreeStructure.java,v 1.1.2.11 2000-01-10 05:08:25 cananian Exp $
 */
public class TreeStructure { 
    private Map structure = new HashMap();

    /** 
     * Class constructor. 
     * <br><b>Requires:</b>
     *   <code>code</code> represents a canonical tree form.
     * <br><b>Effects:</b>
     *   Creates a <code>TreeStructure</code> which allows the user to 
     *   perform direct modifications of <code>code</code>. 
     */
    public TreeStructure(harpoon.IR.Tree.Code code) { 
	this((Stm)code.getRootElement()); 
    }

    /** 
     * Class constructor. 
     * <br><b>Requires:</b>
     *   <code>stm</code> is an element of a canonical tree form. 
     * <br><b>Effects:</b>
     *   Creates a <code>TreeStructure</code> which allows the user to 
     *   perform direct modifications of the subtree represented by 
     *   <code>stm</code>. 
     */
    public TreeStructure(harpoon.IR.Tree.Stm stm) { 
	// stm must be in canonical form. 
	//Util.assert
	//(((Code.TreeFactory)stm.getFactory()).getParent().getName().equals("canonical-tree")); 

    }

    
    /**
     * Returns the <code>Stm</code> in which <code>e</code> is used. 
     * <br><b>Requires:</b>
     * <code>e</code> is contained in this tree structure. 
     */ 
    public Stm getStm(Exp e) { 
	Tree result = e; 
	while (result instanceof Exp) { 
	    result = (Tree)this.structure.get(result); 
	    Util.assert(result != null); 
	}
	return (Stm)result; 
    }

    /** 
     * Removes the specified <code>Stm</code> from this 
     * tree structure. 
     * <br><b>Requires:</b>
     * <ol>
     *   <li><code>stm</code> is not of type <code>SEQ</code>. 
     *   <li><code>stm</code> is contained in this tree structure. 
     *   <li><code>stm</code> is not the root of the tree.  In general, this
     *       will be covered by requirement #1.  This rule is necessary to
     *       handle the special case in which trees consist of only one
     *       statement. 
     * </ol>
     * <br><b>Effects:</b>
     *   Removes <code>stm</code> from this tree structure. 
     */
    public void remove(Stm stm) { 
	Util.assert(stm.kind() != TreeKind.SEQ); 
	
	// All predecessors in canonical tree form must be SEQs
	SEQ pred = (SEQ)stm.getParent();
	Stm newPred;

	if (pred.getParent() == null) { 
	    // FIXME: The predecessor of "stm" is the root of this tree.  
	    // Since we don't have access to the tree code directly, we can't 
	    // update its root pointer as we'd like to here.  The best we can 
	    // do is to replace "stm" with a NOP.  This situation will 
	    // not come up very often, but it'd be nice if someone came up
	    // with a less hacky solution. 
	    TreeFactory tf = pred.getFactory(); 
	    this.replace(stm, new EXP(tf, pred, new CONST(tf, pred, 0)));
	}
	else { 
	    // Replace the predecessor of "stm" with the one remaining 
	    // successor. 
	    if      (pred.getLeft()==stm)  { newPred = pred.getRight(); } 
	    else if (pred.getRight()==stm) { newPred = pred.getLeft(); } 
	    else { throw new Error("Invalid tree form!"); }
	    this.replace(pred, newPred); 
	}
    }
    
    /**
     * Modifies the structure of the tree directly by replacing
     * <code>tOld</code> with <code>tNew</code>.  
     * 
     * <br><b>Requires:</b>
     * <ol>
     *   <li><code>tOld</code> exists in this tree structure. 
     *   <li><code>tNew</code>'s type is compatible with its
     *       position in the tree.  For instance, if <code>tOld</code>
     *       is a pointer to the exception handling code of some
     *       <code>CALL</code> object, then <code>tOld</code> must 
     *       be of type <code>NAME</code>. 
     *   <li><code>tOld</code> is not the root of the tree.  The
     *       behavior in this circumstance has not yet been defined. 
     *       Eventually, this restriction may be dropped. 
     * </ol>
     * <br><b>Effects:</b>
     *   Makes the necessary modifications to replace <code>tOld</code>
     *   with <code>tNew</code> in this tree structure. 
     * 
     */ 
    public void replace(Tree tOld, Tree tNew) { 
	new Replacer(tOld, tNew); 
    }

    /**
     * Visitor class to implement direct replacement of
     * on Tree with another.  
     */ 
    private class Replacer extends TreeVisitor { 
	private Tree tOld, tNew; 

	/**
	 * Class constructor. 
	 * @param tOld  the Tree object to be replaced. 
	 * @param tNew  the Tree object to replace tOld with. 
	 */ 
	public Replacer(Tree tOld, Tree tNew) { 
	    this.tOld = tOld;
	    this.tNew = tNew; 

	    // Find the Tree object which references tOld.  This should
	    // not be null unless: 
	    //
	    // 1) tOld is not found in the Tree.  This is a user error. 
	    // 2) tOld is the root of the Tree.  
	    //    FIXME: The behavior of Replacer in this situation 
	    //           needs to be defined!
	    // 
	    Tree parent = tOld.getParent(); 
	    if (parent == null) { 
		throw new Error("Could not replace " + tOld + " with " + tNew +
				" because " + tOld + " was not found."); 
	    }
	    else { 
		// Update the parent's references. 
		parent.accept(this); 
	    }
	}

	public void visit(BINOP e) { 
	    if      (e.getLeft() == this.tOld)  { e.setLeft((Exp)this.tNew); } 
	    else if (e.getRight() == this.tOld) { e.setRight((Exp)this.tNew); }
	    else { this.errCorrupt(); } 
	} 

	public void visit(CALL e) { 
	    if      (e.getRetex() == this.tOld) { e.setRetex((TEMP)this.tNew); }
 	    else if (e.getHandler() == this.tOld) { e.setHandler((NAME)this.tNew); } 
	    else { this.visit((INVOCATION)e); } 
	}

	public void visit(CJUMP e) { 
	    if (e.getTest() == this.tOld) { e.setTest((Exp)this.tNew); } 
	    else { this.errCorrupt(); } 
	}

	/** A CONST cannot be the parent of another node. */ 
	public void visit(CONST e) { this.errLeaf(e); }

	/** ESEQs cannot be members of a canonical tree form. */ 
	public void visit(ESEQ e) {
	    throw new Error("Tree structure is only for canonical trees!"); 
	}

	public void visit(INVOCATION e) { 
	    if (e.getFunc() == this.tOld) { e.setFunc((Exp)this.tNew); } 
	    else if (e.getRetval() == this.tOld) { e.setRetval((TEMP)this.tNew); } 
	    else { 
		ExpList newArgs = 
		    ExpList.replace(e.getArgs(), (Exp)this.tOld, (Exp)this.tNew); 
		e.setArgs(newArgs); 
	    }
	}

	public void visit(JUMP e) { 
	    if (e.getExp() == this.tOld) { e.setExp((Exp)this.tNew); } 
	    else { this.errCorrupt(); } 
	}

	/** A LABEL cannot be the parent of another node. */ 
	public void visit(LABEL e) { this.errLeaf(e); } 

	public void visit(MEM e) { 
	    if (e.getExp() == this.tOld) { e.setExp((Exp)this.tNew); } 
	    else { this.errCorrupt(); } 
	}

	public void visit(METHOD e) {
	    // tOld must be one of the METHOD parameters. 
	    TEMP[] params = e.getParams(); 
	    TEMP[] newParams = new TEMP[params.length]; 
	    System.arraycopy(params, 0, newParams, 0, params.length); 

	    for (int i=0; i<params.length; i++) { 
		if (params[i] == this.tOld) { 
		    newParams[i] = (TEMP)this.tNew; 
		    e.setParams(newParams); 
		    return; 
		}
	    }
	    this.errCorrupt(); 
	}

	public void visit(MOVE e) { 
	    if      (e.getSrc() == this.tOld) { e.setSrc((Exp)this.tNew); } 
	    else if (e.getDst() == this.tOld) { e.setDst((Exp)this.tNew); } 
	    else { this.errCorrupt(); } 
	}

	/** A NAME cannot be the parent of another node. */ 
	public void visit(NAME e) { this.errLeaf(e); } 

	public void visit(RETURN e) {
	    if (e.getRetval() == this.tOld) { e.setRetval((Exp)this.tNew); } 
	    else { this.errCorrupt(); } 
	}

	/** A SEGMENT cannot be the parent of another node. */ 
	public void visit(SEGMENT e) { this.errLeaf(e); }

	public void visit(SEQ e) {
	    if      (e.getLeft() == this.tOld)  { e.setLeft((Stm)this.tNew); } 
	    else if (e.getRight() == this.tOld) { e.setRight((Stm)this.tNew); }
	    else { this.errCorrupt(); } 
	}

	/** A TEMP cannot be the parent of another node. */
	public void visit(TEMP e) { this.errLeaf(e); } 

	public void visit(THROW e) {
	    if      (e.getRetex() == this.tOld)   { e.setRetex((Exp)this.tNew); } 
	    else if (e.getHandler() == this.tOld) { e.setHandler((Exp)this.tNew); } 
	    else { this.errCorrupt(); } 
	}

	public void visit(Tree t) { throw new Error("No defaults here!"); }

	public void visit(UNOP e) {
	    if (e.getOperand() == this.tOld) { e.setOperand((Exp)this.tNew); } 
	    else { this.errCorrupt(); } 
	}

	// Utility method:  called when the Tree structure has been
	// corrupted. 
	private void errCorrupt() { 
	    throw new Error("The tree structure has been corrupted."); 
	}

	// Utility method:  called when a leaf node is being treated
	// as a non-leaf node. 
	private void errLeaf(Tree t) { 
	    throw new Error
		("Attempted to treat: " + t + " as a non-leaf node."); 
	}
    }
}    


