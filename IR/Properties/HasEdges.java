// HasEdges.java, created Sat Sep 12 18:01:09 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Properties;

import harpoon.ClassFile.HCodeEdge;

import java.util.Collection;
/**
 * <code>HasEdges</code> defines an interface for intermediate representations
 * that are interconnected as directed graphs.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HasEdges.java,v 1.1.2.4 1999-11-05 22:32:54 pnkfelix Exp $
 */

public interface HasEdges extends harpoon.ClassFile.HCodeElement {
    
    /** Returns an array of all the edges to and from this 
     *  <code>HasEdges</code>. */
    public HCodeEdge[] edges();
    public HCodeEdge[] pred();
    public HCodeEdge[] succ();

    // JDK 1.2 collections API: [CSA, 15-Jun-1999]
    /** Returns a <code>Collection</code> of all the edges to and from
     *  this <code>HCodeElement</code>. */
    public Collection edgeC();
    /** Returns a <code>Collection</code> of all the edges to
	this <code>HCodeElement</code>. 
        Each <code>HCodeEdge</code> returned is guaranteed to return 
	<code>this</code> in response to a call to <code>to()</code>;
	the actual predecessor will be returned from
	<code>from()</code>.  
     */
    public Collection predC();
    /** Returns a <code>Collection</code> of all the edges from
	this <code>HCodeElement</code>. 
        Each <code>HCodeEdge</code> returned is guaranteed to return
	<code>this</code> in response to a call to
	<code>from()</code>; the actual successor to <code>this</code>
	will be returned from <code>to()</code>.
     */
    public Collection succC();
}
