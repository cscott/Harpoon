// SWITCH.java, created Wed Aug 26 20:45:24 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;

/**
 * <code>SWITCH</code> represents a switch construct.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SWITCH.java,v 1.1 1998-08-27 00:59:05 cananian Exp $
 */

public class SWITCH extends Quad {
    /** The discriminant. */
    public Temp index;
    /** Integer keys for the cases of the switch. */
    public int keys[];
    /** Creates a <code>SWITCH</code> operation. <p>
     *  <code>next[n]</code> is the jump target corresponding to
     *  <code>key[n]</code>.
     *  <code>next[key.length]</code> is the default target.
     */
    public SWITCH(String sourcefile, int linenumber,
		  Temp index, int keys[]) {
	super(sourcefile, linenumber, 1, keys.length+1 /*multiple targets*/);
	this.index = index;
	this.keys = keys;
    }
    SWITCH(HCodeElement hce, Temp index, int keys[]) {
	this(hce.getSourceFile(), hce.getLineNumber(), index, keys);
    }
    /** Returns the Temp used by this quad.
     * @return the <code>index</code> field. */
    public Temp[] use() { return new Temp[] { index }; }
    /** Returns human-readable representation of this quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer("SWITCH "+index+": ");
	for (int i=0; i<keys.length; i++)
	    sb.append("case "+keys[i]+" => "+next[i].getID()+"; ");
	sb.append("default => "+next[keys.length].getID());
	return sb.toString();
    }
}
