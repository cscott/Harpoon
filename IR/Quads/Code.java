// Code.java, created Fri Aug  7 13:45:29 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.ArrayFactory;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

/**
 * <code>Quads.Code</code> is an abstract superclass of codeviews
 * using the components in <code>IR.Quads</code>.  It implements
 * shared methods for the various codeviews using <code>Quad</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.1.2.16 2000-05-17 03:54:07 cananian Exp $
 */
public abstract class Code extends HCode implements java.io.Serializable {
    /** The method that this code view represents. */
    protected final HMethod parent;
    /** The quadruples composing this code view. */
    protected Quad quads;
    /** Quad factory. */
    protected final QuadFactory qf;
    /** <code>AllocationInformation</code> for this <code>HCode</code>. */
    protected AllocationInformation ai = null;

    /** Create a proper QuadFactory. */
    protected QuadFactory newQF(final HMethod parent) {
	final String scope = parent.getDeclaringClass().getName() + "." +
	    parent.getName() + parent.getDescriptor() + "/" + getName();
	abstract class SerializableQuadFactory extends QuadFactory
	    implements java.io.Serializable { /* only declare inheritance */ }
	return new SerializableQuadFactory() {
	    private final TempFactory tf = Temp.tempFactory(scope);
	    private int id=0;
	    public TempFactory tempFactory() { return tf; }
	    public Code getParent() { return Code.this; }
	    public synchronized int getUniqueID() { return id++; }
	};
    }
    /** constructor. */
    protected Code(final HMethod parent, final Quad quads) {
	this.parent = parent; this.quads = quads;
	this.qf = newQF(parent);
    }
    
    /** Clone this code representation. The clone has its own
     *  copy of the quad graph. */
    public abstract HCode clone(HMethod newMethod);
    /**
     * Return the name of this code view.
     */
    public abstract String getName();
    
    /**
     * Return the <code>HMethod</code> this codeview
     * belongs to.
     */
    public HMethod getMethod() { return parent; }

    /**
     * Return the <code>AllocationInformation</code> for this codeview.
     */
    public AllocationInformation getAllocationInformation() { return ai; }
    /**
     * Set an <code>AllocationInformation</code> for this codeview.
     */
    public void setAllocationInformation(AllocationInformation ai) {
	this.ai = ai;
    }
    /**
     * Return a <code>Derivation</code> for this codeview.
     * @return <code>null</code>, always.
     */
    public Derivation getDerivation() { return null; }

    /** Returns the root of the control flow graph. */
    public HCodeElement getRootElement() { return quads; }
    /** Returns the leaves of the control flow graph. */
    public HCodeElement[] getLeafElements() {
	HEADER h = (HEADER) getRootElement();
	return new Quad[] { h.footer() };
    }

    /**
     * Returns an ordered list of the <code>Quad</code>s
     * making up this code view.  The root of the graph
     * is in element 0 of the array.
     */
    public HCodeElement[] getElements() { return super.getElements(); }

    /** Returns an iterator over the <code>Quad</code>s making up
     *  this code view.  The root of the graph is the first element
     *  in the iteration. */
    public Iterator getElementsI() {
	return new UnmodifiableIterator() {
	    Set visited = new HashSet();
	    Stack s = new Stack();
	    { // initialize stack/set.
		s.push(getLeafElements()[0]); visited.add(s.peek());
		s.push(getRootElement());     visited.add(s.peek());
	    } 
	    public boolean hasNext() { return !s.isEmpty(); }
	    public Object next() {
		if (s.empty()) throw new NoSuchElementException();
		Quad q = (Quad) s.pop();
		// push successors on stack before returning.
		for (int i=q.nextLength()-1; i>=0; i--)
		    if (!visited.contains(q.next(i))) {
			s.push(q.next(i));
			visited.add(q.next(i));
		    }
		// okay.
		return q;
	    }
	};
    }
    // implement elementArrayFactory which returns Quad[]s.
    public ArrayFactory elementArrayFactory() { return Quad.arrayFactory; }

    // print this Code.
    public void print(java.io.PrintWriter pw) {
	Print.print(pw, this);
    }
}
