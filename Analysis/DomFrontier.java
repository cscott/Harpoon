// DomFrontier.java, created Mon Sep 14 22:21:38 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.HasEdges;
import harpoon.Util.Util;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.NullEnumerator;
import harpoon.Util.ArrayEnumerator;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>DomFrontier</code> computes the dominance frontier of a 
 * flowgraph-structured IR.  The <code>HCodeElement</code>s must implement
 * the <code>harpoon.IR.Properties.HasEdges</code> interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DomFrontier.java,v 1.6.2.5 1999-05-19 06:45:08 andyb Exp $
 */

public class DomFrontier  {
    DomTree dt; // a dom tree to use, or null
    boolean isPost;

    /** Creates a <code>DomFrontier</code>, using a pre-existing
     *  <code>DomTree</code>. <p>
     *  This version of the constructor keeps the dominator tree 
     *  structure around after analysis is completed and reuses it. */
    public DomFrontier(DomTree dt) {
        this.dt = dt;
	this.isPost = dt.isPost;
    }
    /** Creates a <code>DomFrontier</code>; if <code>isPost</code> is
     *  <code>false</code> creates the dominance frontier; otherwise
     *  creates the postdominance frontier. <p>
     *  This version of the constructor frees the dominator tree after the
     *  frontier has been created. */
    public DomFrontier(boolean isPost) {
	this.dt = null;
	this.isPost = isPost;
    }

    Hashtable DF = new Hashtable();
    Hashtable analyzed = new Hashtable();

    /** Return an array of <code>HCodeElement</code>s in the (post)dominance
     *  frontier of <code>n</code>.
     *  @param hc the <code>HCode</code> containing <code>n</code>.
     */
    public HCodeElement[] df(HCode hc, HCodeElement n) {
	analyze(hc); 
	HCodeElement[] r =  (HCodeElement[]) DF.get(n);
	if (r==null)
	    return (HCodeElement[]) hc.elementArrayFactory().newArray(0);
	else
	    return (HCodeElement[]) Util.safeCopy(hc.elementArrayFactory(), r);
    }
    /** Return an Enumeration of <code>HCodeElement</code>s in the 
     *  (post)dominance frontier of <code>n</code>.
     *  @param hc the <code>HCode</code> containing <code>n</code>.
     */
    public Enumeration dfE(HCode hc, HCodeElement n) {
	analyze(hc);
	HCodeElement[] r =  (HCodeElement[]) DF.get(n);
	if (r==null)
	    return NullEnumerator.STATIC;
	else
	    return new ArrayEnumerator(r);
    }

    HCode lastHCode = null;
    void analyze(HCode hc) {
	if (hc == lastHCode) return ; // just did this one.
	if (analyzed.get(hc) != null); // hashtable sez we've done it already.
	analyzed.put(hc, hc);
	lastHCode = hc;

	// maybe we don't want to keep the dominator tree around.
	boolean tempDT = (dt==null);
	if (tempDT) dt = new DomTree(isPost);

	HCodeElement[] roots;
	if (!isPost) {
	    roots = (HCodeElement[]) hc.elementArrayFactory().newArray(1);
	    roots[0] = hc.getRootElement();
	} else
	    roots = hc.getLeafElements();
	
	for (int i=0; i < roots.length; i++)
	    computeDF(hc, roots[i]);

	if (tempDT) dt = null; // free the dominator tree.
    }
    void computeDF(HCode hc, HCodeElement n) {
	Set S = new HashSet();
	
	// for every child y in succ[n]
	HCodeEdge[] yl = (!isPost) ? ((HasEdges)n).succ() : ((HasEdges)n).pred();
	for (int i=0; i < yl.length; i++) {
	    HCodeElement y = (!isPost) ? yl[i].to() : yl[i].from();
	    if (!n.equals( dt.idom(hc, y) ))
		S.union(y);
	}
	// for each child c of n in the (post)dominator tree
	HCodeElement[] c = dt.children(hc, n);
	for (int i=0; i < c.length; i++) {
	    computeDF(hc, c[i]);
	    // for each element w of DF[c]
	    HCodeElement[] w = (HCodeElement[]) DF.get(c[i]);
	    for (int j=0; j < w.length; j++)
		if (!n.equals( dt.idom(hc, w[j]) ))
		    S.union(w[j]);
	}
	// DF[n] <- S
	HCodeElement dfn[] = 
	    (HCodeElement[]) hc.elementArrayFactory().newArray(S.size());
	S.copyInto(dfn);
	DF.put(n, dfn);
    }
}
