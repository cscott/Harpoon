// SWITCH.java, created Wed Aug 26 20:45:24 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;

/**
 * <code>SWITCH</code> represents a switch construct.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SWITCH.java,v 1.5 1998-09-11 17:13:57 cananian Exp $
 */

public class SWITCH extends Quad {
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
		  Temp index, int keys[]) {
	super(source, 1, keys.length+1 /*multiple targets*/);
	this.index = index;
	this.keys = keys;
    }

    /** Returns the Temp used by this quad.
     * @return the <code>index</code> field. */
    public Temp[] use() { return new Temp[] { index }; }

    public void accept(Visitor v) { v.visit(this); }

    /** Returns human-readable representation of this quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer("SWITCH "+index+": ");
	for (int i=0; i<keys.length; i++)
	    sb.append("case "+keys[i]+" => "+next(i).getID()+"; ");
	sb.append("default => "+next(keys.length).getID());
	return sb.toString();
    }
}
