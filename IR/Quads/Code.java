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
import harpoon.Util.Collections.Graph;
import harpoon.Util.Collections.UnmodifiableIterator;
import harpoon.Util.Util;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
 * @version $Id: Code.java,v 1.13 2003-05-09 21:06:19 cananian Exp $
 */
public abstract class Code extends HCode<Quad>
    implements java.io.Serializable, Graph<Quad,Edge> {
    /** The method that this code view represents. */
    protected final HMethod parent;
    /** The quadruples composing this code view. */
    protected Quad quads;
    /** Quad factory. */
    protected final QuadFactory qf;
    /** <code>AllocationInformation</code> for this <code>HCode</code>. */
    protected AllocationInformation ai = null;
    /** Keep track of modifications to this <code>Code</code> so that the
     *  <code>getElementsI()</code> <code>Iterator</code> can fail-fast. */
    int modCount=0;

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
    public abstract HCodeAndMaps<Quad> clone(HMethod newMethod);
    /** Helper for clone */
    protected final HCodeAndMaps<Quad> cloneHelper(final Code qc) {
	return cloneHelper(this, qc);
    }
    /** Helper for clone */
    protected HCodeAndMaps<Quad> cloneHelper(Code _this, Code qc) {
	HCodeAndMaps<Quad> hcam = Quad.cloneWithMaps(qc.qf, _this.quads);
	// fill in the missing info in the hcam.
	hcam = new HCodeAndMaps<Quad>(qc,
				hcam.elementMap(), hcam.tempMap(),
				_this,
				hcam.ancestorElementMap(),
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
    public Derivation<Quad> getDerivation() { return null; }

    /** Returns the root of the control flow graph. */
    public HEADER getRootElement() { return (HEADER) quads; }
    /** Returns the leaves of the control flow graph. */
    public Quad[] getLeafElements() {
	HEADER h = (HEADER) getRootElement();
	return new Quad[] { h.footer() };
    }

    /**
     * Returns an ordered list of the <code>Quad</code>s
     * making up this code view.  The root of the graph
     * is in element 0 of the array.
     * @deprecated
     */
    public Quad[] getElements() { return super.getElements(); }

    /** Returns an iterator over the <code>Quad</code>s making up
     *  this code view.  The root of the graph is the first element
     *  in the iteration. */
    public Iterator<Quad> getElementsI() {
	return new UnmodifiableIterator<Quad>() {
	    // record # of modifications to enable fail-fast.
	    int modCount = Code.this.modCount;
	    // set up visited set and to-do stack.
	    Set<Quad> visited = new HashSet<Quad>();
	    Stack<Quad> s = new Stack<Quad>();
	    { // initialize stack/set.
		s.push(getLeafElements()[0]); visited.add(s.peek());
		s.push(getRootElement());     visited.add(s.peek());
	    } 
	    public boolean hasNext() { return !s.isEmpty(); }
	    public Quad next() {
		if (modCount != Code.this.modCount)
		    throw new ConcurrentModificationException();
		if (s.empty()) throw new NoSuchElementException();
		Quad q = s.pop();
		boolean forwards = false;
		// prettiness hack! try to order elements in original source
		// order. =)
		if (q.nextLength()==2 &&
		    q.next(1).getLineNumber() < q.next(0).getLineNumber())
		    forwards = true;
		// push successors on stack before returning.
		for (int i= forwards ? 0 : (q.nextLength()-1);
		     forwards ? (i<q.nextLength()) : (i>=0);
		     i = forwards ? (i+1) : (i-1)) {
		    assert q.nextEdge(i)!=null : q;
		    if (!visited.contains(q.next(i))) {
			s.push(q.next(i));
			visited.add(q.next(i));
		    }
		}
		// let's validate q quickly here.
		for (int i=q.prevLength()-1; i>=0; i--)
		    assert q.prevEdge(i)!=null : q;
		// okay.
		return q;
	    }
	};
    }
    // implement elementArrayFactory which returns Quad[]s.
    public ArrayFactory<Quad> elementArrayFactory() {
	return Quad.arrayFactory;
    }
    // Graph interface
    public Set<Quad> nodes() {
	final List<Quad> l = getElementsL();
	return new AbstractSet<Quad>() {
	    public Iterator<Quad> iterator() { return l.iterator(); }
	    public int size() { return l.size(); }
	};
    }

    // print this Code.
    public void print(java.io.PrintWriter pw, PrintCallback<Quad> callback) {
	Print.print(pw, this, callback);
    }

    /** Returns the list of all quads <code>q</code> from
	<code>this</code> code for which <code>q.accept(v)</code> is
	<code>true</code>. */
    public List<Quad> selectQuads(QuadValueVisitor<Boolean> v) {
	final List<Quad> l = new ArrayList<Quad>();

	for(Iterator<Quad> it = getElementsI(); it.hasNext(); ) {
	    Quad q = it.next();
	    if (q.accept(v).booleanValue()) l.add(q);
	}

	return l;
    }


    /** Returns the list of all <code>CALL</code> quads from
	<code>this</code> code. */
    public List<Quad> selectCALLs() {
	return selectQuads(call_visitor);
    }
    private static QuadValueVisitor<Boolean> call_visitor = 
	new QuadValueVisitor<Boolean>() {
	    public Boolean visit(CALL q) {
		return Boolean.TRUE;
	    }
	    public Boolean visit(Quad q) {
		return Boolean.FALSE;
	    }
	};


    /** Returns the list of all allocation sites (ie, <code>NEW</code>
	and <code>ANEW</code> quads) from <code>this</code> code. */
    public List<Quad> selectAllocations() {
	return selectQuads(allocation_visitor);
    }
    private static QuadValueVisitor<Boolean> allocation_visitor = 
	new QuadValueVisitor<Boolean>() {
	    public Boolean visit(NEW q) {
		return Boolean.TRUE;
	    }
	    public Boolean visit(ANEW q) {
		return Boolean.TRUE;
	    }
	    public Boolean visit(Quad q) {
		return Boolean.FALSE;
	    }
	};

    /** Subclasses of <code>Code</code> that want to be notified when
        some optimization replace one of their quads with some other
        quad can override this method.  This can be useful for
        updating mappings that attach information to quads (i.e.,
        allocation policies, QuadNoSSA to QuadSSI mappings etc.).  See
        the code of DeadCode.replace for an example.

	@param oldquad quad that is being replaced 
	@param newquad new quad that replaces <code>oldquad</code>
	@param tm maps old temps to new temps */
    public void notifyReplace(Quad oldquad, Quad newquad, TempMap tm) { }
}
