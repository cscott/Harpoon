// Code.java, created Fri Aug  7 13:45:29 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.Util.ArrayFactory;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

/**
 * <code>Quads.Code</code> is an abstract superclass of codeviews
 * using the components in <code>IR.Quads</code>.  It implements
 * shared methods for the various codeviews using <code>Quad</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.1.2.3 1998-12-18 04:49:59 cananian Exp $
 */
abstract class Code extends HCode {
    /** The method that this code view represents. */
    HMethod parent;
    /** The quadruples composing this code view. */
    Quad quads;

    Code(HMethod parent, Quad quads) {
	this.parent = parent; this.quads = quads;
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
    public HCodeElement[] getElements() { 
	Vector v = new Vector();
	for (Enumeration e = getElementsE(); e.hasMoreElements(); )
	    v.addElement(e.nextElement());
	HCodeElement[] elements = new Quad[v.size()];
	v.copyInto(elements);
	return (HCodeElement[]) elements;
    }

    /** Returns an enumeration of the <code>Quad</code>s making up
     *  this code view.  The root of the graph is the first element
     *  enumerated. */
    public Enumeration getElementsE() {
	return new Enumeration() {
	    Set visited = new Set();
	    Stack s = new Stack();
	    { // initialize stack/set.
		s.push(getLeafElements()[0]); visited.union(s.peek());
		s.push(getRootElement());     visited.union(s.peek());
	    } 
	    public boolean hasMoreElements() { return !s.isEmpty(); }
	    public Object nextElement() {
		if (s.empty()) throw new NoSuchElementException();
		Quad q = (Quad) s.pop();
		// push successors on stack before returning.
		Quad[] next = q.next();
		for (int i=next.length-1; i>=0; i--)
		    if (!visited.contains(next[i])) {
			s.push(next[i]);
			visited.union(next[i]);
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
