// CFGrapher.java, created Mon Nov 29 23:32:45 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.Util.ArrayIterator;
import harpoon.Util.CombineIterator;
import harpoon.Util.FilterIterator;
import harpoon.Util.UnmodifiableIterator;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
/**
 * <code>CFGrapher</code> provides a means to externally associate
 * control-flow graph information with elements of an intermediate
 * representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CFGrapher.java,v 1.2 2002-02-25 21:04:44 cananian Exp $
 * @see harpoon.IR.Properties.CFGraphable
 */
public abstract class CFGrapher {
    /** Returns the first <code>HCodeElement</code>s to be executed; that is,
     *  the roots of the control-flow graph. */
    public HCodeElement[] getFirstElements(HCode hcode) {
	HCodeElement[] r =
	    (HCodeElement[]) hcode.elementArrayFactory().newArray(1);
	r[0] = getFirstElement(hcode);
	return r;
    }
    /** Returns the last <code>HCodeElement</code>s to be executed; that is,
     *  the leaves of the control-flow graph. */
    public abstract HCodeElement[] getLastElements(HCode hcode);
    /** This method is present for compatibility only.
     *  @deprecated Use getFirstElements() instead. */
    public abstract HCodeElement getFirstElement(HCode hcode);

    /** Returns an array of all the edges to and from the specified
     *  <code>HCodeElement</code>. */
    public HCodeEdge[] edges(HCodeElement hc) {
	Collection c = edgeC(hc);
	return (HCodeEdge[]) c.toArray(new HCodeEdge[c.size()]);
    }
    /** Returns an array of all the edges entering the specified
     *  <code>HCodeElement</code>. */
    public HCodeEdge[] pred(HCodeElement hc) {
	Collection c = predC(hc);
	return (HCodeEdge[]) c.toArray(new HCodeEdge[c.size()]);
    }
    /** Returns an array of all the edges leaving the specified
     *  <code>HCodeElement</code>. */
    public HCodeEdge[] succ(HCodeElement hc) {
	Collection c = succC(hc);
	return (HCodeEdge[]) c.toArray(new HCodeEdge[c.size()]);
    }

    // JDK 1.2 collections API: [CSA, 15-Jun-1999]
    /** Returns a <code>Collection</code> of all the edges to and from
     *  this <code>HCodeElement</code>. */
    public Collection edgeC(HCodeElement hc) {
	Collection p = predC(hc), s = succC(hc);
	HCodeEdge[] ea = new HCodeEdge[p.size()+s.size()];
	Iterator it=new CombineIterator(p.iterator(),s.iterator());
	for (int i=0; it.hasNext(); i++) ea[i] = (HCodeEdge) it.next();
	return Arrays.asList(ea);
    }
    /** Returns a <code>Collection</code> of all the edges to
	this <code>HCodeElement</code>. 
        Each <code>HCodeEdge</code> returned is guaranteed to return 
	<code>hc</code> in response to a call to <code>to()</code>;
	the actual predecessor will be returned from
	<code>from()</code>.  
     */
    public abstract Collection predC(HCodeElement hc);
    /** Returns a <code>Collection</code> of all the edges from
	this <code>HCodeElement</code>. 
        Each <code>HCodeEdge</code> returned is guaranteed to return
	<code>hc</code> in response to a call to
	<code>from()</code>; the actual successor to <code>this</code>
	will be returned from <code>to()</code>.
     */
    public abstract Collection succC(HCodeElement hc);
    
    /** Returns a <code>Collection</code> of all the 
	<code>HCodeElement</code>s preceeding <code>hc</code>.
    */
    public Collection predElemC(HCodeElement hc) {
	final Collection predEdges = this.predC(hc);
	return new AbstractCollection() {
		public int size() { return predEdges.size(); }
		public Iterator iterator() {
		    return new FilterIterator
			(predEdges.iterator(), 
			 new FilterIterator.Filter() { 
			    public Object map(Object o)
				{ return ((HCodeEdge)o).from(); }
			});
		}
	    };
    }
    
    /** Returns a <code>Collection</code> of all the 
	<code>HCodeElement</code> succeeding <code>hc</code>.
    */
    public Collection succElemC(HCodeElement hc) {
	final Collection succEdges = this.succC(hc);
	return new AbstractCollection() {
		public int size() { return succEdges.size(); }
		public Iterator iterator() {
		    return new FilterIterator
			(succEdges.iterator(), 
			 new FilterIterator.Filter() {
			    public Object map(Object o)
				{ return ((HCodeEdge)o).to(); }
			});
		}
	    };
    }

    public Set getElements(final HCode code) {
	return new AbstractSet() {
	    public int size() {
		int i=0;
		for (Iterator it=iterator(); it.hasNext(); it.next())
		    i++;
		return i;
	    }
	    public Iterator iterator() {
		return new UnmodifiableIterator() {
		    // implementation borrowed from IR/Quads/Code.
		    Set visited = new HashSet();
		    Stack s = new Stack();
		    { // initialize stack/set.
			Iterator it=new ArrayIterator(getFirstElements(code));
			HCodeElement[] leaves = getLastElements(code);
			if (leaves!=null)
			    it = new CombineIterator
				(new ArrayIterator(leaves), it);
			while (it.hasNext()) {
			    HCodeElement hce = (HCodeElement) it.next();
			    s.push(hce); visited.add(hce);
			}
		    }
		    public boolean hasNext() { return !s.isEmpty(); }
		    public Object next() {
			if (s.empty()) throw new NoSuchElementException();
			HCodeElement hce = (HCodeElement) s.pop();
			// push successors on stack before returning.
			Iterator it=succElemC(hce).iterator();
			while (it.hasNext()) {
			    HCodeElement nxt = (HCodeElement) it.next();
			    if (!visited.contains(nxt)) {
				s.push(nxt); visited.add(nxt);
			    }
			}
			return hce;
		    }
		};
	    }
	};
    }
    
    /** Returns an edge-reversed grapher based on this one.
     *  Certain algorithms operate more naturally on this
     *  representation --- for example, the difference between a
     *  dominator tree and a post-dominator tree is now simply
     *  whether you use the <code>grapher</code> or
     *  <code>grapher.edgeReversed()</code>. */
    public CFGrapher edgeReversed() {
	return new ReverseGrapher(this); // cache?
    }
    private static class ReverseGrapher extends SerializableGrapher {
	private final CFGrapher grapher;
	ReverseGrapher(CFGrapher grapher) { this.grapher = grapher; }
	public HCodeElement[] getFirstElements(HCode hcode) {
	    return grapher.getLastElements(hcode);
	}
	public HCodeElement getFirstElement(HCode hcode) {
	    HCodeElement[] r = getFirstElements(hcode);
	    if (r.length!=1)
		throw new Error("Deprecated use of getFirstElement() not "+
				"supported with edge-reversed graphers.");
	    return r[0];
	}
	public HCodeElement[] getLastElements(HCode hcode) {
	    return grapher.getFirstElements(hcode);
	}
	public Collection predC(HCodeElement hc) {
	    return reverseEdges(grapher.succC(hc));
	}
	public Collection succC(HCodeElement hc) {
	    return reverseEdges(grapher.predC(hc));
	}
	private static Collection reverseEdges(Collection edges) {
	    HCodeEdge[] ea = new HCodeEdge[edges.size()];
	    Iterator it=edges.iterator();
	    for (int i=0; it.hasNext(); i++) {
		final HCodeEdge e = (HCodeEdge) it.next();
		ea[i] = new HCodeEdge() {
		    public HCodeElement from() { return e.to(); }
		    public HCodeElement to() { return e.from(); }
		};
	    }
	    return Arrays.asList(ea);
	}
	public CFGrapher edgeReversed() { return grapher; }
    }

    /** Default <code>CFGrapher</code> for <code>HCodeElement</code>s
     *  which implement <code>CFGraphable</code>.  Does nothing
     *  but cast the supplied <code>HCodeElement</code> to a
     *  <code>CFGraphable</code> and invoke the appropriate
     *  corresponding method in the <code>CFGraphable</code>
     *  interface.  The root of the control flow graph is
     *  assumed to be whatever <code>HCode.getRootElement</code>
     *  returns.
     * @see java.util.Comparator
     * @see java.lang.Comparable
     * @see harpoon.Util.Default.comparator
     */
    public static final CFGrapher DEFAULT = new SerializableGrapher() {
	public HCodeElement getFirstElement(HCode hcode) {
	    return hcode.getRootElement();
	}
	public HCodeElement[] getLastElements(HCode hcode) {
	    return hcode.getLeafElements();
	}
	public HCodeEdge[] edges(HCodeElement hc) {
	    return ((CFGraphable)hc).edges();
	}
	public HCodeEdge[] pred(HCodeElement hc) {
	    return ((CFGraphable)hc).pred();
	}
	public HCodeEdge[] succ(HCodeElement hc) {
	    return ((CFGraphable)hc).succ();
	}
	public Collection edgeC(HCodeElement hc) {
	    return ((CFGraphable)hc).edgeC();
	}
	public Collection predC(HCodeElement hc) {
	    return ((CFGraphable)hc).predC();
	}
	public Collection succC(HCodeElement hc) {
	    return ((CFGraphable)hc).succC();
	}
    };
    private static abstract class SerializableGrapher extends CFGrapher
	implements java.io.Serializable { }
}
