// Graph.java, created Thu Oct 15 20:22:46 1998 by marinov
package harpoon.Util;

import harpoon.ClassFile.*;
import harpoon.Analysis.DomTree;
import harpoon.Analysis.DomFrontier;
import harpoon.IR.Properties.Edges;
import java.util.Enumeration;
/**
 * <code>Graph</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: Graph.java,v 1.1 1998-10-16 01:12:31 marinov Exp $
 */

public class Graph  {
    
    /** Creates a <code>Graph</code>. */
    private Graph() { }
    
    /** Print (vcg format) graph representing code view. Use default style. */
    public static final void printGraph(HCode hc, java.io.PrintWriter pw, String title) {
	printGraph(hc,pw,title,null,null);
    }

    /** Print (vcg format) graph representing code view. */
    public static final void printGraph(HCode hc, java.io.PrintWriter pw, String title, String[] setup, String type) {
	pw.println("graph: {");
	pw.println("title: \""+title+"/"+hc.getName()+"\"");
	if (setup==null) {
	    String[] defaultSetup = { "x: 30", "y: 30",
		      "height: 800", "width: 500",
		      "stretch: 60", "shrink: 100",
		      "display_edge_labels: yes",
		      "dirty_edge_labels: yes",
		      "near_edges: no"
	    };
	    setup = defaultSetup;
	}
	for (int i=0; i<setup.length; i++)
	    pw.println(setup[i]);
	int t = (type==null) ? 0 : ((type.equals("dom")) ? 1 : -1);
	pw.println((t!=0) ? "layoutalgorithm: tree" : "priority_phase: yes");
	for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
	    HCodeElement hce = (HCodeElement) e.nextElement();
	    String label = "#" + hce.getID() + ": " + escape(hce.toString());
	    pw.print("node: { ");
	    pw.print("title:\""+hce.getID()+"\" ");
	    pw.print("label:\"" + label + "\" ");
	    pw.print("shape: box ");
	    pw.println("}");
	}
	if (t != 0) {
	    DomTree dt = new DomTree(t==-1);
	    DomFrontier df = new DomFrontier(dt);
	    for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
		HCodeElement hce = (HCodeElement) e.nextElement();
		HCodeElement idom = dt.idom(hc, hce);
		// make dominance frontier label.
		StringBuffer sb = new StringBuffer("DF[");
		sb.append(hce.getID()); sb.append("]={");
		for (Enumeration e2 = df.dfE(hc, hce); e2.hasMoreElements(); ){
		    sb.append(((HCodeElement)e2.nextElement()).getID());
		    if (e2.hasMoreElements())
			sb.append(",");
		}
		sb.append("}");
		if (idom!=null)
		    pw.println(edgeString(idom, hce,
					   sb.toString()));
	    }
	} else {// control flow graph. The HCodeElements better implement Edges
	    for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
		HCodeElement hce = (HCodeElement) e.nextElement();
		HCodeEdge[] next = ((Edges)hce).succ();
		for (int j=0; j<next.length; j++) {
		    String label;
		    if (next.length==1)
			label = null;
		    else if (next.length==2)
			label = (j==0)?"false":"true";
		    else
			label = Integer.toString(j);
		    pw.println(edgeString(next[j].from(), next[j].to(), 
					   label));
		}
	    }
	}
	pw.println("}");
    }

    private static String edgeString(HCodeElement from, HCodeElement to, String label)
    {
	return "edge: { " +
	    "sourcename: \""+from.getID()+"\" " +
	    "targetname: \""+to.getID()+"\" " +
	    ( (label==null)?"":("label: \""+label+"\" ") ) +
	    "}";
    }
    
    private static String escape(String s) {
	s = Util.escape(s);
	return s.replace('\"', ' ');
    }

}
