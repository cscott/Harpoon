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
 * @version $Id: CFGrapher.java,v 1.2.2.2 2002-03-15 16:28:17 cananian Exp $
 * @see harpoon.IR.Properties.CFGraphable
 */
public abstract class CFGrapher<HCE extends HCodeElement> {
    /** Returns the first <code>HCodeElement</code>s to be executed; that is,
     *  the roots of the control-flow graph. */
    public HCE[] getFirstElements(HCode<HCE> hcode) {
	HCE[] r = hcode.elementArrayFactory().newArray(1);
	r[0] = getFirstElement(hcode);
	return r;
    }
    /** Returns the last <code>HCodeElement</code>s to be executed; that is,
     *  the leaves of the control-flow graph. */
    public abstract HCE[] getLastElements(HCode<HCE> hcode);
    /** This method is present for compatibility only.
     *  @deprecated Use getFirstElements() instead. */
    public abstract HCE getFirstElement(HCode<HCE> hcode);

    /** Returns an array of all the edges to and from the specified
     *  <code>HCodeElement</code>. */
    public HCodeEdge<HCE>[] edges(HCE hc) {
	Collection<HCodeEdge<HCE>> c = edgeC(hc);
	return c.toArray(new HCodeEdge<HCE>[c.size()]);
    }
    /** Returns an array of all the edges entering the specified
     *  <code>HCodeElement</code>. */
    public HCodeEdge<HCE>[] pred(HCE hc) {
	Collection<HCodeEdge<HCE>> c = predC(hc);
	return c.toArray(new HCodeEdge<HCE>[c.size()]);
    }
    /** Returns an array of all the edges leaving the specified
     *  <code>HCodeElement</code>. */
    public HCodeEdge<HCE>[] succ(HCE hc) {
	Collection<HCodeEdge<HCE>> c = succC(hc);
	return c.toArray(new HCodeEdge<HCE>[c.size()]);
    }

    // JDK 1.2 collections API: [CSA, 15-Jun-1999]
    /** Returns a <code>Collection</code> of all the edges to and from
     *  this <code>HCodeElement</code>. */
    public Collection<HCodeEdge<HCE>> edgeC(HCE hc) {
	Collection<HCodeEdge<HCE>> p = predC(hc), s = succC(hc);
	HCodeEdge<HCE>[] ea = new HCodeEdge<HCE>[p.size()+s.size()];
	Iterator<HCodeEdge<HCE>> it
	    = new CombineIterator<HCodeEdge<HCE>>(p.iterator(),s.iterator());
	for (int i=0; it.hasNext(); i++) ea[i] = it.next();
	return Arrays.asList(ea);
    }
    /** Returns a <code>Collection</code> of all the edges to
	this <code>HCodeElement</code>. 
        Each <code>HCodeEdge</code> returned is guaranteed to return 
	<code>hc</code> in response to a call to <code>to()</code>;
	the actual predecessor will be returned from
	<code>from()</code>.  
     */
    public abstract Collection<HCodeEdge<HCE>> predC(HCE hc);
    /** Returns a <code>Collection</code> of all the edges from
	this <code>HCodeElement</code>. 
        Each <code>HCodeEdge</code> returned is guaranteed to return
	<code>hc</code> in response to a call to
	<code>from()</code>; the actual successor to <code>this</code>
	will be returned from <code>to()</code>.
     */
    public abstract Collection<HCodeEdge<HCE>> succC(HCE hc);
    
    /** Returns a <code>Collection</code> of all the 
	<code>HCodeElement</code>s preceeding <code>hc</code>.
    */
    public Collection<HCE> predElemC(HCE hc) {
	final Collection<HCodeEdge<HCE>> predEdges = this.predC(hc);
	return new AbstractCollection<HCE>() {
		public int size() { return predEdges.size(); }
		public Iterator<HCE> iterator() {
		    return new FilterIterator<HCodeEdge<HCE>,HCE>
			(predEdges.iterator(), 
			 new FilterIterator.Filter<HCodeEdge<HCE>,HCE>() { 
			    public HCE map(HCodeEdge<HCE> o)
				{ return o.from(); }
			});
		}
	    };
    }
    
    /** Returns a <code>Collection</code> of all the 
	<code>HCodeElement</code> succeeding <code>hc</code>.
    */
    public Collection<HCE> succElemC(HCE hc) {
	final Collection<HCodeEdge<HCE>> succEdges = this.succC(hc);
	return new AbstractCollection<HCE>() {
		public int size() { return succEdges.size(); }
		public Iterator<HCE> iterator() {
		    return new FilterIterator<HCodeEdge<HCE>,HCE>
			(succEdges.iterator(), 
			 new FilterIterator.Filter<HCodeEdge<HCE>,HCE>() {
			    public HCE map(HCodeEdge<HCE> o)
				{ return o.to(); }
			});
		}
	    };
    }

    public Set<HCE> getElements(final HCode<HCE> code) {
	return new AbstractSet<HCE>() {
	    public int size() {
		int i=0;
		for (Iterator<HCE> it=iterator(); it.hasNext(); it.next())
		    i++;
		return i;
	    }
	    public Iterator<HCE> iterator() {
		return new UnmodifiableIterator<HCE>() {
		    // implementation borrowed from IR/Quads/Code.
		    Set<HCE> visited = new HashSet<HCE>();
		    Stack<HCE> s = new Stack<HCE>();
		    { // initialize stack/set.
			Iterator<HCE> it =
			    new ArrayIterator<HCE>(getFirstElements(code));
			HCE[] leaves = getLastElements(code);
			if (leaves!=null)
			    it = new CombineIterator<HCE>
				(new ArrayIterator<HCE>(leaves), it);
			while (it.hasNext()) {
			    HCE hce = it.next();
			    s.push(hce); visited.add(hce);
			}
		    }
		    public boolean hasNext() { return !s.isEmpty(); }
		    public HCE next() {
			if (s.empty()) throw new NoSuchElementException();
			HCE hce = s.pop();
			// push successors on stack before returning.
			Iterator<HCE> it=succElemC(hce).iterator();
			while (it.hasNext()) {
			    HCE nxt = it.next();
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
    public CFGrapher<HCE> edgeReversed() {
	if (reversed==null)
	    reversed=new ReverseGrapher<HCE>(this);
	return reversed;
    }
    private CFGrapher<HCE> reversed = null;
    private static class ReverseGrapher<HCE extends HCodeElement>
	extends SerializableGrapher<HCE> {
	private final CFGrapher<HCE> grapher;
	ReverseGrapher(CFGrapher<HCE> grapher) { this.grapher = grapher; }
	public HCE[] getFirstElements(HCode<HCE> hcode) {
	    return grapher.getLastElements(hcode);
	}
	public HCE getFirstElement(HCode<HCE> hcode) {
	    HCE[] r = getFirstElements(hcode);
	    if (r.length!=1)
		throw new Error("Deprecated use of getFirstElement() not "+
				"supported with edge-reversed graphers.");
	    return r[0];
	}
	public HCE[] getLastElements(HCode<HCE> hcode) {
	    return grapher.getFirstElements(hcode);
	}
	public Collection<HCodeEdge<HCE>> predC(HCE hc) {
	    return reverseEdges(grapher.succC(hc));
	}
	public Collection<HCodeEdge<HCE>> succC(HCE hc) {
	    return reverseEdges(grapher.predC(hc));
	}
	private static <HCE extends HCodeElement>
        Collection<HCodeEdge<HCE>> reverseEdges(Collection<HCodeEdge<HCE>> edges) {
	    HCodeEdge<HCE>[] ea = new HCodeEdge<HCE>[edges.size()];
	    Iterator<HCodeEdge<HCE>> it=edges.iterator();
	    for (int i=0; it.hasNext(); i++) {
		final HCodeEdge<HCE> e = it.next();
		ea[i] = new HCodeEdge<HCE>() {
		    public HCE from() { return e.to(); }
		    public HCE to() { return e.from(); }
		};
	    }
	    return Arrays.asList(ea);
	}
	public CFGrapher<HCE> edgeReversed() { return grapher; }
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
    // XXX in theory, we could use a type system to enforce the
    //  constraint that DEFAULT is a CFGrapher that only works
    //  for HCodeElements which are CFGraphable.  but the GJ
    //  type system's not powerful enough.
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
    private static abstract class SerializableGrapher<HCE extends HCodeElement>
	extends CFGrapher<HCE>
	implements java.io.Serializable { }
}
