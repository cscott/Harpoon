// Print.java, created Thu Dec 17 17:31:26 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCode.PrintCallback;
import harpoon.IR.LowQuad.PCALL;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>Print</code> class pretty-prints a quad representation,
 * inserting labels to make the control flow clear.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Print.java,v 1.5 2003-07-10 02:18:23 cananian Exp $
 */
abstract class Print  {
    /** Print <code>Quad</code> code representation <code>c</code> to
     *  <code>PrintWriter</code> <code>pw</code> using the specified
     *  <code>PrintCallback</code>. */
    final static void print(PrintWriter pw, Code c, PrintCallback<Quad> callback) {
	if (callback==null) callback = new PrintCallback<Quad>(); // nop callback
	// get elements.
	Quad[] ql = c.getElements();
	METHOD qM = ((HEADER) c.getRootElement()).method();
	// compile list of back edges
	Map<Quad,Label> labels = new HashMap<Quad,Label>();
	for (int i=0; i<ql.length; i++) {
	    // if has more than one edge, then other edges branch to labels.
	    for (int j=0; j<ql[i].next().length; j++) {
		if (j!=0 || i==ql.length-1 || ql[i].next(j)!=ql[i+1] ||
		    ql[i] instanceof SWITCH || ql[i] instanceof TYPESWITCH)
		    if (!labels.containsKey(ql[i].next(j)))
			labels.put(ql[i].next(j), new Label());
		if (ql[i].next(j) instanceof PHI)
		    if (!labels.containsKey(ql[i]))
			labels.put(ql[i], new Label());
	    }
	}
	// number labels.
	for (int i=0, j=0; i<ql.length; i++)
	    if (labels.containsKey(ql[i]))
		labels.get(ql[i]).renumber(j++);

	// okay, print these pookies.
	pw.println("Codeview \""+c.getName()+"\" for "+c.getMethod()+":");
	HandlerSet hs = null; // no handlers at top.
	for (int i=0; i<ql.length; i++) {
	    // make label and description string.
	    String l = (labels.containsKey(ql[i])) ?
		labels.get(ql[i]).toString()+":" : "";
	    String s = ql[i].toString();

	    // determine if HandlerSet has changed & print if necessary.
	    HandlerSet oldHS = hs; hs = handlers(qM, ql[i]);
	    if (!HandlerSet.equals(oldHS, hs)) { // changed, print update.
		StringBuffer sb=new StringBuffer("-- new handlers [");
		for (HandlerSet hsp=hs; hsp!=null; hsp=hsp.next) {
		    sb.append(labels.get(hsp.h));
		    if (hsp.next!=null) sb.append(", ");
		}
		sb.append("] --");
		indent(pw, null, sb.toString());
	    }
	    // printBefore callback.
	    callback.printBefore(pw, ql[i]);
	    // Add footer tag to HEADER quads.
	    if (ql[i] instanceof HEADER)
		s += " [footer at "+labels.get(ql[i].next(0))+"]";
	    // Print CALL, CJMP, SWITCH, TYPESWITCH & PHI specially.
	    if (ql[i] instanceof CJMP) {
		CJMP Q = (CJMP) ql[i];
		indent(pw, ql[i], l, 
		       "CJMP " + Q.test() + ", " + labels.get(Q.next(1)));
		indent(pw, Q);
	    } else if (ql[i] instanceof SWITCH) {
		SWITCH Q = (SWITCH) ql[i];
		indent(pw, ql[i], l, "SWITCH "+Q.index());
		for (int j=0; j<Q.keysLength(); j++)
		    indent(pw, null, "  "+
			   "case "+Q.keys(j)+": " +
			   "goto "+labels.get(Q.next(j)));
		indent(pw, null, "  "+
		       "default: goto "+labels.get(Q.next(Q.keysLength())));
		indent(pw, Q);
	    } else if (ql[i] instanceof TYPESWITCH) {
		TYPESWITCH Q = (TYPESWITCH) ql[i];
		indent(pw, ql[i], l, "TYPESWITCH "+Q.index());
		for (int j=0; j<Q.keysLength(); j++)
		    indent(pw, null, "  "+
			   "case "+Q.keys(j)+": " +
			   "goto "+labels.get(Q.next(j)));
		if (Q.hasDefault())
		    indent(pw, null, "  "+
			   "default: goto "+labels.get(Q.next(Q.keysLength())));
		indent(pw, Q);
	    } else if (ql[i] instanceof PHI) {
		PHI Q = (PHI) ql[i];
		if (Q instanceof LABEL) { // ADD a label tag.
		    indent(pw, null, l, "LABEL "+((LABEL)Q).label());
		    l="";
		}
		StringBuffer sb=new StringBuffer("PHI[");
		for (int j=0; j<Q.arity(); j++) {
		    sb.append(labels.get(Q.prev(j)));
		    if (j<Q.arity()-1) sb.append(", ");
		}
		sb.append("]");
		indent(pw, ql[i], l, sb.toString());
		indent(pw, Q);
	    } else if (ql[i] instanceof CALL || ql[i] instanceof PCALL) {
		SIGMA Q = (SIGMA) ql[i];
		// reformat stuff after 'exceptions'
		int j = s.indexOf(" exceptions ");
		assert j>=0 : "(P)CALL.toString() changed, oops.";
		indent(pw, Q, l, s.substring(0, j));
		Temp retex = (Q instanceof CALL)
		    ? ((CALL)Q).retex() : ((PCALL)Q).retex();
		if (retex!=null) // suppress exc info if not applicable
		    indent(pw, null, null, " exception in "+retex+"; "+
			   "handler at "+labels.get(Q.next(1)));
		indent(pw, Q); // print sigma functions.
	    } else if (ql[i] instanceof METHOD) {
		indent(pw, ql[i], l, s);
		StringBuffer sb = new StringBuffer();
		int n = ql[i].next().length;
		for (int j=1; j < n; j++) {
		    sb.append(labels.get(ql[i].next(j)));
		    if (j < n-1) sb.append(", ");
		}
		if (n>1)
		    indent(pw, null, null, "  handlers at ["+sb+"]");
	    } else indent(pw, ql[i], l, s);

	    // DEFAULT branch for HEADER is 1-edge. For all others, 0-edge.
	    int j = (ql[i] instanceof HEADER)?1:0;
	    if (i<ql.length-1 && ql[i].next(j) != ql[i+1]) {
		if (ql[i].next(j) instanceof FOOTER)
		    indent(pw, null,
			   "[footer at "+labels.get(ql[i].next(j))+"]");
		else
		    indent(pw, null, "goto "+labels.get(ql[i].next(j)));
	    }
	    // printAfter callback.
	    callback.printAfter(pw, ql[i]);
	}
	pw.println();
	pw.flush();
    }
    /** Pretty-print a PHI. */
    static void indent(PrintWriter pw, PHI p) {
	for (int i=0; i<p.numPhis(); i++) {
	    StringBuffer sb = new StringBuffer();
	    sb.append(p.dst(i));
	    sb.append("=phi(");
	    for (int j=0; j<p.arity(); j++) {
		sb.append(p.src(i, j));
		if (j<p.arity()-1) sb.append(", ");
	    }
	    sb.append(")");
	    indent(pw, null, sb.toString());
	}
    }
    /** Pretty-print a SIGMA. */
    static void indent(PrintWriter pw, SIGMA s) {
	for (int i=0; i<s.numSigmas(); i++) {
	    StringBuffer sb = new StringBuffer("  <");
	    for (int j=0; j<s.arity(); j++) {
		sb.append(s.dst(i, j));
		if (j<s.arity()-1) sb.append(", ");
	    }
	    sb.append(">=sigma(");
	    sb.append(s.src(i));
	    sb.append(")");
	    indent(pw, null, sb.toString());
	}
    }
    
    ////////////////////////////////////////////////////////////
    /** Pretty-print a string at the proper indentation. */
    static void indent(PrintWriter pw, HCodeElement src, String s) {
	indent(pw, src, null, s);
    }
    /** Pretty-print a string and an optional label at the proper 
     *  indentation.*/
    static void indent(PrintWriter pw, HCodeElement src, String l, String s) {
	StringBuffer sb = new StringBuffer();
	if (src!=null) {
	    sb.append(":" + src.getLineNumber());
	    String sf = src.getSourceFile();
	    int n = fieldW1 - sb.length();
	    n = (n<0)?0:(n>sf.length())?sf.length():n;
	    sb.insert(0, sf.substring(0,n));
	}
	while (sb.length() <= fieldW1) sb.append(' ');

	if (l!=null) sb.append(l);
	while (sb.length() < (1+fieldW1+fieldW2)) sb.append(' ');
	if (s!=null) sb.append(s);

	pw.println(sb.toString());
    }
    /** Width of the first (source file/line number) output field. */
    private static final int fieldW1 = 8;
    /** Width of the second (label) output field. */
    private static final int fieldW2 = 5;

    ///////// Handler Set utility functions. //////
    private static final HandlerSet handlers(final METHOD m, final Quad q) {
	HandlerSet hs = null;
	final Quad[] ql = m.next();
	for (int i=ql.length-1; i > 0; i--) // element 0 is not a HANDLER
	    if (((HANDLER)ql[i]).isProtected(q))
		hs = new HandlerSet((HANDLER)ql[i], hs);
	return hs;
    }

    ///////// Inner classes ///////////////////
    /** Label class. */
    private static final class Label {
	int num = 0;
	public void renumber(int n) { num = n; }
	public String toString() { return "L"+num; }
    }
}
