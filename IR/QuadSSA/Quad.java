package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Util.Util;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
/**
 * <code>Quad</code> is the base class for the quadruple representation.<p>
 * No <code>Quad</code>s throw exceptions implicitly.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Quad.java,v 1.17 1998-09-13 23:57:29 cananian Exp $
 */
public abstract class Quad 
    implements harpoon.ClassFile.HCodeElement, 
               harpoon.IR.Properties.UseDef, harpoon.IR.Properties.Edges
{
    String sourcefile;
    int linenumber;
    int id;
    /** Constructor. */
    protected Quad(HCodeElement source,
		   int prev_arity, int next_arity) {
	this.sourcefile = source.getSourceFile();
	this.linenumber = source.getLineNumber();
	synchronized(lock) {
	    this.id = next_id++;
	}
	this.prev = new Edge[prev_arity];
	this.next = new Edge[next_arity];
    }
    protected Quad(HCodeElement source) {
	this(source, 1, 1);
    }

    static int next_id = 0;
    static final Object lock = new Object();

    /** Returns the original source file name that this <code>Quad</code>
     *  is derived from. */
    public String getSourceFile() { return sourcefile; }
    /** Returns the line in the original source file that this 
     *  <code>Quad</code> is derived from. */
    public int getLineNumber() { return linenumber; }
    /** Returns a unique numeric identifier for this <code>Quad</code>. */
    public int getID() { return id; }
    /** Force everyone to reimplement toString() */
    public abstract String toString();

    /** Accept a visitor. */
    public abstract void visit(QuadVisitor v);

    /** Rename all variables in a Quad according to a mapping. */
    public abstract void rename(TempMap tm);

    /*----------------------------------------------------------*/
    /** Return all the Temps used by this Quad. */
    public Temp[] use() { return new Temp[0]; }
    /** Return all the Temps defined by this Quad. */
    public Temp[] def() { return new Temp[0]; }

    /*----------------------------------------------------------*/
    // Graph structure.
    // Can modify links, but not *number of links*.
    Edge next[], prev[];

    /** Returns the <code>i</code>th successor of this quad. */
    public Quad next(int i) { return (Quad) next[i].to(); }
    /** Returns the <code>i</code>th predecessor of this quad. */
    public Quad prev(int i) { return (Quad) prev[i].from(); }

    /** Returns an array containing all the successors of this quad,
     *  in order. */
    public Quad[] next() { 
	Quad[] r = new Quad[next.length];
	for (int i=0; i<r.length; i++)
	    r[i] = (next[i]==null)?null:(Quad)next[i].to();
	return r;
    }
    /** Returns an array containing all the predecessors of this quad,
     *  in order. */
    public Quad[] prev() {
	Quad[] r = new Quad[prev.length];
	for (int i=0; i<r.length; i++)
	    r[i] = (prev[i]==null)?null:(Quad)prev[i].from();
	return r;
    }
    
    /** Returns an array containing all the outgoing edges from this quad. */
    public Edge[] nextEdge() { return (Edge[]) Util.copy(next); }
    /** Returns an array containing all the incoming edges of this quad. */
    public Edge[] prevEdge() { return (Edge[]) Util.copy(prev); }
    /** Returns the <code>i</code>th outgoing edge for this quad. */
    public Edge nextEdge(int i) { return next[i]; }
    /** Returns the <code>i</code>th incoming edge of this quad. */
    public Edge prevEdge(int i) { return prev[i]; }

    /** Returns an array with all the edges to and from this 
     *  <code>Quad</code>. */
    public HCodeEdge[] edges() {
	Edge[] e = new Edge[next.length+prev.length];
	System.arraycopy(next,0,e,0,next.length);
	System.arraycopy(prev,0,e,next.length,prev.length);
	return (HCodeEdge[]) e;
    }

    /** Adds an edge between two Quads.  The <code>from_index</code>ed
     *  outgoing edge of <code>from</code> is connected to the 
     *  <code>to_index</code>ed incoming edge of <code>to</code>. */
    public static void addEdge(Quad from, int from_index,
			       Quad to, int to_index) {
	Edge e = new Edge(from, from_index, to, to_index);
	from.next[from_index] = e;
	to.prev[to_index] = e;
    }
    /** Adds an edge between two Quads.  <code>from</code> must have
     *  exactly one successor, and <code>to</code> must require exactly
     *  one predecessor. Equivalent to <code>addEdge(from, 0, to, 0)</code>.
     */
    public static void addEdge(Quad from, Quad to) {
	Util.assert(from.next.length==1);
	Util.assert(to.prev.length==1);
	addEdge(from, 0, to, 0);
    }
}
