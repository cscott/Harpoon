// HArrayField.java, created Sat Aug  8 09:54:55 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;
/**
 * <code>HArrayField</code> provides information about a 'phantom' field
 * of an array class.  From outside this package, <code>HArrayField</code>s
 * should appear identical to 'real' <code>HField</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HArrayField.java,v 1.2 1998-10-11 02:37:08 cananian Exp $
 * @see HArrayMethod
 * @see HArrayConstructor
 */
class HArrayField extends HField {
    String name;
    int access_flags;
    /** Creates a <code>HArrayField</code>. */
    HArrayField(HClass parent, 
		String name, HClass type, int access_flags) {
	super(parent, type);
	this.name = name;
	this.access_flags = access_flags;
    }
    /**
     * Returns the name of the field represented by this 
     * <code>HArrayField</code> object.
     */
    public String getName() { return name; }
    /**
     * Returns the Java language modifiers for the field represented by this
     * <code>HArrayField</code> object, as an integer.  The 
     * <code>Modifier</code> class should be used to decode the modifiers.
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers() { return access_flags; }
    /**
     * Return the type descriptor for this <code>HArrayField</code> object.
     */
    public String getDescriptor() {
	return type.getDescriptor();
    }
    /**
     * Determines whether this <code>HArrayField</code> represents a constant
     * field.
     * @return <code>false</code>
     */
    public boolean isConstant() { return false; }
    /**
     * Determines whether this <code>HField</code> is synthetic.
     * @return <code>false</code>
     */
    public boolean isSynthetic() { return false; }
}
