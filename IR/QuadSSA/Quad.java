package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Util.Util;
import harpoon.Temp.Temp;
/**
 * <code>Quad</code> is the base class for the quadruple representation.<p>
 * No <code>Quad</code>s throw exceptions implicitly.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Quad.java,v 1.11 1998-09-08 14:38:39 cananian Exp $
 */
public abstract class Quad implements HCodeElement {
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
	this.prev = new Quad[prev_arity];
	this.next = new Quad[next_arity];
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

    /*----------------------------------------------------------*/
    /** Return all the Temps used by this Quad. */
    public Temp[] use() { return new Temp[0]; }
    /** Return all the Temps defined by this Quad. */
    public Temp[] def() { return new Temp[0]; }

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
    public static void addEdge(Quad from, Quad to) {
	Util.assert(from.next.length==1);
	Util.assert(to.prev.length==1);
	addEdge(from, 0, to, 0);
    }
}






