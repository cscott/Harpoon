// Instr.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.HasEdges;
import harpoon.IR.Properties.UseDef;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.ArrayFactory;
import harpoon.Util.Util;

import java.util.*;

/**
 * <code>Instr</code> is the primary class for representing
 * assembly-level instructions used in the Backend.* packages.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Instr.java,v 1.1.2.16 1999-06-14 07:12:07 pnkfelix Exp $
 */
public class Instr implements HCodeElement, UseDef, HasEdges {
    private String assem;
    private InstrFactory inf;

    /* temporarily private for felix. should modify code to use
     * TempMaps and accessor functions (use/def) to avoid modifying 
     * these. */
    public Temp[] dst;
    public Temp[] src;

    private int hashCode;

    // for implementing HCodeElement
    private String source_file;
    private int source_line;
    private int id;

    // for implementing HasEdges
    // ADB: the use of vector is intended to be temporary, to be
    //      replaced by a more specific, efficient data type if necessary.

    // contains Instrs which have Edges to this
    private Vector pred;
    // contains Instrs which this has Edges to
    private Vector succ;

    /** Defines an array factory which can be used to generate
     *  arrays of <code>Instr</code>s. */
    public static final ArrayFactory arrayFactory =
        new ArrayFactory() {
            public Object[] newArray(int len) { return new Instr[len]; }
        };

    // *************** CONSTRUCTORS *****************

    /** Creates an <code>Instr</code> consisting of the String 
     *  assem and the lists of destinations and sources in dst and src. */
    public Instr(InstrFactory inf, HCodeElement source, 
          String assem, Temp[] dst, Temp[] src) {
        Util.assert(inf != null);
        Util.assert(assem != null);
	// Util.assert(dst!=null && src!=null, "DST and SRC should not = null");
	if (src == null) src = new Temp[0];
	if (dst == null) dst = new Temp[0];
	
        this.source_file = (source != null)?source.getSourceFile():"unknown";
        this.id = inf.getUniqueID();
        this.inf = inf;
        this.assem = assem; this.dst = dst; this.src = src;

	this.pred = new Vector();
	this.succ = new Vector();
	
	this.hashCode = (id<<5) + inf.getParent().getName().hashCode() ^
            inf.getMethod().hashCode();
    }

    /** Creates an <code>Instr</code> consisting of the String assem
     *  and the list of sources in src. The list of destinations is
     *  empty. */
    public Instr(InstrFactory inf, HCodeElement source,
          String assem, Temp[] src) {
        this(inf, source, assem, null, src);
    }

    /** Creates an <code>Instr</code> consisting of the String assem.
     *  The lists of sources and destinations are empty. */
    public Instr(InstrFactory inf, HCodeElement source, String assem) {
        this(inf, source, assem, null, null);
    }

    // ********* INSTR METHODS ********

    /** Creates an <code>Edge</code> which connects <code>src</code>
     *  to <code>dest</code> */
    public static Edge addEdge(Instr src, Instr dest) {
	Util.assert(src != null);
	Util.assert(dest != null);

	Edge e = new Edge(src, dest);

	src.succ.addElement(e);
	dest.pred.addElement(e);

	return e;
    }

    /** Removes an <code>Edge</code> which previously connected <code>src</code>
     *  to <code>dest</code> */
    public static void removeEdge(Instr src, Instr dest) {
	Util.assert(src != null);
	Util.assert(dest != null);

	/* ADB: dangerous way of doing this, should be done better in
         * future. XXX */
	Enumeration enum = src.succ.elements();
	while (enum.hasMoreElements()) {
	    HCodeEdge hce = (HCodeEdge)enum.nextElement();	
            if (hce.to() == dest) {
		src.succ.removeElement(hce);
            }
        }
	enum = dest.pred.elements();
        while (enum.hasMoreElements()) {
	    HCodeEdge hce = (HCodeEdge)enum.nextElement();
            if (hce.from() == src) {
		dest.pred.removeElement(hce);
            }
        }
    }

    /** Replaces <code>oldi</code> in the Instruction Stream with
	<code>newis/code>.  Don't know if this is the best way of
	doing this, since it simply makes all the edges of the old
	instr become the edges of the instruction list.  In any case,
	both oldi and newi are modified: oldi loses its Edges and newi
	loses its original Edges but gets oldi's old Edges 
    */
    public static void replaceInstrList(Instr oldi, List newis) {
	// TODO
    }

    /** Replaces <code>oldi</code> in the Instruction Stream with
	<code>newi</code>.   Don't know if this is the best way of
	doing this, since it simply makes all the edges of the old
	instr become the edges of the new instr.  In any case, both
	oldi and newi are modified: oldi loses its Edges and newi
	loses its original Edges but gets oldi's old Edges
     */
    public static void replaceInstr(Instr oldi, Instr newi) {
	Util.assert(oldi != null && newi != null, "Null Arguments are bad");
	
	newi.pred = new Vector();
	for(int i=0; i<oldi.pred.size(); i++) {
	    Edge e = (Edge) oldi.pred.get(i);
	    addEdge( e.from, newi );
	}
	for(int i=0; i<newi.pred.size(); i++) {
	    Edge e = (Edge) newi.pred.get(i);
	    removeEdge( e.from, oldi );
	}
	
	newi.succ = new Vector();
	for(int i=0; i<oldi.succ.size(); i++) {
	    Edge e = (Edge) oldi.succ.get(i);
	    addEdge( newi, e.to );
	}
	for(int i=0; i<newi.succ.size(); i++) {
	    Edge e = (Edge) newi.succ.get(i);
	    removeEdge( oldi, e.to );
	}
    }

    /** Inserts <code>newi</code> as an Instr after <code>pre</code>, such
     *  that <code>pre</code>'s successors become <code>newi</code>'s
     *  successors, and <code>pre</code>'s only successor is <code>newi</code>.
     */
    public static void insertInstrAfter(Instr pre, Instr newi) {
	Util.assert(pre != null);
        Util.assert(newi != null);

        HCodeEdge[] oldsucc = pre.succ();
	for (int i = 0; i < oldsucc.length; i++) {
	    removeEdge(pre, (Instr)oldsucc[i].to());
	    addEdge(newi, (Instr)oldsucc[i].to());
        }
	pre.succ = new Vector();
	addEdge(pre, newi);
    }

    /** Inserts <code>newi</code> as an Instr before <code>post</code>, such
     *  that <code>post</code>'s predecessors become <code>newi</code>'s
     *  predecessors, and <code>post</code>'s only predecessor is
     *  <code>newi</code>. */
    public static void insertInstrBefore(Instr post, Instr newi) {
        Util.assert(post != null);
        Util.assert(newi != null);

	HCodeEdge[] oldpred = post.pred();
	for (int i = 0; i < oldpred.length; i++) {
            removeEdge((Instr)oldpred[i].from(), post);
	    addEdge((Instr)oldpred[i].from(), newi);
	}
	post.pred = new Vector();
	addEdge(newi, post);
    }

    /** Accept a visitor. */
    public void visit(InstrVisitor v) { v.visit(this); }

    /** Returns the <code>InstrFactory</code> that generated this. */
    public InstrFactory getFactory() { return inf; }
    // shouldn't this return inf.clone()???????

    /** Returns the hashcode for this. */
    public int hashCode() { return hashCode; }

    // ********* INTERFACE IMPLEMENTATIONS and SUPERCLASS OVERRIDES

    // ******************** Object overrides
 
    public String toString() {
        StringBuffer s = new StringBuffer();
        int len = assem.length();
        for (int i = 0; i < len; i++) 
            if (assem.charAt(i) == '`')
                switch (assem.charAt(++i)) {
		case 'd': { // FSK changed s -> d
		    int n = Character.digit(assem.charAt(++i), 10);
		    Util.assert(n < dst.length, "Instr can't parse " + assem);
		    s.append(dst[n]);
		}
		break;
		case 's': { // FSK changed d -> s
		    int n = Character.digit(assem.charAt(++i), 10);
		    Util.assert(n < src.length, "Instr can't parse " + assem);
		    s.append(src[n]);
		}
		break;
		case 'j': {
		    int n = Character.digit(assem.charAt(++i), 10);
		    Util.assert(n < src.length, "Instr can't parse " + assem);
		    s.append(src[n]);
		}
		break;
		case '`': 
		    s.append('`');
		    break;
                }
            else s.append(assem.charAt(i));

        return s.toString();
    }

    // ******************** UseDef Interface

    /** Returns the <code>Temp</code>s used by this <code>Instr</code>. */
    public Temp[] use() { 
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, src); 
    }

    /** Returns the <code>Temp</code>s defined by this <code>Instr</code>. */
    public Temp[] def() { 
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, dst);
    }

    // ******************* HCodeElement interface

    public String getSourceFile() { return source_file; }

    public int getLineNumber() { return source_line; }

    public int getID() { return id; }

    // ******************** HasEdges interface

    public HCodeEdge[] edges() { 
	HCodeEdge[] p = (HCodeEdge[]) pred.toArray();
	HCodeEdge[] s = (HCodeEdge[]) succ.toArray();
	HCodeEdge[] e = new HCodeEdge[p.length + s.length];
	System.arraycopy(p, 0, e, 0, p.length);
	System.arraycopy(s, 0, e, p.length, s.length);
	return e;
    }

    public HCodeEdge[] pred() { 
	Object[] predarr = pred.toArray();
	HCodeEdge[] edges = new HCodeEdge[predarr.length];
	for (int i = 0; i < predarr.length; i++)
	    edges[i] = (HCodeEdge) predarr[i];
	return edges;
    }

    public HCodeEdge[] succ() { 
	Object[] succarr = succ.toArray();
	HCodeEdge[] edges = new HCodeEdge[succarr.length];
	for (int i = 0; i < succarr.length; i++)
	    edges[i] = (HCodeEdge) succarr[i];
        return edges;
    }
}
