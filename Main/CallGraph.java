// CallGraph.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.*;
//import harpoon.IR.Properties.Edges;
//import harpoon.Temp.Temp;
import harpoon.Util.UniqueVector;

import java.util.Enumeration;
/**
 * <code>CallGraph</code> is a command-line call-graph generation tool.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraph.java,v 1.1 1998-10-12 10:11:27 cananian Exp $
 */

public abstract class CallGraph extends harpoon.IR.Registration {

    public static final void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	HMethod m = null;

	if (args.length < 2) {
	    System.err.println("Needs class and method name.");
	    return;
	}

	{
	    HClass cls = HClass.forName(args[0]);
	    HMethod hm[] = cls.getDeclaredMethods();
	    for (int i=0; i<hm.length; i++)
		if (hm[i].getName().equals(args[1])) {
		    m = hm[i];
		    break;
		}
	}

	harpoon.Analysis.QuadSSA.ClassHierarchy ch = 
	    new harpoon.Analysis.QuadSSA.ClassHierarchy(m);
	harpoon.Analysis.QuadSSA.CallGraph cg =
	    new harpoon.Analysis.QuadSSA.CallGraph(ch);

	out.println("graph: {");
	out.println("title: \"Call graph rooted at "+m.getName()+"\"");
	String[] setup = { //"x: 30", "y: 30",
	                   //"height: 800", "width: 500",
			   "stretch: 60", "shrink: 100",
			   "ignore_singles: no",
			   "display_edge_labels: no",
			   "classname1: \"class summary\"",
			   "classname2: \"call graph\"",
			   "hidden: 1",
			   // "layoutalgorithm: ???",
	};
	for (int i=0; i<setup.length; i++)
	    out.println(setup[i]);

	// make class names.
	/*
	int z=1;
	for (Enumeration e = ch.classes(); e.hasMoreElements(); z++) {
	    HClass hc = (HClass) e.nextElement();
	    //out.println("classname"+z+": \""+hc.getName()+"\"");
	    //out.println("hidden: "+z);
	}
	out.println("classname"+z+": \"call graph\"");
	*/
	// make nodes.
	for (Enumeration e = ch.classes(); e.hasMoreElements(); ) {
	    HClass hc = (HClass) e.nextElement();
	    HMethod hm[] = hc.getDeclaredMethods();
	    for (int i=0; i<hm.length; i++)
		out.println("  node: { title:\""+nodeString(hm[i])+"\" }");
	}
	// make invisible summary-node edges
	int z=1;
	for (Enumeration e = ch.classes(); e.hasMoreElements(); /*z++*/) {
	    HClass hc = (HClass) e.nextElement();
	    HMethod hm[] = hc.getDeclaredMethods();
	    for (int i=0; i<hm.length; i++) {
		out.println(edgeString(hm[(i==0?hm.length:i)-1],hm[i],
				       "linestyle: invisible class: "+z));
	    }
	} z++;
	// make edges.
	UniqueVector uv = new UniqueVector();
	uv.addElement(m);
	for (int i=0; i<uv.size(); i++) {
	    HMethod hm = (HMethod) uv.elementAt(i);
	    HMethod children[] = cg.calls(hm);
	    for (int j=0; j<children.length; j++) {
		uv.addElement(children[j]);
		out.println(edgeString(hm, children[j], "class: "+z));
	    }
	}
	// done.
	out.println("}");
    }

    static String edgeString(HMethod from, HMethod to, String otherinfo)
    {
	return "edge: { " +
	    "sourcename: \""+nodeString(from)+"\" " +
	    "targetname: \""+nodeString( to )+"\" " +
	    ( (otherinfo==null)?"":otherinfo ) +
	    "}";
    }
    static String nodeString(HMethod m) {
	// Modified version of HMethod.toString.
	StringBuffer r = new StringBuffer();
	r.append(getTypeName(m.getDeclaringClass()));
	if (!(m instanceof HConstructor)) {
	    r.append('.');
	    r.append(m.getName());
	}
	r.append('(');
	HClass hcp[] = m.getParameterTypes();
	for (int i=0; i<hcp.length; i++) {
	    r.append(getTypeName(hcp[i]));
	    if (i<hcp.length-1)
		r.append(',');
	}
	r.append(')');
	return r.toString();
    }
    // From HField:
    static String getTypeName(HClass hc) {
	if (hc.isArray()) {
	    StringBuffer r = new StringBuffer();
	    HClass sup = hc;
	    int i=0;
	    for (; sup.isArray(); sup = sup.getComponentType())
		i++;
	    r.append(sup.getName());
	    for (int j=0; j<i; j++)
		r.append("[]");
	    return r.toString();
	}
	return hc.getName();
    }
}
