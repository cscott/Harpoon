// Graph.java, created Thu Oct 15 20:22:46 1998 by marinov
// Copyright (C) 1998 Darko Marinov <marinov@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Analysis.DomTree;
import harpoon.Analysis.DomFrontier;
import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.FOOTER;


import harpoon.Util.LightBasicBlocks.LightBasicBlock;
import harpoon.Util.LightBasicBlocks.LBBConverter;
import harpoon.Util.BasicBlocks.BBConverter;

import harpoon.Temp.Temp;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import java.io.PrintWriter;


/**
 * <code>Graph</code>
 * 
 * @author  Darko Marinov <marinov@lcs.mit.edu>
 * @version $Id: Graph.java,v 1.2.2.13 2001-12-16 04:38:51 salcianu Exp $
 */

public abstract class Graph  {
    
    /** Print (vcg format) control from graph representing code view. Use default style. */
    public static final void printCFG(HCode hc, java.io.PrintWriter pw, String title) {
	printCFG(hc,pw,title,null);
    }

    /** Print (vcg format) DomTree of code view. Use default style. */
    public static final void printDomTree(HCode hc, java.io.PrintWriter pw, String title) {
	printDomTree(false,hc,pw,title,null);
    }

    /** Print (vcg format) (Post)DomTree of code view. Use default style. */
    public static final void printDomTree(boolean isPost, HCode hc, java.io.PrintWriter pw, String title) {
	printDomTree(isPost,hc,pw,title,null);
    }

    /** Print (vcg format) control flow graph representing code view. */
    public static final void printCFG(HCode hc, java.io.PrintWriter pw, String title, String[] setup) {
	commonHeader(hc, pw, title, setup, "CFG");
	// control flow graph. The HCodeElements better implement CFGraphable
	for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
	    HCodeElement hce = (HCodeElement) e.nextElement();
	    HCodeEdge[] next = ((CFGraphable)hce).succ();
	    for (int j=0; j<next.length; j++) {
		String label;
		if (next.length==1)
		    label = null;
		else if (next.length==2)
		    label = (j==0)?"false":"true";
		else
		    label = Integer.toString(j);
		// also print which_pred of edge, for QuadSSI.
		if (next[j] instanceof harpoon.IR.Quads.Edge &&
		    ((CFGraphable)next[j].to()).pred().length > 1)
		    label = ((label==null)?"":label) + "[" + 
			((harpoon.IR.Quads.Edge)next[j]).which_pred() +
			"]";
		pw.println(edgeString(next[j].from(), next[j].to(), 
				      label));
	    }
	}
	commonFooter(pw);
    }

    /** Print (vcg format) of DomTree. */
    public static final void printDomTree(HCode hc, java.io.PrintWriter pw, String title, String[] setup) {
	printDomTree(false,hc,pw,title,setup);
    }

    /** Print (vcg format) of (Post)DomTree. */
    public static final void printDomTree(boolean isPost, HCode hc, java.io.PrintWriter pw, String title, String[] setup) {
	commonHeader(hc, pw, title, setup, "DomTree");
	DomTree dt = new DomTree(hc, isPost);
	DomFrontier df = new DomFrontier(dt);
	for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
	    HCodeElement hce = (HCodeElement) e.nextElement();
	    HCodeElement idom = dt.idom(hce);
	    // make dominance frontier label.
	    StringBuffer sb = new StringBuffer("DF[");
	    sb.append(hce.getID()); sb.append("]={");
	    for (Iterator it2=df.dfS(hce).iterator(); it2.hasNext(); ) {
		sb.append(((HCodeElement)it2.next()).getID());
		if (it2.hasNext())
		    sb.append(",");
	    }
	    sb.append("}");
	    if (idom!=null)
		pw.println(edgeString(idom, hce,
				      sb.toString()));
	}
	commonFooter(pw);
    }

    public static final void printClassHierarchy(java.io.PrintWriter pw, HMethod root, ClassHierarchy ch) {
	pw.println("graph: {");
	pw.println(" title: \"Class Hierarchy: "+root+"\"");
	pw.println(" x: 30");
	pw.println(" y: 30");
	pw.println(" height: 800");
	pw.println(" width: 500");
	pw.println(" stretch: 60");
	pw.println(" shrink: 100");
	pw.println(" display_edge_labels: no");
	pw.println(" dirty_edge_labels: no");
	pw.println(" near_edges: no");
	pw.println(" orientation: left_to_right");
	pw.println(" layoutalgorithm: minbackward");
	pw.println(" port_sharing: no");
	pw.println(" arrowmode: free");
	for (Iterator it = ch.classes().iterator(); it.hasNext(); ) {
	    HClass c = (HClass) it.next();
	    pw.print(" node: { ");
	    pw.print("title:\""+c.getName()+"\" ");
	    pw.print("label:\""+c.getName()+ "\" ");
	    pw.print("shape: box ");
	    pw.println("}");
	}	
	for (Iterator it = ch.classes().iterator(); it.hasNext(); ) {
	    HClass c = (HClass) it.next();
	    HClass sc= c.getSuperclass();
	    if (sc!=null)
		pw.println(" edge: { " +
			   "sourcename: \""+sc.getName()+"\" " +
			   "targetname: \""+ c.getName()+"\" " +
			   "}");
	    HClass[] in=c.getInterfaces();
	    for (int i=0; i<in.length; i++)
		pw.println(" edge: { " +
			   "sourcename: \""+in[i].getName()+"\" " +
			   "targetname: \""+ c.getName()+"\" " +
			   "}");
	}
	pw.println("}");
    }

    /** Print common header of (vcg format) graphs for CFG and (Post)DomTree. */
    private static void commonHeader(HCode hc, java.io.PrintWriter pw, String title, String[] setup, String type) {
	pw.println("graph: {");
	pw.println("title: \""+title+"/"+hc.getName()+"\"");
	if (setup==null) {
	    String[] defaultSetup = { "x: 30", "y: 30", "yspace: 15",
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
	pw.println((type!="CFG") ? "layoutalgorithm: tree" : "priority_phase: yes");
	for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
	    HCodeElement hce = (HCodeElement) e.nextElement();
	    String label = "#" + hce.getID() + ": " + escape(hce.toString());
	    pw.print("node: { ");
	    pw.print("title:\""+hce.getID()+"\" ");
	    pw.print("label:\"" + label + "\" ");
	    pw.print("shape: box ");
	    pw.println("}");
	}

    }

    private static void commonFooter (java.io.PrintWriter pw) {
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




    /** Print the Control Flow Graph of the Basic Blocks composing
	the code of method <code>hm</code>, as constructed by the
	code factory <code>hcf</code>. The output is written to
	<code>out</code> in VCG format. */
    public static void printBBCFG(HMethod hm, HCodeFactory hcf,
				  PrintWriter out) {
	BBConverter bbconv = new BBConverter(hcf);
	LBBConverter lbbconv = new LBBConverter(bbconv);
	printBBCFG(hm, lbbconv, out);
    }

    /** Print the Control Flow Graph of the Basic Blocks composing
	the code of method <code>hm</code>, as constructed by the
	<code>LBBConverter</code> <code>hcf</code>.
	The output is written to <code>out</code> in VCG format. */
    public static void printBBCFG(HMethod hm, LBBConverter lbbconv,
				  PrintWriter out) {
	LightBasicBlock.Factory lbbfact = lbbconv.convert2lbb(hm);

	print_VCG_header(out, "BB_CFG for " + hm.getName());
	Map map = print_VCG_nodes(out, lbbfact);
	print_VCG_edges(out, lbbfact, map);
	print_VCG_footer(out);
    }

    private static void print_VCG_header(PrintWriter out, String graph_name) {
	out.println("graph: {");
	out.println("\ttitle: \"" + graph_name + "\"");
	out.println("\tlayoutalgorithm: maxdepthslow");
	out.println("\tdisplay_edge_labels: yes");
    }

    private static Map print_VCG_nodes
	(PrintWriter out, LightBasicBlock.Factory lbbfact) {
	out.println("\n/* (light) basic block description */");
	Map map = new HashMap();
	LightBasicBlock lbbs[] = lbbfact.getAllBBs();
	for(int i = 0; i < lbbs.length ; i++) {
	    String lbb_name = Integer.toString(i);
	    map.put(lbbs[i], lbb_name);
	    print_VCG_node(out, lbbs[i], lbb_name);
	}
	out.println();
	return map;
    }

    private static void print_VCG_node
	(PrintWriter out, LightBasicBlock lbb, String lbb_name) {
	out.println("node: {");
	out.println("\ttitle: \"" + lbb_name + "\"");
	out.print("\tlabel: \"");
	HCodeElement quads[] = lbb.getElements();
	for(int i = 0; i < quads.length; i++)
	    out.print( ((i == 0) ? "" : "\\n") +
			 quad2string(quads[i]) );
	out.println("\"");
	out.println("}");
    }

    private static String quad2string(HCodeElement hce) {
	if(!(hce instanceof CALL))
	    return hce.toString();
	CALL call = (CALL) hce;
	StringBuffer buff = new StringBuffer();
	if(call.retval() != null) {
	    buff.append(call.retval());
	    buff.append(" = ");
	}
	if(call.isStatic())
	    buff.append("(static) ");
	buff.append("CALL ");
	HMethod hm = call.method();

	String classname = hm.getDeclaringClass().getName();
	classname = classname.substring(classname.lastIndexOf(".") + 1);
	buff.append(classname);

	buff.append(".");
	buff.append(hm.getName());
	buff.append("(");
	Temp params[] = call.params();
	for(int i = 0; i < params.length; i++)
	    buff.append(((i == 0) ? "" : ",") + params[i]);
	buff.append(")");
	return buff.toString();
    }

    private static void print_VCG_edges
	(PrintWriter out, LightBasicBlock.Factory lbbfact, Map map) {
	out.println("\n/* control flow edges */");

	METHOD method = get_METHOD(lbbfact);

	LightBasicBlock lbbs[] = lbbfact.getAllBBs();
	for(int i = 0; i < lbbs.length; i++) {
	    LightBasicBlock lbb = lbbs[i];
	    String sourcename = (String) map.get(lbb);

	    LightBasicBlock next[] = lbb.getNextLBBs();
	    for(int j = 0; j < next.length; j++) {
		String targetname = (String) map.get(next[j]);
		out.println("edge: { " +
			    "sourcename : \"" + sourcename + "\" " +
			    "targetname : \"" + targetname + "\" " +
			    ((j >= lbb.getHandlerStartIndex()) ? 
			     "linestyle : dashed " : "" ) +
			    "}");
	    }
	}
    }

    private static METHOD get_METHOD(LightBasicBlock.Factory lbbfact) {
	LightBasicBlock root = lbbfact.getRoot();
	HEADER header = (HEADER) root.getFirstElement();
	return (METHOD) header.next(1);
    }

    private static void print_VCG_footer(PrintWriter out) {
	out.println("}");
    }

}
