package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>Quad</code> is the base class for the quadruple representation.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Quad.java,v 1.4 1998-08-08 00:43:22 cananian Exp $
 */
public abstract class Quad implements HCodeElement,QuadList {
    String sourcefile;
    int linenumber;
    int id;
    /** Constructor. */
    protected Quad(String sourcefile, int linenumber,
		   int prev_arity, int next_arity) {
	this.sourcefile = sourcefile;
	this.linenumber = linenumber;
	synchronized(lock) {
	    this.id = next_id++;
	}
	this.prev = new Quad[prev_arity];
	this.next = new Quad[next_arity];
    }
    protected Quad(String sourcefile, int linenumber) {
	this(sourcefile, linenumber, 1, 1);
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

    /*----------------------------------------------------------*/
    // Graph structure.
    // Can modify links, but not *number of links*.
    public Quad[] next() { return next; }
    public Quad[] prev() { return prev; }
    Quad next[], prev[];
    
    /*
    public void append(Quad q) {
	Util.assert(next.length==1 && q.prev.length==1);
	next[0]=q; q.prev[0]=this;
    }
    */
    public static void addEdge(Quad from, int from_index,
			       Quad to, int to_index) {
	from.next[from_index] = to;
	to.prev[to_index] = from;
    }
}






