package harpoon.Analysis.Tree;

import harpoon.IR.Tree.CanonicalTreeCode;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Util.HashSet;
import harpoon.Util.Util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Stack;

/**
 * The <code>TreeCFG</code> class is used to specify the control flow
 * in an <code>CanonicalTreeCode</code>.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: TreeCFG.java,v 1.1.2.2 1999-04-23 06:20:21 pnkfelix Exp $
 */
public class TreeCFG { 
    private Hashtable cfg  = new Hashtable();
    private Stm       root;

    /** Class constructor. */
    public TreeCFG(CanonicalTreeCode code) { 
	this.root = (Stm)code.getRootElement();
	buildCFG(code);
	compactCFG();
    }

    public Enumeration depthFirstEnumeration() { 
	return new Enumeration() { 
	    HashSet h = new HashSet();  // visited nodes
	    Stack stack = new Stack(); 
	    { 
		if (root instanceof SEQ) 
		    visitElement(((SEQ)root).left);
		else
		    visitElement(root);
	    }
	    public boolean hasMoreElements() { 
		if (stack.isEmpty()) { h.clear(); return false; }
		else { return true; }
	    }
	
	    private void visitElement(Object elem) {
		if (!h.contains(elem)) {
		    stack.push(elem);
		    h.union(elem);
		}
	    }
	    public Object nextElement() {
		if (stack.isEmpty()) throw new NoSuchElementException();
		else {
		    Stm s = (Stm)stack.pop();
		    Stm[] successors = getSuccessors(s);
		    for (int i=0;i<successors.length; i++) {
			visitElement(successors[i]);
		    }
		    return s;

		}
	    }
	};
    }

    /** Returns all <code>Stm</code> objects to which control could 
     *  legally pass from the specified parameter.  */
    public Stm[] getSuccessors(Stm s) { 
	return (s instanceof SEQ)?new Stm[]{((SEQ)s).left}:(Stm[])cfg.get(s);
    }

    private void buildCFG(CanonicalTreeCode code) { 
	CFGVisitor cv = new CFGVisitor(code, cfg);
        while (cv.nextNode!=null)
	    cv.nextNode.visit(cv);
    }
  
    private void compactCFG() { 
	for (Enumeration e = cfg.keys(); e.hasMoreElements();) { 
	    Stm s = (Stm)e.nextElement();
	    Stm[] successors = getSuccessors(s);
	    for (int i=0; i<successors.length; i++) {
		while(successors[i] instanceof SEQ) 
		    successors[i] = ((SEQ)successors[i]).left;
	    }
	}
	for (Enumeration e = ((Hashtable)cfg.clone()).keys(); 
	     e.hasMoreElements();) { 
	    Object s = e.nextElement();
	    if (s instanceof SEQ)
		cfg.remove(s);
	}
    }

    private static final Object END = new Object();

    // Builds the control flow graph
    class CFGVisitor extends TreeVisitor { 

	
	private CanonicalTreeCode code;
	private Hashtable         cfg;
	private Hashtable         labels = new Hashtable();
	private Stack             nodes  = new Stack();

	Stm nextNode;

	CFGVisitor(CanonicalTreeCode code, Hashtable cfg) {
	    this.code     = code;
	    this.cfg      = cfg;
	    this.nodes    = new Stack();
	    this.nextNode = (Stm)code.getRootElement();
	    
	    mapLabels(labels, code);
	}
	
	public void visit(Tree t) { 
	    throw new Error("No defaults allowed");
	}

	public void visit(Exp e) { /* Do nothing for Exps */ }

	public void visit(CJUMP s) { 
	    Util.assert(!cfg.containsKey(s));
	    Util.assert(labels.containsKey(s.iftrue));
	    Util.assert(labels.containsKey(s.iffalse));

	    Stm[] successors;
	    HashSet set = new HashSet();
	    
	    Object iftrue  = labels.get(s.iftrue);
	    Object iffalse = labels.get(s.iffalse); 
	    if (iftrue!=END)  set.union(iftrue);
	    if (iffalse!=END) set.union(iffalse);
	    successors = new Stm[set.size()];
	    set.copyInto(successors);

	    cfg.put(s, successors);

	    nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	}

	public void visit(JUMP s) { 
	    Util.assert(!cfg.containsKey(s));
	    
	    Stm[]   successors;
	    HashSet set = new HashSet(); // Used to compute successors

	    for (LabelList l = s.targets; l!=null; l=l.tail) { 
		Util.assert(labels.containsKey(l.head));
		Object target = labels.get(l.head);
		if (target!=END) set.union(target);
	    }
	    successors = new Stm[set.size()];
	    set.copyInto(successors);
	    cfg.put(s, successors);
	    
	    nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	}
	
	public void visit(SEQ s) { 
	    Util.assert(!cfg.containsKey(s));
	    Util.assert(s.left!=null && s.right!=null);
	    
	    Stm[] successors = new Stm[] { s.left };
	    cfg.put(s, successors);
	    nodes.push(s.right);

	    nextNode = s.left;
	}

	public void visit(Stm s) { 
	    Util.assert(!cfg.containsKey(s));

	    nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();

	    if (nextNode!=null) 
		cfg.put(s, new Stm[] { nextNode });
	    else
		cfg.put(s, new Stm[] { });
	}

	private void mapLabels(Hashtable table, 
			       CanonicalTreeCode code) {
	    for (Enumeration e = code.getElementsE(); e.hasMoreElements();) {
		Object next = e.nextElement();
		if (next instanceof SEQ) { 
		    SEQ seq = (SEQ)next;
		    if (seq.left instanceof LABEL) {
			Label l = ((LABEL)seq.left).label;
			table.put(l, seq.right);
		    }	
		    // dangerous, assumes that only the last label in
		    // the tree is on the right-hand side of a SEQ
		    if (seq.right instanceof LABEL) {
			Label l = ((LABEL)seq.right).label;
			table.put(l, END);
		    }
		}
	    }
	}
    }
}







