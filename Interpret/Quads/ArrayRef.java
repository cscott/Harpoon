// ArrayRef.java, created Mon Dec 28 03:02:39 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.Util.Util;

/**
 * <code>ArrayRef</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayRef.java,v 1.1.2.3 1999-01-22 23:53:18 cananian Exp $
 */
final class ArrayRef extends ObjectRef {
    final Object[] elements;
    ArrayRef(StaticState ss, HClass type, int dims[]) 
	throws InterpretedThrowable {
	super(ss, type);
	Util.assert(dims.length >= 1 && dims[0]>=0);
	this.elements = new Object[dims[0]];
	if (dims.length==1)
	    for (int i=0; i<this.elements.length; i++)
		this.elements[i] = defaultValue(type.getComponentType());
	else {
	    int ndims[] = new int[dims.length-1];
	    System.arraycopy(dims, 1, ndims, 0, ndims.length);
	    for (int i=0; i<this.elements.length; i++)
		this.elements[i] =
		    new ArrayRef(ss, type.getComponentType(), ndims);
	}
    }
    // private constructor for use by clone() method
    private ArrayRef(StaticState ss, HClass type, Object[] elements) {
        super(ss, type);
        this.elements = elements;
        Util.assert(elements!=null);
    }
    public Object clone() { // arrays can always be cloned.
       return new ArrayRef(ss, type, (Object[]) elements.clone());
    }
    int length() { return this.elements.length; }
    Object get(int i) { return this.elements[i]; }
    void update(int i, Object value) { this.elements[i] = value; }
}
