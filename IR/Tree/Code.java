// Code.java, created Mon Feb  8 16:55:15 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.Util.ArrayFactory;
import harpoon.Util.Util;
import harpoon.Temp.LabelList;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

/**
 * <code>Tree.Code</code> is an abstract superclass of codeviews
 * using the components in <code>IR.Tree</code>.  It implements
 * shared methods for the various codeviews using <code>Tree</code>s.
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: Code.java,v 1.1.2.29 1999-09-06 18:45:11 duncan Exp $
 */
public abstract class Code extends HCode 
    implements Derivation, TypeMap {
    /** The Tree Objects composing this code view. */
    protected Tree tree;

    /** The Frame containing machine-specific information*/
    protected /* final */ Frame frame;

    /** Tree factory. */
    protected /* final */ TreeFactory tf;

    /** The method that this code view represents. */
    protected /* final */ HMethod parent;

    /** Create a proper TreeFactory. */
    protected TreeFactory newTF(final HMethod parent) { 
	return new TreeFactory() {
	    private int id=0;
	    // Everyone uses same TempFactory now.
	    public TempFactory tempFactory() { return frame.tempFactory(); }
	    public HCode getParent() { return Code.this; }
	    public Frame getFrame() { return Code.this.frame; } 
	    public synchronized int getUniqueID() { return id++; }
	    public String toString() { 
		return "TreeFactory["+getParent().toString()+"]"; 
	    }
	};
    }

    /** constructor. */
    protected Code(final HMethod parent, final Tree tree, 
		   final Frame topframe) {
	final String scope = parent.getDeclaringClass().getName() + "." +
	    parent.getName() + parent.getDescriptor() + "/" + getName();
	this.parent = parent;
	this.tree   = tree;
	this.frame  = topframe.newFrame(scope);
	this.tf     = newTF(parent);
    }
  
    /** Clone this code representation. The clone has its own copy
     *  of the Tree */
    public abstract HCode  clone(HMethod newMethod, Frame frame);
    
    /** Return the name of this code view. */
    public abstract String getName();
    
    /** Return the <code>HMethod</code> this codeview
     *  belongs to.  */
    public HMethod getMethod() { return this.parent; }

    public Frame getFrame() { return this.frame; }

    /** Returns the root of the Tree */
    public HCodeElement getRootElement() { 
	// Ensures that the root is a SEQ, and the first instruction is 
	// a SEGMENT.
	Tree first = (SEQ)this.tree;
	while(first.kind()==TreeKind.SEQ) first = ((SEQ)first).left; 
	Util.assert(first.kind()==TreeKind.SEGMENT); 
	return this.tree; 
    }

    /** Returns the leaves of the Tree */
    public HCodeElement[] getLeafElements() { 
	Stack nodes  = new Stack();
	List  leaves = new ArrayList();

	nodes.push(getRootElement());
	while (!nodes.isEmpty()) {
	    Tree t = (Tree)nodes.pop();
	    if (t.kind()==TreeKind.SEQ) {
		SEQ seq = (SEQ)t;
		if (seq.left==null) {
		    if (seq.right==null) { leaves.add(seq); }
		    else                 { nodes.push(seq.right);  }
		}
		else {
		    nodes.push(seq.left);
		    if (seq.right!=null) nodes.push(seq.right);
		}
	    }
	    else if (t.kind()==TreeKind.ESEQ) {
		ESEQ eseq = (ESEQ)t;
		if (eseq.exp==null) {
		    if (eseq.stm==null) { leaves.add(eseq); }
		    else                { nodes.push(eseq.stm);    }
		}
		else {
		    nodes.push(eseq.exp);
		    if (eseq.stm!=null) nodes.push(eseq.stm);
		}
	    }
	    else {
		ExpList explist = t.kids();
		if (explist==null) leaves.add(t);
		else {
		    while (explist!=null) {
			if (explist.head!=null) nodes.push(explist.head);
			explist = explist.tail;
		    }
		}
	    }
	}
	return (HCodeElement[])leaves.toArray(new HCodeElement[0]);
    }
  
    /**
     * Returns an ordered list of the <code>Tree</code> Objects
     * making up this code view.  The root of the tree
     * is in element 0 of the array.
     */
    public HCodeElement[] getElements() {
	return super.getElements();
    }

    /** 
     * Returns an <code>Iterator</code> of the <code>Tree</code> Objects 
     * making up this code view.  The root of the tree is the first element
     * of the Iterator. 
     */
    public Iterator getElementsI() { 
	return new Iterator() {
	    Set h = new HashSet();
	    Stack stack = new Stack(); 
	    { visitElement(getRootElement()); }
	    public boolean hasNext() { 
		if (stack.isEmpty()) {
		    h = null; // free up some memory
		    return false;
		}
		else return true;
	    }
	    public Object next() {
		Tree t;
		if (stack.isEmpty()) throw new NoSuchElementException();
		else {
		    t = (Tree)stack.pop();
		    // Push successors on stack
		    switch (t.kind()) { 
		    case TreeKind.SEQ: 
			SEQ seq = (SEQ)t;
			if (seq.left!=null)  visitElement(seq.left);
			if (seq.right!=null) visitElement(seq.right);
			break;
		    case TreeKind.ESEQ: 
			ESEQ eseq = (ESEQ)t;
			if (eseq.exp!=null) visitElement(eseq.exp);
			if (eseq.stm!=null) visitElement(eseq.stm);
			break;
		    default:
			ExpList explist = t.kids();
			while (explist!=null) {
			    if (explist.head!=null) visitElement(explist.head);
			    explist = explist.tail;
			}
		    }
		}
		return t;
	    }
	    public void remove() { 
		throw new UnsupportedOperationException();
	    }
	    private void visitElement(Object elem) {
		if (h.add(elem)) stack.push(elem);
	    }
	};
    }
  
    // implement elementArrayFactory which returns Tree[]s.  
    public ArrayFactory elementArrayFactory() { return Tree.arrayFactory; }

    public void print(java.io.PrintWriter pw) {
	Print.print(pw,this);
    } 

    /** 
     * Returns true if this codeview is a canonical representation
     */
    public abstract boolean isCanonical();

    /** 
     * Recomputes the control-flow graph exposed through this codeview
     * by the <code>HasEdges</code> interface of its elements.  
     * This method should be called whenever the tree structure of this
     * codeview is modified.  This is an optional operation, which should
     * be implemented by all canonical codeviews.  
     *
     * @exception UnsupportedOperationException if this operation is not
     *      implemented. 
     *
     */
    public abstract void recomputeEdges();


    /**
     * Implementation of the <code>Derivation</code> interface.
     */
    public abstract DList derivation(HCodeElement hce, Temp t);
    
    /**
     * Implementation of the <code>TypeMap</code> interface.
     */
    public abstract HClass typeMap(HCodeElement hc, Temp t);

    /*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*
     *                                                           *
     *                EDGE INITIALIZATION CODE                   *
     *                                                           *
     *++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/

    /** JDK1.1 work-around for odd restrictions on static members of inner
     *  classes.  This interface declares the state constants for the
     *  Visitor class below. [CSA] */
    private interface VisitorStates {
	static final int COMPUTE_EDGE_SETS = 0;
	static final int ALLOC_EDGE_ARRAYS = 1;
	static final int ASSIGN_EDGE_DATA  = 2;
    }

    /* Only for canonical views, a class to initialize
     *  the edges representing the CFG of this Tree form
     */
    class EdgeInitializer extends TreeVisitor implements VisitorStates { 
	private Map    labels        = new HashMap();
	private Map    successors    = new HashMap();
	private Map    predecessors  = new HashMap();
	private Stack  nodes         = new Stack();
	private Stm    nextNode;
	private int    state;

	EdgeInitializer() { 
	    Util.assert(isCanonical());
	    mapLabels(); 
	}
	
	void computeEdges() { 
	    for (state=0; state<3; state++) { 
		nextNode = (Stm)getRootElement();
		while (nextNode!=null) { 
		    nextNode.visit(this); 
		}
	    }
	}

	public void visit(Tree t) { throw new Error("No defaults here."); }
	public void visit(Exp e)  { /* Do nothing for Exps */ } 

	public void visit(CALL s) { 
	    switch (state) { 
	    case COMPUTE_EDGE_SETS:
		nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
		Util.assert(labels.containsKey(s.retex.label));
		Util.assert(nextNode!=null);
		Util.assert(RS(nextNode)!=(Stm)labels.get(s.retex.label));
		addEdge(s, RS(nextNode)); 
		addEdge(s, (Stm)labels.get(s.retex.label));
		break;
	    case ALLOC_EDGE_ARRAYS:
		visit((Stm)s); return;
	    case ASSIGN_EDGE_DATA:
		nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
		if (successors.containsKey(s)) { 
		    Stm ex   = (Stm)labels.get(s.retex.label);
		    Set succ = (Set)successors.get(s);
		    Util.assert(succ.size()==2);
		    for (Iterator i = succ.iterator(); i.hasNext();) { 
			Stm next     = (Stm)i.next();
			Set nextPred = (Set)predecessors.get(next);
			Tree.addEdge(s, next==ex?1:0, next, nextPred.size()-1);
			nextPred.remove(s);
		    }
		    successors.remove(s);
		}
		break;
	    default: throw new Error("Bad state: " + state);
	    }
	}

	public void visit(CJUMP s) { 
	    switch(state) { 
	    case COMPUTE_EDGE_SETS:
		Util.assert(labels.containsKey(s.iftrue));
		Util.assert(labels.containsKey(s.iffalse));
		addEdge(s, (Stm)labels.get(s.iftrue));
		addEdge(s, (Stm)labels.get(s.iffalse));
		break;
	    case ALLOC_EDGE_ARRAYS:
		if (s.prev==null) 
		    s.prev = predecessors.containsKey(s)?
			new Edge[((Set)predecessors.get(s)).size()]:
			    new Edge[0];
		break;
	    case ASSIGN_EDGE_DATA:
		Util.assert(successors.containsKey(s));
		Stm next; Set nextPred;
		
		if (s.iftrue.equals(s.iffalse)) { 
		    next = (Stm)labels.get(s.iftrue);
		    nextPred = (Set)predecessors.get(next);
		    Edge[] oldNextPrev = next.prev;
		    next.prev = new Edge[next.prev.length+1];
		    System.arraycopy(oldNextPrev, 0, 
				     next.prev, 1, oldNextPrev.length);
		    Tree.addEdge(s, 0, next, nextPred.size()-1);
		    Tree.addEdge(s, 1, next, nextPred.size()-2);
		    nextPred.remove(s);
		}
		else { 
		    // Add true branch
		    next         = (Stm)labels.get(s.iftrue);
		    nextPred     = (Set)predecessors.get(next);
		    Tree.addEdge(s, 0, next, nextPred.size()-1);
		    nextPred.remove(s);
		    
		    // Add false branch
		    next         = (Stm)labels.get(s.iffalse);
		    nextPred     = (Set)predecessors.get(next);
		    Tree.addEdge(s, 1, next, nextPred.size()-1);
		    nextPred.remove(s);
		}
		break;
	    default:
		throw new Error("Bad state");
	    }
	    
	    nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	}
	
	public void visit(JUMP s) { 
	    switch (state) { 
	    case COMPUTE_EDGE_SETS:
		for (LabelList l = s.targets; l!=null; l=l.tail) { 
		    Util.assert(labels.containsKey(l.head));
		    addEdge(s, (Stm)labels.get(l.head));
		}
		break;
	    case ALLOC_EDGE_ARRAYS:
		visit((Stm)s);
		return;
	    case ASSIGN_EDGE_DATA:
		visit((Stm)s);
		return;
	    default:
		throw new Error("Bad state");
	    }
	    nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	}
	
	public void visit(SEQ s) { 
	    // Same for all states
	    Util.assert(s.left!=null && s.right!=null);
	    nodes.push(s.right);
	    nextNode = s.left;
	}

	public void visit(RETURN s) { 
	    switch (state) {
	    case COMPUTE_EDGE_SETS: 
		break;
	    case ALLOC_EDGE_ARRAYS: 
	    case ASSIGN_EDGE_DATA:  
		visit((Stm)s); 
		return;
	    default:
		throw new Error("Bad state");
	    }
	    nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	}

	public void visit(THROW s) { 
	    switch (state) {
	    case COMPUTE_EDGE_SETS: 
		break;
	    case ALLOC_EDGE_ARRAYS: 
	    case ASSIGN_EDGE_DATA:  
		visit((Stm)s); 
		return;
	    default:
		throw new Error("Bad state");
	    }
	    nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	}

	public void visit(Stm s) { 
	    nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	    switch (state) 
		{ 
		case COMPUTE_EDGE_SETS:
		    if (nextNode!=null) addEdge(s, RS(nextNode)); 
		    break;
		case ALLOC_EDGE_ARRAYS:
		    if (s.prev==null) 
			s.prev = predecessors.containsKey(s)?
			    new Edge[((Set)predecessors.get(s)).size()]:
				new Edge[0];
		    break;
		case ASSIGN_EDGE_DATA:
		    int n=0;
		    if (successors.containsKey(s)) { 
			Set succ = (Set)successors.get(s);
			for (Iterator i = succ.iterator(); i.hasNext();) { 
			    Stm next     = (Stm)i.next();
			    Set nextPred = (Set)predecessors.get(next);
			    Tree.addEdge(s, n++, next, nextPred.size()-1);
			    nextPred.remove(s);
			}
			succ.clear(); 
		    }
		    break;
		default:
		    throw new Error("Bad state: " + state);
		}
	}

	private void mapLabels() {
	    for (Iterator i = getElementsI(); i.hasNext();) {
		Object next = i.next();
		try {  
		    SEQ seq = (SEQ)next;
		    if (seq.left.kind()==TreeKind.LABEL) 
			labels.put(((LABEL)seq.left).label, seq.left);
		    if (seq.right.kind()==TreeKind.LABEL) 
			labels.put(((LABEL)seq.right).label, seq.right);
		}
		catch (ClassCastException ex) { } 
	    }
	}
	
	private void addEdge(Stm from, Stm to) { 
	    Set pred, succ;
	    if (predecessors.containsKey(to))
		pred = (Set)predecessors.get(to);
	    else {
		pred = new HashSet();
		predecessors.put(to, pred);
	    }
	    if (successors.containsKey(from)) 
		succ = (Set)successors.get(from);
	    else { 
		succ = new HashSet();
		successors.put(from, succ);
	    }
	    pred.add(from);
	    succ.add(to);
	}	    

	private Stm RS(Stm seq) { 
	    while (seq.kind()==TreeKind.SEQ) seq = ((SEQ)seq).left;  
	    return seq;
	}
    }
}
