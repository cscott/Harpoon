// Code.java, created Fri Aug  7 13:45:29 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.QuadSSA.DeadCode;
import harpoon.ClassFile.*;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.Util.ArrayFactory;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;
/**
 * <code>Quads.QuadSSA</code> is a code view that exposes the details of
 * the java classfile bytecodes in a quadruple format.  Implementation
 * details of the stack-based JVM are hidden in favor of a flat consistent
 * temporary-variable based approach.  The generated quadruples adhere
 * to an SSA form; that is, every variable has exactly one definition,
 * and <code>PHI</code> functions are used where control flow merges.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadSSA.java,v 1.1.2.1 1998-12-01 12:36:43 cananian Exp $
 */

public class QuadSSA extends HCode {
    /** The name of this code view. */
    public static final String codename = "quad-ssa";

    /** The method that this code view represents. */
    HMethod parent;
    /** The quadruples composing this code view. */
    Quad quads;

    /** Creates a <code>Code</code> object from a bytecode object. */
    QuadSSA(harpoon.IR.Bytecode.Code bytecode) 
    {
	this.parent = bytecode.getMethod();
	this.quads = Translate.trans(bytecode);
	CleanUp.cleanup(this); // cleanup null predecessors of phis.
	Peephole.optimize(this); // peephole optimizations.
	FixupFunc.fixup(this); // add phi/sigma functions.
	DeadCode.optimize(this); // get rid of unused phi/sigmas.
    }
    /** 
     * Create a new code object given a quadruple representation
     * of the method instructions.  If <code>addPhi</code> is true,
     * adds phi and sigma functions to the <code>PHI</code> and
     * <code>SIGMA</code> quads in the representations.
     */
    public QuadSSA(HMethod parent, Quad quads, boolean addPhi) {
	this.parent = parent;
	this.quads = quads;
	Util.assert(quads instanceof HEADER);
	Util.assert(((HEADER)quads).footer instanceof FOOTER);
	// if addPhi, check that phis and sigmas are empty?
	if (addPhi)
	    FixupFunc.fixup(this);
	DeadCode.optimize(this);
    }
    /** Same as above; with <code>addPhi==false</code>. */
    public QuadSSA(HMethod parent, Quad quads) {
	this(parent, quads, false);
    }

    /** Clone this code representation. The clone has its own
     *  copy of the quad graph. */
    public HCode clone(HMethod newMethod) {
	return new QuadSSA(newMethod, Quad.clone(quads));
    }

    /**
     * Return the <code>HMethod</code> this codeview
     * belongs to.
     */
    public HMethod getMethod() { return parent; }

    /**
     * Return the name of this code view.
     * @return the string <code>"quad-ssa"</code>.
     */
    public String getName() { return codename; }
    
    public static void register() {
	HCodeFactory f = new HCodeFactory() {
	    public HCode convert(HMethod m) {
		HCode c = m.getCode("bytecode");
		return (c==null)?null:new QuadSSA((harpoon.IR.Bytecode.Code)c);
	    }
	    public String getCodeName() {
		return codename;
	    }
	};
	HMethod.register(f);
    }

    /** Returns the root of the control flow graph. */
    public HCodeElement getRootElement() { return quads; }
    /** Returns the leaves of the control flow graph. */
    public HCodeElement[] getLeafElements() {
	HEADER h = (HEADER) getRootElement();
	return new Quad[] { h.footer };
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
}
