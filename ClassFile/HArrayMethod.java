// HArrayMethod.java, created Sat Aug  8 03:02:01 1998 by cananian
package harpoon.ClassFile;

import java.lang.reflect.Modifier;
import java.util.Vector;
/**
 * An <code>HArrayMethod</code> represents the 'phantom' methods of
 * array objects.  Arrays inherit most of the methods of 
 * <code>java.lang.Object</code>, but in addition we give them
 * special constructors and access methods, to code for the
 * various array operations.  Specifically, we have 
 * <code>&lt;init&gt;</code> methods to represent <code>arraynew</code>
 * operations, <code>get</code> and <code>set</code> methods to access
 * members of the array, and a <code>public static final int length</code>
 * field to represent the <code>arraylength</code> operation.
 * <p>
 * From outside this package, <code>HArrayMethod</code>s should appear
 * identical to 'real' <code>HMethod</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HArrayMethod.java,v 1.2 1998-08-08 12:26:43 cananian Exp $
 */

class HArrayMethod extends HMethod {
    String name;
    String descriptor;

    /** Creates a <code>HArrayMethod</code>. */
    HArrayMethod(HClass parent, String name, String descriptor) {
	super(parent);
	this.name = name;
	this.descriptor = descriptor;
    }
    /** No code for array methods. */
    public HCode getCode(String codetype) { return null; }
    /** Attempts to putCode are illegal! */
    public void putCode(HCode codeobj) {
	throw new Error("Cannot putCode an array method.");
    }
    /** Returns the name of this array method. */
    public String getName() { return name; }
    /** Returns the java language modifiers for this array method.
     *  @return Modifier.PUBLIC */
    public int getModifiers() { return Modifier.PUBLIC; }
    /**
     * Returns a <code>HClass</code> object that represents the formal
     * return type of the method represented by this <code>HMethod</code>
     * object.
     */
    public HClass getReturnType() {
	if (returnType==null) {
	    // extract just the return value descriptor.
	    String desc = descriptor.substring(descriptor.lastIndexOf(')')+1);
	    returnType = HClass.forDescriptor(desc);
	}
	return returnType;
    }
    /** Cached value of <code>getReturnType</code>. */
    private HClass returnType=null;

    /** Returns the descriptor for this method. */
    public String getDescriptor() {
	return descriptor;
    }

    /**
     * Returns an array of <code>HClass</code> objects that represent the
     * formal parameter types, in declaration order, of the method
     * represented by this object.  Returns an array
     * of length 0 is the underlying method takes no parameters.
     */
    public HClass[] getParameterTypes() {
	if (parameterTypes==null) {
	    // parse method descriptor, stripping parens and retval desc.
	    String desc = descriptor.substring(1, descriptor.lastIndexOf(')'));
	    Vector v = new Vector();
	    for (int i=0; i<desc.length(); i++) {
		// make HClass for first param in list.
		v.addElement(HClass.forDescriptor(desc.substring(i)));
		// skip over the one we just added.
		while (desc.charAt(i)=='[') i++;
		if (desc.charAt(i)=='L') i=desc.indexOf(';', i);
	    }
	    parameterTypes = new HClass[v.size()];
	    v.copyInto(parameterTypes);
	}
	return HClass.copy(parameterTypes);
    }
    /** Cached value of <code>getParameterTypes</code>. */
    private HClass[] parameterTypes = null;
    
    /**
     * Returns an array of <code>String</code> objects giving the declared
     * names of the formal parameters of the method.  The length of the
     * returned array is equal to the number of formal parameters.
     * There are no <code>LocalVariableTable</code> attributes available
     * for array method, so every element of the returned array will be
     * <code>null</code>.
     */
    public String[] getParameterNames() {
	return new String[getParameterTypes().length];
    }
}
