// DomFrontier.java, created Mon Sep 14 22:21:38 1998 by cananian
package harpoon.Analysis;

import harpoon.ClassFile.*;
import harpoon.IR.Properties.Edges;
import harpoon.Util.UniqueVector;

import java.util.Hashtable;
/**
 * <code>DomFrontier</code> computes the dominance frontier of a 
 * flowgraph-structured IR.  The <code>HCodeElement</code>s must implement
 * the <code>harpoon.IR.Properties.Edges</code> interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DomFrontier.java,v 1.2 1998-09-15 03:40:05 cananian Exp $
 */

public class DomFrontier  {
    DomTree dt;
    boolean isPost;

    /** Creates a <code>DomFrontier</code>, using a pre-existing
     *  <code>DomTree</code>.*/
    public DomFrontier(DomTree dt) {
        this.dt = dt;
	this.isPost = dt.isPost;
    }
    /** Creates a <code>DomFrontier</code>; if <code>isPost</code> is
     *  <code>false</code> creates the dominance frontier; otherwise
     *  creates the postdominance frontier. */
    public DomFrontier(boolean isPost) {
	this(new DomTree(isPost));
    }
    /** Creates a <code>DomFrontier</code> representing the 
     *  dominance frontier. */
    public DomFrontier() { this(false); }

    Hashtable DF = new Hashtable();
    Hashtable analyzed = new Hashtable();

    /** Return the set of <code>HCodeElement</code>s in the (post)dominance
     *  frontier of <code>n</code>.
     *  @param hc the <code>HCode</code> containing <code>n</code.
     */
    public HCodeElement[] DF(HCode hc, HCodeElement n) {
	analyze(hc); 
	HCodeElement[] r =  (HCodeElement[]) DF.get(n);
	if (r==null) return new HCodeElement[0];
	else return r;
    }

    void analyze(HCode hc) {
	if (analyzed.get(hc) == null) {
	    analyzed.put(hc, hc);

	    HCodeElement[] roots;
	    if (!isPost)
		roots = new HCodeElement[] { hc.getRootElement() };
	    else
		roots = hc.getLeafElements();
	    
	    for (int i=0; i < roots.length; i++)
		computeDF(hc, roots[i]);
	}
    }
    void computeDF(HCode hc, HCodeElement n) {
	UniqueVector S = new UniqueVector();
	
	// for every child y in succ[n]
	HCodeEdge[] yl = (!isPost) ? ((Edges)n).succ() : ((Edges)n).pred();
	for (int i=0; i < yl.length; i++) {
	    HCodeElement y = (!isPost) ? yl[i].to() : yl[i].from();
	    if (!n.equals( dt.idom(hc, y) ))
		S.addElement(y);
	}
	// for each child c of n in the (post)dominator tree
	HCodeElement[] c = dt.children(hc, n);
	for (int i=0; i < c.length; i++) {
	    computeDF(hc, c[i]);
	    // for each element w of DF[c]
	    HCodeElement[] w = (HCodeElement[]) DF.get(c[i]);
	    for (int j=0; j < w.length; j++)
		if (!n.equals( dt.idom(hc, w[j]) ))
		    S.addElement(w[j]);
	}
	// DF[n] <- S
	HCodeElement dfn[] = new HCodeElement[S.size()];
	S.copyInto(dfn);
	DF.put(n, dfn);
    }
}
