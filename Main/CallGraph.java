// CallGraph.java, created Mon Oct 12  6:11:27 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.Util.Collections.UniqueVector;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
/**
 * <code>CallGraph</code> is a command-line call-graph generation tool.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraph.java,v 1.3 2002-02-25 21:06:05 cananian Exp $
 */

public abstract class CallGraph extends harpoon.IR.Registration {

    public static final void main(String args[]) {
	java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
	Linker linker = Loader.systemLinker;
	HMethod m = null;

	if (args.length < 2) {
	    System.err.println("Needs class and method name.");
	    return;
	}

	{
	    HClass cls = linker.forName(args[0]);
	    HMethod hm[] = cls.getDeclaredMethods();
	    for (int i=0; i<hm.length; i++)
		if (hm[i].getName().equals(args[1])) {
		    m = hm[i];
		    break;
		}
	}

	HCodeFactory hcf =
	    new CachingCodeFactory(harpoon.IR.Quads.QuadNoSSA.codeFactory());
	harpoon.Analysis.ClassHierarchy ch = 
	    new harpoon.Analysis.Quads.QuadClassHierarchy
	    (linker, Collections.singleton(m), hcf);
	harpoon.Analysis.Quads.CallGraph cg =
	    new harpoon.Analysis.Quads.CallGraphImpl(ch, hcf);

	out.println("graph: {");
	out.println("title: \"Call graph rooted at "+m.getName()+"\"");
	String[] setup = { "x: 30", "y: 30",
	                   "height: 800", "width: 500",
			   "stretch: 60", "shrink: 100",
			   "display_edge_labels: no",
			   "classname1: \"class summary\"",
			   "classname2: \"call graph\"",
			   "hidden: 1",
			   "layoutalgorithm: maxdepthslow",
			   "finetuning: no",
			   "cmin: 50 rmin:50 pmin: 50",
	};
	for (int i=0; i<setup.length; i++)
	    out.println(setup[i]);

	// traverse the call tree.
	StringBuffer nodes = new StringBuffer();
	StringBuffer edges = new StringBuffer();

	UniqueVector uv = new UniqueVector();
	uv.addElement(m);
	for (int i=0; i<uv.size(); i++) {
	    // make node
	    HMethod hm = (HMethod) uv.elementAt(i);
	    nodes.append("node: { title:\""+nodeString(hm)+"\" ");
	    nodes.append("textcolor: "+nodeColor(hm)+" }\n");
	    // make edges
	    HMethod child[] = cg.calls(hm);
	    for (int j=0; j<child.length; j++) {
		edges.append(edgeString(hm, child[j], 
					"class: 2 color: "+nodeColor(hm)));
		uv.addElement(child[j]); // add to worklist.
	    }
	}
	// make invisible summary-node edges
	Hashtable classMethods = new Hashtable();
	for (int i=0; i<uv.size(); i++) {
	    HMethod hm = (HMethod) uv.elementAt(i);
	    HClass  hc = hm.getDeclaringClass();
	    Vector v = (Vector) classMethods.get(hc);
	    if (v==null) { v = new Vector(); classMethods.put(hc, v); }
	    v.addElement(hm);
	}
	for (Enumeration e = classMethods.keys(); e.hasMoreElements(); ) {
	    HClass hc = (HClass) e.nextElement();
	    Vector v = (Vector) classMethods.get(hc);
	    for (int i=0; i<v.size(); i++) {
		int from = ( (i==0)?v.size():i ) - 1;
		edges.append(edgeString((HMethod) v.elementAt(from),
					(HMethod) v.elementAt(i),
					"linestyle: invisible class: 1"));
	    }
	}
	// print all this schtuff out.
	out.println(nodes);
	out.println(edges);
	out.println("}");
    }

    static String edgeString(HMethod from, HMethod to, String otherinfo)
    {
	return "edge: { " +
	    "sourcename: \""+nodeString(from)+"\" " +
	    "targetname: \""+nodeString( to )+"\" " +
	    ( (otherinfo==null)?"":otherinfo ) +
	    "}\n";
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
    static String nodeColor(HMethod m) {
	// list of color values to skip:
	int not[]=new int[]{ 0/*white*/, 4/*yellow*/, 6/*cyan*/, 28/*pink*/};
	int color = m.getDeclaringClass().hashCode()%(32-not.length);
	for (int i=0; i<not.length; i++)
	    if (color >= not[i]) color++;
	return ""+color;
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
