package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Properties.Derivation;
import harpoon.Util.ArrayFactory;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

public abstract class Code extends HCode 
  implements Derivation, TypeMap
{
  /** The Tree Objects composing this code view. */
  protected Tree tree;
  /** The Frame containing machine-specific information*/
  protected final Frame frame;
  /** Tree factory. */
  protected final TreeFactory tf;
  /** The method that this code view represents. */
  protected final HMethod parent;

  /** Create a proper TreeFactory. */
  protected TreeFactory newTF(final HMethod parent)
    {
      final String scope = parent.getDeclaringClass().getName() + "." +
	parent.getName() + parent.getDescriptor() + "/" + getName();
      return new TreeFactory() {
	private final TempFactory tFact = Temp.tempFactory(scope);
	private int id=0;
	// Everyone uses same TempFactory now.
	public TempFactory tempFactory() { return frame.tempFactory(); }
	public Frame getFrame()  { return frame; }
	public HCode getParent() { return Code.this; }
	public synchronized int getUniqueID() { return id++; }
	public String toString() { 
	  return "TreeFactory["+getParent().toString()+"]"; 
	}
      };
    }

  /** constructor. */
  protected Code(final HMethod parent, final Tree tree, final Frame frame)
    {
      this.parent = parent;
      this.tree   = tree;
      this.frame  = frame;
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

  /** Returns the root of the Tree */
  public HCodeElement getRootElement() { return this.tree; }

  /** Returns the leaves of the Tree */
  public HCodeElement[] getLeafElements() 
    { 
      HCodeElement[] result;
      Stack          nodes    = new Stack();
      Vector         leaves   = new Vector();

      nodes.push(getRootElement());
      while (!nodes.isEmpty())
	{
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
  public HCodeElement[] getElements() 
    {
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
  public Enumeration getElementsE() 
    { 
      return new Enumeration() {
	Stack stack = new Stack();
	{ stack.push(getRootElement()); }
	public boolean hasMoreElements() { return !stack.isEmpty(); }
	public Object nextElement() {
	  Tree t;
	  if (stack.isEmpty()) throw new NoSuchElementException();
	  else {
	    t = (Tree)stack.pop();
	    // Push successors on stack
	    if (t instanceof SEQ) {
	      SEQ seq = (SEQ)t;
	      if (seq.left!=null)  stack.push(seq.left);
	      if (seq.right!=null) stack.push(seq.right);
	    }
	    else if (t instanceof ESEQ) {
	      ESEQ eseq = (ESEQ)t;
	      if (eseq.exp!=null) stack.push(eseq.exp);
	      if (eseq.stm!=null) stack.push(eseq.stm);
	    }
	    else {
	      ExpList explist = t.kids();
	      while (explist!=null) {
		if (explist.head!=null) stack.push(explist.head);
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

  public void print(java.io.PrintStream ps) 
    {
      Print printer = new Print(ps);
      printer.prStm((Stm)getRootElement());
    }

  public abstract DList derivation(HCodeElement hce, Temp t);
  public abstract HClass typeMap(HCode hc, Temp t);
}
