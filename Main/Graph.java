package harpoon.Main;

import harpoon.ClassFile.*;
import harpoon.Analysis.DomTree;
import harpoon.IR.Properties.Edges;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>Graph</code> is a command-line graph generation tool.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Graph.java,v 1.3 1998-09-15 11:16:13 cananian Exp $
 */

public final class Graph extends harpoon.IR.Registration {
    // hide away constructor.
    private Graph() { }

    /** The compiler should be invoked with the names of classes
     *  extending <code>java.lang.Thread</code>.  These classes
     *  define the external interface of the machine. */
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
			   "near_edges: no" };
	for (int i=0; i<setup.length; i++)
	    out.println(setup[i]);
	
	HCodeElement[] el = hc.getElements();
	for (int i=0; i<el.length; i++) {
	    out.print("node: { ");
	    out.print("title:\""+el[i].getID()+"\" ");
	    out.print("label:\"#" + el[i].getID() + ": " + 
		      escape(el[i].toString())+"\" ");
	    out.print("shape: box");
	    out.println("}");
	}
	if (args.length>2 && 
	    (args[2].equals("dom") || args[2].equals("post"))) {
	    DomTree dt = new DomTree(args[2].equals("post"));
	    for (int i=0; i<el.length; i++) {
		HCodeElement idom = dt.idom(hc, el[i]);
		if (idom!=null)
		    out.println(edgeString(idom, el[i]));
	    }
	} else {// control flow graph. The HCodeElements better implement Edges
	    for (int i=0; i<el.length; i++) {
		HCodeEdge[] next = ((Edges)el[i]).succ();
		for (int j=0; j<next.length; j++)
		    out.println(edgeString(next[j].from(), next[j].to()));
	    }
	}
	out.println("}");
    }

    static String edgeString(HCodeElement from, HCodeElement to) {
	return "edge: { " +
	    "sourcename: \""+from.getID()+"\" " +
	    "targetname: \""+to.getID()+"\" " +
	    "}";
    }

    static String escape(String s) {
	s = Util.escape(s);
	return s.replace('\"', ' ');
    }
}
