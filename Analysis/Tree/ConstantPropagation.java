// ConstantPropagation.java, created Sun Dec  5  1:26:37 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Tree;

import harpoon.Analysis.BasicBlock; 
import harpoon.Analysis.DataFlow.ReachingDefs; 
import harpoon.Analysis.DataFlow.ReachingHCodeElements; 
import harpoon.Analysis.DataFlow.Solver; 
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Tree.Code;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.ESEQ; 
import harpoon.IR.Tree.Exp; 
import harpoon.IR.Tree.ExpList; 
import harpoon.IR.Tree.INVOCATION; 
import harpoon.IR.Tree.METHOD; 
import harpoon.IR.Tree.MOVE; 
import harpoon.IR.Tree.NAME; 
import harpoon.IR.Tree.SEQ; 
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TEMP; 
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeKind;
import harpoon.IR.Tree.TreeVisitor; 
import harpoon.Temp.Temp; 
import harpoon.Util.CloneableIterator; 
import harpoon.Util.Util;

import java.util.HashMap; 
import java.util.HashSet;
import java.util.Iterator; 
import java.util.Map;
import java.util.Set; 
import java.util.Stack; 

/**
 * <code>ConstantPropagation</code> performs constant propagation on 
 * canonical trees.  
 * 
 * <p><b>CAUTION</b>: it modifies code in-place.
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ConstantPropagation.java,v 1.1.2.17 2001-06-17 22:31:58 cananian Exp $
 */
public final class ConstantPropagation { 

    // Prevent instantiation. 
    private ConstantPropagation() { } 

    /** 
     * Performs the constant propagation transformation on <code>code</code>. 
     * 
     * <br><b>Requires:</b>
     *   <code>code</code> is in canonical form.
     * <br><b>Modifies:</b>
     *   <code>code</code>
     * <br><b>Effects:</b>
     *   Performs constant propagation on <code>code</code> in-place. 
     */ 
    public static void optimize(Code code) { 
	ConstPropVisitor cpv = new ConstPropVisitor(code); 
    }

    /** Class to do constant propagation. */
    private static class ConstPropVisitor extends TreeVisitor { 
	// Maps temps to the Stms that define them. 
	private Map                   tempsToDefs = new HashMap(); 
	// Worklist containing the next Trees to process. 
	private Stack                 worklist    = new Stack(); 
	// Class to perform reaching definitions analysis. 
	private ReachingHCodeElements rch; 
	// The tree code we are modifying
	private Code                  code;

	/** Class constructor. */ 
	public ConstPropVisitor(Code code) { 
	    this.code = code; 

	    // Initialize mapping of temps to the Stms that define them. 
	    mapTempsToDefs(code); 

	    // Perform reaching definitions analysis on the tree code. 
	    BasicBlock.Factory bbf = new BasicBlock.Factory
		(code, code.getGrapher());

	    this.rch = new ReachingHCodeElements(bbf);
	    Solver.forwardRPOSolve(bbf.getRoot(), this.rch); 


	    // Traverse the tree. 
	    this.worklist.push(code.getRootElement()); 
	    while (!worklist.isEmpty()) { 
		Tree t = (Tree)worklist.pop(); 
		t.accept(this); 
	    }
	}

	/** Throws an error: ESEQ is not part of canonical trees */ 
	public void visit(ESEQ e) { 
	    throw new Error
		("Constant propagation only works on canonical tree form!"); 
	}
	
	public void visit(Exp e) { 
	    for (ExpList el = e.kids(); el != null; el = el.tail) { 
		this.worklist.push(el.head); 
	    }
	}

	public void visit(INVOCATION i) { 
	    // Only the function pointer and the arguments could conceivably
	    // be replaced by this transformation. 
	    this.worklist.push(i.getFunc()); 
	    for (ExpList el = i.getArgs(); el != null; el = el.tail) { 
		this.worklist.push(el.head); 
	    }
	}

	public void visit(METHOD m) { /* Don't need to replace parameters. */ }

	public void visit(MOVE m) { 
	    // If dst is a TEMP, can't replace it because we're writing to it. 
	    if (m.getDst().kind() != TreeKind.TEMP) { this.worklist.push(m.getSrc()); }
	    this.worklist.push(m.getSrc()); 
	}

	public void visit(SEQ s) { 
	    this.worklist.push(s.getLeft()); 
	    this.worklist.push(s.getRight()); 
	}
	
	public void visit(Stm s) { 
	    for (ExpList e = s.kids(); e != null; e = e.tail) { 
		this.worklist.push(e.head); 
	    }
	}

	/** 
	 * Determines whether "t" is constant.  If so, replaces "t"
	 * with the appropriate constant. 
	 */ 
	public void visit(TEMP t) { 
	    // The statement in which this TEMP is used.
	    Stm parent       = STM(t); 
	    // The reaching definition information on entry to "parent". 
	    Set reachingDefs = this.rch.getReachingBefore(parent); 
	    // The set of Stms which define this t.temp. 
	    Set tDefs = (Set)this.tempsToDefs.get(t.temp); 
	    // The set of Stms which define this temp that reach "parent". 
	    // This set MUST be non-empty (or we'd have a USE with no DEF). 
	    tDefs.retainAll(reachingDefs); 
	    Util.assert(!tDefs.isEmpty()); 

	    // Initially assume that we can replace "t" with a constant. 
	    boolean replaceT = true; 
	    // We don't yet know the value of "t".  
	    Exp     valueT   = null; 
	    // Examine all definitions of "t" which reach this statement.  
	    // If they are all constant, and define "t" to the same value,
	    // we can replace "t". 
	    for (Iterator i=tDefs.iterator(); i.hasNext() && replaceT; ) { 
		Stm sNext = (Stm)i.next(); // The next definition of "t". 
		Exp src   = getSrc(sNext); // The value assigned to "t".    
		if (src != null) { 
		    // Don't yet know the value of "t". 
		    if (valueT == null) { valueT = src; } 
		    // Check the "src" is consistent with other definitions
		    // of "t", and that it represents a constant value. 
		    replaceT = isEqualConst(valueT, src); 
		}
		// Could not determine the value assigned to "t". 
		// Can't perform constant propagation. 
		else { replaceT = false; } 
	    }
	    // Our analysis says that it's OK to replace "t" with a constant
	    // value.  Use the tree structure to accomplish this. 
	    if (replaceT) { 
		t.replace((Tree)valueT.clone());
	    }
	}

	public void visit(Tree t) { throw new Error("No defaults here."); }
	
	//************************************************************//
	//                                                            //
	//                      Utility Methods                       //
	//                                                            //
	//************************************************************// 
	
	private Exp getSrc(Stm def) { 
	    if (def.kind() == TreeKind.MOVE) { 
		return ((MOVE)def).getSrc(); 
	    }
	    else { return null; }
	}

	// Returns true if "e1" and "e2" are constant, 
	// and evaluate to the same value. 
	private boolean isEqualConst(Exp e1, Exp e2) { 
	    if (e1.kind() != e2.kind()) { 
		return false; 
	    }
	    else if (e1.kind() == TreeKind.CONST) { 
		CONST c1 = (CONST)e1; 
		CONST c2 = (CONST)e2; 
		return 
		    c1.type == c2.type &&
		    c1.value == c2.value; 
	    }
	    else if (e1.kind() == TreeKind.NAME) { 
		NAME n1 = (NAME)e1;
		NAME n2 = (NAME)e2;
		return n1.label.equals(n2.label); 
	    }
	    else { 
		return false; 
	    }
	}

	// Maps each temp to the Stms that define it. 
	private void mapTempsToDefs(Code code) { 
	    UseDefer ud = code.getUseDefer();
	    for (Iterator i = code.getElementsI(); i.hasNext(); ) { 
		Tree tNext = (Tree)i.next(); 
		if (tNext instanceof Stm && !(tNext instanceof SEQ)) { 
		    Stm sNext = (Stm)tNext; 
		    Temp[] defs = ud.def(sNext); 
		    for (int n=0; n<defs.length; n++) { 
			if (!this.tempsToDefs.containsKey(defs[n]))
			    this.tempsToDefs.put(defs[n], new HashSet()); 
			Set hceDefs = (Set)this.tempsToDefs.get(defs[n]); 
			hceDefs.add(sNext); 
		    }
		}
	    }
	}

	private static Stm RS(Stm seq) { 
	    while (seq.kind()==TreeKind.SEQ) seq = ((SEQ)seq).getLeft();  
	    return seq;
	}

	private static Stm STM(Exp e) { 
	    Tree t = e; 
	    while (t instanceof Exp) { t = t.getParent(); }
	    return (Stm)t;
	}
    }
}



