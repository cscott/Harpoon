// CFGrapher.java, created Mon Nov 29 23:32:45 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;

import java.util.Collection;
/**
 * <code>CFGrapher</code> provides a means to externally associate
 * control-flow graph information with elements of an intermediate
 * representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CFGrapher.java,v 1.1.2.5 2000-06-29 21:28:18 cananian Exp $
 */
public abstract class CFGrapher {
    /** Returns the first <code>HCodeElement</code> to be executed; that is,
     *  the root of the control-flow graph. */
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
    public abstract Collection edgeC(HCodeElement hc);
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
