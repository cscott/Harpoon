// Print.java, created Thu Dec 17 17:31:26 1998 by cananian
package harpoon.IR.Quads;

import harpoon.ClassFile.*;

import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * The <code>Print</code> class pretty-prints a quad representation,
 * inserting labels to make the control flow clear.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Print.java,v 1.1.2.1 1998-12-18 04:49:59 cananian Exp $
 */
abstract class Print  {
    /** Print <code>Quad</code> code representation <code>c</code> to
     *  <code>PrintWriter</code> <code>pw</code>. */
    final static void print(PrintWriter pw, Code c) {
	// get elements.
	Quad[] ql = (Quad[]) c.getElements();
	// compile list of back edges
	Hashtable labels = new Hashtable();
	for (int i=0; i<ql.length; i++) {
	    // if has more than one edge, then other edges branch to labels.
	    for (int j=0; j<ql[i].next().length; j++) {
		if (j!=0 || i==ql.length-1 || ql[i].next(j)!=ql[i+1] ||
		    ql[i] instanceof SWITCH)
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
		((Label)labels.get(ql[i])).renumber(j++);

	// okay, print these pookies.
	pw.println("Codeview \""+c.getName()+"\" for "+c.getMethod()+":");
	for (int i=0; i<ql.length; i++) {
	    String l = (labels.containsKey(ql[i])) ?
		labels.get(ql[i]).toString()+":" : "";
	    String s = ql[i].toString();
	    
	    // Add footer tag to HEADER quads.
	    if (ql[i] instanceof HEADER)
		s += " [footer at "+labels.get(ql[i].next(0))+"]";
	    // Print CJMP, SWITCH & PHI specially.
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
	    } else indent(pw, ql[i], l, s);

	    // DEFAULT branch for HEADER is 1-edge. For all others, 0-edge.
	    int j = (ql[i] instanceof HEADER)?1:0;
	    if (i<ql.length-1 && ql[i].next(j) != ql[i+1]) {
		if (ql[i].next(0) instanceof FOOTER)
		    indent(pw, null,
			   "[footer at "+labels.get(ql[i].next(0))+"]");
		else
		    indent(pw, null, "goto "+labels.get(ql[i].next(0)));
	    }
	}
	pw.println();
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
	indent(pw, src, "", s);
    }
    /** Pretty-print a string and an optional label at the proper 
     *  indentation.*/
    static void indent(PrintWriter pw, HCodeElement src, String l, String s) {
	StringBuffer rc;
	if (src!=null) {
	    rc = new StringBuffer(":" + src.getLineNumber());
	    rc.insert(0, src.getSourceFile().substring(0, 8-rc.length()));
	} else rc = new StringBuffer("       ");

	StringBuffer sb = new StringBuffer(l);
	while (sb.length() < 5) sb.append(' ');
	sb.append(s);
	rc.append(' '); rc.append(sb);
	pw.println(rc.toString());
    }

    // Label class.
    static class Label {
	int num = 0;
	public void renumber(int n) { num = n; }
	public String toString() { return "L"+num; }
    }
}
