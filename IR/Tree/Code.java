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
import harpoon.Util.HashSet;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.Temp.LabelList;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

public abstract class Code extends HCode 
    implements Derivation, TypeMap {
    /** The Tree Objects composing this code view. */
    protected Tree tree;
    /** The Frame. */
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
    public HCodeElement getRootElement() { return this.tree; }

    /** Returns the leaves of the Tree */
    public HCodeElement[] getLeafElements() { 
	HCodeElement[] result;
	Stack          nodes    = new Stack();
	Vector         leaves   = new Vector();

	nodes.push(getRootElement());
	while (!nodes.isEmpty()) {
	    Tree t = (Tree)nodes.pop();
	    if (t instanceof SEQ) {
		SEQ seq = (SEQ)t;
		if (seq.left==null) {
		    if (seq.right==null) { leaves.addElement(seq); }
		    else                 { nodes.push(seq.right);  }
		}
		else {
		    nodes.push(seq.left);
		    if (seq.right!=null) nodes.push(seq.right);
		}
	    }
	    else if (t instanceof ESEQ) {
		ESEQ eseq = (ESEQ)t;
		if (eseq.exp==null) {
		    if (eseq.stm==null) { leaves.addElement(eseq); }
		    else                { nodes.push(eseq.stm);    }
		}
		else {
		    nodes.push(eseq.exp);
		    if (eseq.stm!=null) nodes.push(eseq.stm);
		}
	    }
	    else {
		ExpList explist = t.kids();
		if (explist==null) leaves.addElement(t);
		else {
		    while (explist!=null) {
			if (explist.head!=null) nodes.push(explist.head);
			explist = explist.tail;
		    }
		}
	    }
	}
	result = new HCodeElement[leaves.size()];
	leaves.copyInto(result);
	return result;
    }
  
    /**
   *  Returns an ordered list of the <code>Tree</code> Objects
   *  making up this code view.  The root of the tree
   *  is in element 0 of the array.
   */
    public HCodeElement[] getElements() {
	Vector v = new Vector();
	for (Enumeration e = getElementsE(); e.hasMoreElements();)
	    v.addElement(e.nextElement());
	HCodeElement[] elements = new HCodeElement[v.size()];
	v.copyInto(elements);
	return (HCodeElement[])elements;
    }

    /** Returns an enumeration of the <code>Tree</code> Objects making up
   *  this code view.  The root of the tree is the first element
   *  enumerated. */
    public Enumeration getElementsE() { 
	return new Enumeration() {
	    HashSet h = new HashSet();
	    Stack stack = new Stack(); 
	    { 
		visitElement(getRootElement());
	    }
	    public boolean hasMoreElements() { 
		if (stack.isEmpty()) {
		    h.clear();
		    return false;
		}
		else return true;
	    }

	    private void visitElement(Object elem) {
		if (!h.contains(elem)) {
		    stack.push(elem);
		    h.union(elem);
		}
	    }
	    public Object nextElement() {
		Tree t;
		if (stack.isEmpty()) throw new NoSuchElementException();
		else {
		    t = (Tree)stack.pop();
		    // Push successors on stack
		    if (t instanceof SEQ) {
			SEQ seq = (SEQ)t;
			if (seq.left!=null)  visitElement(seq.left);
			if (seq.right!=null) visitElement(seq.right);
		    }
		    else if (t instanceof ESEQ) {
			ESEQ eseq = (ESEQ)t;
			if (eseq.exp!=null) visitElement(eseq.exp);
			if (eseq.stm!=null) visitElement(eseq.stm);
		    }
		    else {
			ExpList explist = t.kids();
			while (explist!=null) {
			    if (explist.head!=null) visitElement(explist.head);
			    explist = explist.tail;
			}
		    }
		}
		return t;
	    }
	};
    }
  
    // implement elementArrayFactory which returns Tree[]s.  
    public ArrayFactory elementArrayFactory() { return Tree.arrayFactory; }

    public void print(java.io.PrintWriter pw) {
	Print.print(pw,this);
    } 

    public boolean isCanonical() { 
	return getName().equals("canonical-tree");
    }

    public abstract DList derivation(HCodeElement hce, Temp t);

    public abstract HClass typeMap(HCode hc, Temp t);


    /** Only for CanoncialTreeCode and later views, a class to initialize
     *  the edges representing the CFG of this Tree form
     */
    class EdgeInitializer extends TreeVisitor { 
	private /* static */ final int COMPUTE_EDGE_SETS = 0;
	private /* static */ final int ALLOC_EDGE_ARRAYS = 1;
	private /* static */ final int ASSIGN_EDGE_DATA  = 2;

	private Hashtable labels       = new Hashtable();
	private Hashtable successors   = new Hashtable();
	private Hashtable predecessors = new Hashtable();
	private Stack     nodes        = new Stack();
	private Stm       nextNode;
	private int       state;

	EdgeInitializer() { 
	    Util.assert(isCanonical());
	    mapLabels(); 
	}
	
	void computeEdges() { 
	    for (state=0; state<3; state++) { 
		nextNode = (Stm)getRootElement();
		while (nextNode!=null) { nextNode.visit(this); }
	    }
	}

	public void visit(Tree t) { 
	    throw new Error("No defaults here.");
	}

	public void visit(Exp e) { /* Do nothing for Exps */ } 

	public void visit(CJUMP s) { 
	    switch(state) 
		{ 
		case COMPUTE_EDGE_SETS:
		    Util.assert(labels.containsKey(s.iftrue));
		    Util.assert(labels.containsKey(s.iffalse));
		    addEdge(s, (Stm)labels.get(s.iftrue));
		    addEdge(s, (Stm)labels.get(s.iffalse));
		    break;
		case ALLOC_EDGE_ARRAYS:
		    s.prev = predecessors.containsKey(s)?
			new Edge[((Set)predecessors.get(s)).size()]:
			    new Edge[0];
		    Util.assert(successors.containsKey(s));
		    s.next = new Edge[2];
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
	    switch (state) 
		{ 
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

	public void visit(Stm s) { 
	    nextNode = nodes.isEmpty()?null:(Stm)nodes.pop();
	    switch (state) 
		{ 
		case COMPUTE_EDGE_SETS:
		    if (nextNode!=null) addEdge(s, RS(nextNode)); 
		    break;
		case ALLOC_EDGE_ARRAYS:
		    s.prev = predecessors.containsKey(s)?
			new Edge[((Set)predecessors.get(s)).size()]:
			    new Edge[0];
		    s.next = successors.containsKey(s)?
			new Edge[((Set)successors.get(s)).size()]:
			    new Edge[0];
		    break;
		case ASSIGN_EDGE_DATA:
		    int i=0;
		    if (successors.containsKey(s)) { 
			Set succ = (Set)successors.get(s);
			for (Enumeration e = succ.elements(); 
			     e.hasMoreElements();) { 
			    Stm next     = (Stm)e.nextElement();
			    Set nextPred = (Set)predecessors.get(next);
			    Tree.addEdge(s, i++, next, nextPred.size()-1);
			    nextPred.remove(s);
			}
			succ.clear(); 
			i=0;
		    }
		    break;
		default:
		    throw new Error("Bad state: " + state);
		}
	}

	private void mapLabels() {
	    for (Enumeration e = getElementsE(); e.hasMoreElements();) {
		Object next = e.nextElement();
		if (next instanceof SEQ) { 
		    SEQ seq = (SEQ)next;
		    if (seq.left instanceof LABEL) 
			labels.put(((LABEL)seq.left).label, seq.left);
		    if (seq.right instanceof LABEL) 
			labels.put(((LABEL)seq.right).label, seq.right);
		}
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
	    pred.union(from);
	    succ.union(to);
	}	    

	private Stm RS(Stm seq) { 
	    while (seq instanceof SEQ) seq = ((SEQ)seq).left;
	    return seq;
	}
    }
}


















