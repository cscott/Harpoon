// LeafConst.java, created Thu Aug  6 04:32:18 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Util.Util;
/**
 * <code>LeafConst</code> objects represent constant values.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LeafConst.java,v 1.1 1998-08-07 09:56:38 cananian Exp $
 */

public class LeafConst extends Leaf {
    public Object value;
    public HClass type;
    /** Creates a <code>LeafConst</code> from an object value and
     *  its class type. */
    public LeafConst(Object value, HClass type) {
        this.value = value; this.type = type;
    }
    public String toString() {
	if (type==HClass.forName("java.lang.String"))
	    return "(String)\""+Util.escape(value.toString())+"\"";
	return "("+type.getName()+")"+value.toString();
    }
}
