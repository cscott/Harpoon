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
import harpoon.IR.Tree.DATA; 
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
 * @version $Id: TreeStructure.java,v 1.1.2.9 1999-12-20 09:28:53 duncan Exp $
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
	Util.assert
	    (((Code.TreeFactory)stm.getFactory()).getParent().getName().equals("canonical-tree")); 

	// Fill in the mappings necessary to represent this tree structure. 
	new StructureBuilder(stm, this.structure); 
    }

    /**
     * Returns the direct parent of <code>e</code> in this tree structure. 
     * <br><b>Requires:</b>
     * <code>e</code> is contained in this tree structure. 
     */ 
    public Tree getParent(Exp e) { 
	return (Tree)this.structure.get(e);
    }

    /**
     * Returns the direct parent of <code>s</code> in this tree structure.  
     * The parent of a <code>Stm</code> is always a <code>SEQ</code> object. 
     * <br>b>Requires:</b>
     * <ol>
     *   <li><code>s</code> is contained in this tree structure.
     *   <li><code>s</code> is not the root of this tree structure. 
     * </ol> 
     */ 
    public SEQ getParent(Stm s) { 
	return (SEQ)this.structure.get(s);
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
     *       will be covered by requirement #2.  This rule is necessary to
     *       handle the special case in which trees consist of only one
     *       statement. 
     * </ol>
     * <br><b>Effects:</b>
     *   Removes <code>stm</code> from this tree structure. 
     */
    public void remove(Stm stm) { 
	Util.assert(stm.kind() != TreeKind.SEQ); 
	Util.assert(structure.containsKey(stm));

	// All predecessors in canonical tree form must be SEQs
	SEQ pred = this.getParent(stm);
	Stm newPred;

	if (!structure.containsKey(pred)) { 
	    // FIXME: The predecessor of "stm" is the root of this tree.  
	    // Since we don't have access to the tree code directly, we can't 
	    // update its root pointer as we'd like to here.  The best we can 
	    // do is to replace "stm" with a NOP. 
	    TreeFactory tf = pred.getFactory(); 
	    this.replace(stm, new EXP(tf, pred, new CONST(tf, pred, 0)));
	}
	else { 
	    // Replace the predecessor of "stm" with the one remaining 
	    // successor. 
	    if      (pred.left==stm)  { newPred = pred.right; } 
	    else if (pred.right==stm) { newPred = pred.left; } 
	    else { throw new Error("Tree structure has been corrupted!"); }
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
	    Tree parent = (Tree)TreeStructure.this.structure.get(tOld); 
	    if (parent == null) { 
		throw new Error("Could not replace " + tOld + " with " + tNew +
				" because " + tOld + " was not found."); 
	    }
	    else { 
		// Update the parent's references. 
		parent.accept(this); 
		// Update the structure map to reflect this change. 
		new StructureDeleter(tOld, TreeStructure.this.structure); 
		new StructureBuilder(tNew, TreeStructure.this.structure); 
		TreeStructure.this.structure.put(tNew, parent); 
	    }
	}

	public void visit(BINOP e) { 
	    if      (e.left == this.tOld)  { e.left  = (Exp)this.tNew; } 
	    else if (e.right == this.tOld) { e.right = (Exp)this.tNew; } 
	    else { this.errCorrupt(); } 
	} 

	public void visit(CALL e) { 
	    if      (e.retex == this.tOld)   { e.retex   = (TEMP)this.tNew; } 
	    else if (e.handler == this.tOld) { e.handler = (NAME)this.tNew; } 
	    else { this.visit((INVOCATION)e); } 
	}

	public void visit(CJUMP e) { 
	    if (e.test == this.tOld) { e.test = (Exp)this.tNew; } 
	    else { this.errCorrupt(); } 
	}

	/** A CONST cannot be the parent of another node. */ 
	public void visit(CONST e) { this.errLeaf(e); }

	/** ESEQs cannot be members of a canonical tree form. */ 
	public void visit(ESEQ e) {
	    throw new Error("Tree structure is only for canonical trees!"); 
	}

	public void visit(INVOCATION e) { 
	    if (e.func == this.tOld) { e.func = (Exp)this.tNew; } 
	    else if (e.retval == this.tOld) { e.retval = (TEMP)this.tNew; } 
	    else { 
		ExpList newArgs = 
		    ExpList.replace(e.args, (Exp)this.tOld, (Exp)this.tNew); 
		e.args = newArgs; 
	    }
	}

	public void visit(JUMP e) { 
	    if (e.exp == this.tOld) { e.exp = (Exp)this.tNew; } 
	    else { this.errCorrupt(); } 
	}

	/** A LABEL cannot be the parent of another node. */ 
	public void visit(LABEL e) { this.errLeaf(e); } 

	public void visit(MEM e) { 
	    if (e.exp == this.tOld) { e.exp = (Exp)this.tNew; } 
	    else { this.errCorrupt(); } 
	}

	public void visit(METHOD e) {
	    // tOld must be one of the METHOD parameters. 
	    for (int i=0; i<e.params.length; i++) { 
		if (e.params[i] == this.tOld) { 
		    e.params[i] = (TEMP)this.tNew; 
		    return; 
		}
	    }
	    this.errCorrupt(); 
	}

	public void visit(MOVE e) { 
	    if      (e.src == this.tOld) { e.src = (Exp)this.tNew; } 
	    else if (e.dst == this.tOld) { e.dst = (Exp)this.tNew; } 
	    else { this.errCorrupt(); } 
	}

	/** A NAME cannot be the parent of another node. */ 
	public void visit(NAME e) { this.errLeaf(e); } 

	public void visit(RETURN e) {
	    if (e.retval == this.tOld) { e.retval = (Exp)this.tNew; } 
	    else { this.errCorrupt(); } 
	}

	/** A SEGMENT cannot be the parent of another node. */ 
	public void visit(SEGMENT e) { this.errLeaf(e); }

	public void visit(SEQ e) {
	    if      (e.left == this.tOld)  { e.left  = (Stm)this.tNew; } 
	    else if (e.right == this.tOld) { e.right = (Stm)this.tNew; } 
	    else { this.errCorrupt(); } 
	}

	/** A TEMP cannot be the parent of another node. */
	public void visit(TEMP e) { this.errLeaf(e); } 

	public void visit(THROW e) {
	    if      (e.retex == this.tOld)   { e.retex = (Exp)this.tNew; } 
	    else if (e.handler == this.tOld) { e.handler = (Exp)this.tNew; } 
	    else { this.errCorrupt(); } 
	}

	public void visit(Tree t) { throw new Error("No defaults here!"); }

	public void visit(UNOP e) {
	    if (e.operand == this.tOld) { e.operand = (Exp)this.tNew; } 
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

    /** 
     * Class to construct the map of back-pointers that represents the 
     * tree structure. 
     */ 
    private class StructureBuilder extends TreeVisitor { 
	private Map   structure; 
	private Stack worklist = new Stack(); 

	/** 
	 * Class constructor.  
	 * @param root      the root of the tree to make a structure for.
	 * @param structure a map to hold the generated structure. 
	 */ 
	public StructureBuilder (Tree root, Map structure) { 
	    this.structure = structure; 
	    this.worklist.add(root); 
	    while (!worklist.isEmpty())
		((Tree)worklist.pop()).accept(this); 
	}

	/** kids() not applicable to SEQ.  Need a special case. */ 
	public void visit(SEQ s) { 
	    Util.assert(!this.structure.containsKey(s.left)); 
	    Util.assert(!this.structure.containsKey(s.right)); 

	    this.worklist.push(s.left); 
	    this.worklist.push(s.right);
	    this.structure.put(s.left, s);
	    this.structure.put(s.right, s); 
	}

	public void visit(Tree t) { 
	    for (ExpList e = t.kids(); e != null; e = e.tail) { 
		Util.assert(!this.structure.containsKey(e.head)); 
		this.worklist.push(e.head); 
		this.structure.put(e.head, t); 
	    }
	}
    }

    private class StructureDeleter extends TreeVisitor { 
	private Map   structure; 
	private Stack worklist = new Stack(); 

	/** 
	 * Class constructor.  
	 * @param root      the root of the tree to make a structure for.
	 * @param structure a map to hold the generated structure. 
	 */ 
	public StructureDeleter(Tree root, Map structure) { 
	    this.structure = structure; 
	    this.worklist.add(root); 
	    while (!worklist.isEmpty())
		((Tree)worklist.pop()).accept(this); 
	}

	/** kids() not applicable to SEQ.  Need a special case. */ 
	public void visit(SEQ s) { 
	    this.structure.remove(s); 
	    this.worklist.push(s.left); 
	    this.worklist.push(s.right);
	}

	public void visit(Tree t) { 
	    this.structure.remove(t); 
	    for (ExpList e = t.kids(); e != null; e = e.tail) { 
		this.worklist.push(e.head); 
		this.structure.remove(e.head); 
	    }
	}
    }
}    


