// Graph.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.*;
import harpoon.Analysis.DomTree;
import harpoon.Analysis.DomFrontier;
import harpoon.IR.Properties.Edges;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Enumeration;
/**
 * <code>Graph</code> is a command-line graph generation tool.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Graph.java,v 1.10 1998-10-11 03:01:17 cananian Exp $
 */

public abstract class Graph extends harpoon.IR.Registration {

    public static final void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);

	HClass cls = HClass.forName(args[0]);
	HMethod hm[] = cls.getDeclaredMethods();
	HMethod m = null;
	for (int i=0; i<hm.length; i++)
	    if (hm[i].getName().equals(args[1])) {
		m = hm[i];
		break;
	    }

	String codetype = "quad-ssa";
	if (args.length > 3)
	    codetype = args[3];
	HCode hc = m.getCode(codetype);

	out.println("graph: {");
	out.println("title: \""+m.getName()+"/"+hc.getName()+"\"");
	String[] setup = { "x: 30", "y: 30",
			   "height: 800", "width: 500",
			   "stretch: 60", "shrink: 100",
			   "display_edge_labels: yes",
			   "dirty_edge_labels: yes",
			   "near_edges: no"
	};
	for (int i=0; i<setup.length; i++)
	    out.println(setup[i]);
	if (args.length>2 && 
	    (args[2].equals("dom") || args[2].equals("post")))
	    out.println("layoutalgorithm: tree");
	else
	    out.println("priority_phase: yes");
	
	for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
	    HCodeElement hce = (HCodeElement) e.nextElement();
	    String label = "#" + hce.getID() + ": " + escape(hce.toString());
	    out.print("node: { ");
	    out.print("title:\""+hce.getID()+"\" ");
	    out.print("label:\"" + label + "\" ");
	    out.print("shape: box ");
	    out.println("}");
	}
	if (args.length>2 && 
	    (args[2].equals("dom") || args[2].equals("post"))) {
	    DomTree dt = new DomTree(args[2].equals("post"));
	    DomFrontier df = new DomFrontier(dt);
	    for (Enumeration e=hc.getElementsE(); e.hasMoreElements(); ) {
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
		    out.println(edgeString(idom, hce,
					   sb.toString()));
	    }
	} else {// control flow graph. The HCodeElements better implement Edges
	    for (Enumeration e=hc.getElementsE(); e.hasMoreElements(); ) {
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
		    out.println(edgeString(next[j].from(), next[j].to(), 
					   label));
		}
	    }
	}
	out.println("}");
    }

    static String edgeString(HCodeElement from, HCodeElement to, String label)
    {
	return "edge: { " +
	    "sourcename: \""+from.getID()+"\" " +
	    "targetname: \""+to.getID()+"\" " +
	    ( (label==null)?"":("label: \""+label+"\" ") ) +
	    "}";
    }

    static String escape(String s) {
	s = Util.escape(s);
	return s.replace('\"', ' ');
    }
}
