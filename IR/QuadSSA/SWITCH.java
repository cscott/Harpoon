// SWITCH.java, created Wed Aug 26 20:45:24 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

/**
 * <code>SWITCH</code> represents a switch construct.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SWITCH.java,v 1.11 1998-09-18 00:50:29 cananian Exp $
 */

public class SWITCH extends SIGMA {
    /** The discriminant, compared against each value in <code>keys</code>.*/
    public Temp index;
    /** Integer keys for switch cases. <p>
     *  <code>next(n)</code> is the jump target corresponding to
     *  <code>keys[n]</code> for <code>0 <= n < keys.length</code>. <p>
     *  <code>next(keys.length)</code> is the default target. */
    public int keys[];
    /** Creates a <code>SWITCH</code> operation. <p>
     *  <code>next[n]</code> is the jump target corresponding to
     *  <code>keys[n]</code> for <code>0 <= n < keys.length</code>. <p>
     *  <code>next[keys.length]</code> is the default target.
     */
    public SWITCH(HCodeElement source,
		  Temp index, int keys[],
		  Temp dst[][], Temp src[]) {
	super(source, dst, src, keys.length+1 /*multiple targets*/);
	this.index = index;
	this.keys = keys;
    }
    public SWITCH(HCodeElement source, Temp index, int keys[], Temp src[]) {
	this(source, index, keys, new Temp[src.length][keys.length+1], src);
    }

    /** Returns the Temp used by this quad.
     * @return the <code>index</code> field. */
    public Temp[] use() { 
	Temp[] u = super.use();
	Temp[] r = new Temp[u.length+1];
	System.arraycopy(u, 0, r, 0, u.length);
	// add 'index' to end of use array.
	r[u.length] = index;
	return r;
    }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	super.renameUses(tm);
	index = tm.tempMap(index);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	super.renameDefs(tm);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer("SWITCH "+index+": ");
	for (int i=0; i<keys.length; i++)
	    sb.append("case "+keys[i]+" => "+next(i).getID()+"; ");
	sb.append("default => "+next(keys.length).getID());
	sb.append(" / "); sb.append(super.toString());
	return sb.toString();
    }
}
