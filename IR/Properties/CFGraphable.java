// CFGraphable.java, created Mon Nov 29 23:34:03 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;

import java.util.Collection;
/**
 * <code>CFGraphable</code> defines an interface for intermediate 
 * representations that are inherently interconnected in a directed
 * control-flow graphs.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CFGraphable.java,v 1.4 2003-05-09 00:22:48 cananian Exp $
 * @see harpoon.IR.Properties.CFGrapher
 * @see harpoon.IR.Properties.CFGrapher#DEFAULT
 */
public interface CFGraphable<CFG extends CFGraphable<CFG,E>,
			     E extends CFGEdge<CFG,E>>
    extends harpoon.ClassFile.HCodeElement {       
    /** Returns an array of all the edges to and from this 
     *  <code>CFGraphable</code>. */
    public E[] edges();
    /** Returns an array of all the edges entering this
     *  <code>CFGraphable</code>. */
    public E[] pred();
    /** Returns an array of all the edges leaving this
     *  <code>CFGraphable</code>. */
    public E[] succ();

    // JDK 1.2 collections API: [CSA, 15-Jun-1999]
    /** Returns a <code>Collection</code> of all the edges to and from
     *  this <code>HCodeElement</code>. */
    public Collection<E> edgeC();
    /** Returns a <code>Collection</code> of all the edges to
	this <code>HCodeElement</code>. 
        Each <code>CFGEdge</code> returned is guaranteed to return 
	<code>this</code> in response to a call to <code>to()</code>;
	the actual predecessor will be returned from
	<code>from()</code>.  
     */
    public Collection<E> predC();
    /** Returns a <code>Collection</code> of all the edges from
	this <code>HCodeElement</code>. 
        Each <code>CFGEdge</code> returned is guaranteed to return
	<code>this</code> in response to a call to
	<code>from()</code>; the actual successor to <code>this</code>
	will be returned from <code>to()</code>.
     */
    public Collection<E> succC();
}
