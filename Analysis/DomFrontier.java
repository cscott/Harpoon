// DomFrontier.java, created Mon Sep 14 22:21:38 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.Util.ArrayIterator;
import harpoon.Util.ArraySet;
import harpoon.Util.Default;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
/**
 * <code>DomFrontier</code> computes the dominance frontier of a 
 * flowgraph-structured IR.  The <code>HCodeElement</code>s must implement
 * the <code>harpoon.IR.Properties.CFGraphable</code> interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DomFrontier.java,v 1.6.2.8 2000-11-10 21:56:05 cananian Exp $
 */

public class DomFrontier  {
    final HCode hc;
    /** Creates a <code>DomFrontier</code> using a pre-existing
     *  <code>DomTree</code>. */
    public DomFrontier(DomTree dt) {
	this.hc = dt.hcode;
	analyze(dt);
    }
    /** Creates a <code>DomFrontier</code> for the given
     *  <code>HCode</code> using the default grapher; if
     *  <code>isPost</code> is <code>false</code> creates the
     *  dominance frontier; otherwise creates the postdominance
     *  frontier. */
    public DomFrontier(HCode hcode, boolean isPost) {
	this(hcode, CFGrapher.DEFAULT, isPost);
    }
    /** Creates a <code>DomFrontier</code> for the given
     *  <code>HCode</code> using the given <code>CFGrapher</code>; if
     *  <code>isPost</code> is <code>false</code> creates the
     *  dominance frontier; otherwise creates the postdominance
     *  frontier. */
    public DomFrontier(HCode hcode, CFGrapher grapher, boolean isPost) {
	this(new DomTree(hcode, grapher, isPost));
    }

    Hashtable DF = new Hashtable();

    /** Return an array of <code>HCodeElement</code>s in the (post)dominance
     *  frontier of <code>n</code>.
     */
    public HCodeElement[] df(HCodeElement n) {
	HCodeElement[] r =  (HCodeElement[]) DF.get(n);
	if (r==null)
	    return (HCodeElement[]) hc.elementArrayFactory().newArray(0);
	else
	    return (HCodeElement[]) Util.safeCopy(hc.elementArrayFactory(), r);
    }
    /** Return an immutable <code>Set</code> of <code>HCodeElement</code>s
     *  in the (post)dominance frontier of <code>n</code>.
     */
    public Set dfS(HCodeElement n) {
	HCodeElement[] r =  (HCodeElement[]) DF.get(n);
	if (r==null)
	    return Collections.EMPTY_SET;
	else
	    return new ArraySet(r);
    }

    void analyze(DomTree dt) {
	HCodeElement[] roots = dt.roots();
	for (int i=0; i < roots.length; i++)
	    computeDF(dt, roots[i]);
    }
    void computeDF(DomTree dt, HCodeElement n) {
	CFGrapher grapher = dt.grapher;
	Set S = new WorkSet();
	
	// for every child y in succ[n]
	for (Iterator it=grapher.succC(n).iterator(); it.hasNext(); ) {
	    HCodeElement y = ((HCodeEdge)it.next()).to();
	    if (!n.equals( dt.idom(y) ))
		S.add(y);
	}
	// for each child c of n in the (post)dominator tree
	for (Iterator it=new ArrayIterator(dt.children(n)); it.hasNext(); ) {
	    HCodeElement c = (HCodeElement) it.next();
	    computeDF(dt, c);
	    // for each element w of DF[c]
	    HCodeElement[] w = (HCodeElement[]) DF.get(c);
	    for (int j=0; j < w.length; j++)
		if (!n.equals( dt.idom(w[j]) ))
		    S.add(w[j]);
	}
	// DF[n] <- S
	HCodeElement dfn[] = 
	    (HCodeElement[]) dt.hcode.elementArrayFactory().newArray(S.size());
	S.toArray(dfn);
	DF.put(n, dfn);
    }
}
