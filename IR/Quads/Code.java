// Code.java, created Fri Aug  7 13:45:29 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.Derivation;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Temp.TempFactory;
import harpoon.Util.ArrayFactory;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

/**
 * <code>Quads.Code</code> is an abstract superclass of codeviews
 * using the components in <code>IR.Quads</code>.  It implements
 * shared methods for the various codeviews using <code>Quad</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Code.java,v 1.1.2.23 2001-01-24 19:33:54 cananian Exp $
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
    public abstract HCodeAndMaps clone(HMethod newMethod);
    /** Helper for clone */
    protected final HCodeAndMaps cloneHelper(final Code qc) {
	return cloneHelper(this, qc);
    }
    /** Helper for clone */
    protected HCodeAndMaps cloneHelper(Code _this, Code qc) {
	HCodeAndMaps hcam = Quad.cloneWithMaps(qc.qf, _this.quads);
	// fill in the missing info in the hcam.
	hcam = new HCodeAndMaps(qc, hcam.elementMap(), hcam.tempMap(),
				_this, hcam.ancestorElementMap(),
				hcam.ancestorTempMap());
	// finish setting up qc.
	qc.quads = (HEADER) hcam.elementMap().get(_this.quads);
	// clone allocation information.
	if (_this.getAllocationInformation()!=null)
	    qc.setAllocationInformation(cloneAllocationInformation(hcam));
	// derivation is cloned in LowQuad.cloneHelper()
	return hcam;
    }
    private static AllocationInformation cloneAllocationInformation
	(HCodeAndMaps hcam) {
	Code ocode = (Code) hcam.ancestorHCode();
	AllocationInformation oaim = ocode.getAllocationInformation();
	AllocationInformationMap naim = new AllocationInformationMap();
	for (Iterator it=ocode.getElementsI(); it.hasNext(); ) {
	    HCodeElement ohce = (HCodeElement) it.next();
	    HCodeElement nhce = (HCodeElement) hcam.elementMap().get(ohce);
	    if (ohce instanceof ANEW || ohce instanceof NEW)
		naim.transfer(nhce, ohce, hcam.tempMap(), oaim);
	}
	return naim;
    }
	
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
		for (int i=q.nextLength()-1; i>=0; i--) {
		    Util.assert(q.nextEdge(i)!=null, q);
		    if (!visited.contains(q.next(i))) {
			s.push(q.next(i));
			visited.add(q.next(i));
		    }
		}
		// let's validate q quickly here.
		for (int i=q.prevLength()-1; i>=0; i--)
		    Util.assert(q.prevEdge(i)!=null, q);
		// okay.
		return q;
	    }
	};
    }
    // implement elementArrayFactory which returns Quad[]s.
    public ArrayFactory elementArrayFactory() { return Quad.arrayFactory; }

    // print this Code.
    public void print(java.io.PrintWriter pw, PrintCallback callback) {
	Print.print(pw, this, callback);
    }
}
