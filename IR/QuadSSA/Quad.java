package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>Quad</code> is the base class for the quadruple representation.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Quad.java,v 1.3 1998-08-07 13:38:13 cananian Exp $
 */
public abstract class Quad implements HCodeElement {
    String sourcefile;
    int linenumber;
    int id;
    /** Constructor. */
    protected Quad(String sourcefile, int linenumber) {
	this.sourcefile = sourcefile;
	this.linenumber = linenumber;
	synchronized(lock) {
	    this.id = next_id++;
	}
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
}
