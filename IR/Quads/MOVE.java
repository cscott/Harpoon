// MOVE.java, created Wed Aug  5 06:53:38 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>MOVE</code> objects represent an assignment to a compiler temporary.
 * The source of the assignment must be another temporary.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MOVE.java,v 1.3.2.1 2002-02-27 08:36:32 cananian Exp $
 */
public class MOVE extends Quad {
    /** The destination <code>Temp</code>. */
    protected Temp dst;
    /** The source <code>Temp</code>. */
    protected Temp src;
    
    /** Creates a <code>MOVE</code> from a source and destination 
     *  <code>Temp</code>.
     * @param dst the destination <code>Temp</code>.
     * @param src the source <code>Temp</code>.
     */
    public MOVE(QuadFactory qf, HCodeElement source,
	       Temp dst, Temp src) {
	super(qf, source);
	this.dst = dst; this.src = src;
	assert dst!=null && src!=null;
    }
    // ACCESSOR METHODS:
    /** Returns the destination <code>Temp</code>. */
    public Temp dst() { return dst; }
    /** Returns the source <code>Temp</code>. */
    public Temp src() { return src; }
    
    /** Returns the <code>Temp</code>s used by this <code>Quad</code>. */
    public Temp[] use() { return new Temp[] { src }; }
    /** Returns the <code>Temp</code>s defined by this <code>Quad</code>. */
    public Temp[] def() { return new Temp[] { dst }; }

    public int kind() { return QuadKind.MOVE; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new MOVE(qqf, this, map(defMap, dst), map(useMap, src));
    }
    /** Rename all defined variables in this <code>Quad</code> according 
     *  to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }
    /** Rename all used variables in this <code>Quad</code> according 
     *  to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	src = tm.tempMap(src);
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this <code>Quad</code>. */
    public String toString() { 
	return dst.toString() + " = MOVE " + src.toString();
    }
}
