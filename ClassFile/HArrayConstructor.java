// HArrayConstructor.java, created Sat Aug  8 09:48:07 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;
import java.util.Vector;
/**
 * An <code>HArrayConstructor</code> represents the 'phantom' constructors
 * of array objects.  From outside this package, 
 * <code>HArrayConstructor</code>s should appear identical to 'real'
 * <Code>HConstructor</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HArrayConstructor.java,v 1.2 1998-10-11 02:37:08 cananian Exp $
 * @see HArrayMethod
 * @see HArrayField
 */

class HArrayConstructor extends HConstructor {
    String descriptor;

    /** Creates an <code>HArrayConstructor</code>. */
    HArrayConstructor(HClass parent, String descriptor) {
        super(parent);
	this.descriptor = descriptor;
    }
     /** No code for array constructors. */
    public HCode getCode(String codetype) { return null; }
    /** Attempts to putCode are illegal. */
    public void putCode(HCode codeobj) {
	throw new Error("Cannot putCode an array method.");
    }
    /** Returns the java language modifiers for this array constructor.
     *  @return Modifier.PUBLIC */
    public int getModifiers() { return Modifier.PUBLIC | Modifier.NATIVE; }
    /**
     * Returns a <code>HClass</code> object that represents the formal
     * return type of the method represented by this <code>HMethod</code>
     * object.
     */
    public HClass getReturnType() { return HClass.Void; }
    /** Returns the descriptor for this method. */
    public String getDescriptor() { return descriptor; }

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
     * for array constructors, so every element of the returned array will be
     * <code>null</code>.
     */
    public String[] getParameterNames() {
	return new String[getParameterTypes().length];
    }
}
