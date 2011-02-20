// HArrayMethod.java, created Sun Feb  7 15:40:47 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * <code>HArrayMethod</code> provides information about a 'phantom' method
 * of an array class.  From outside this package, <code>HArrayMethod</code>s
 * should appear identical to 'real' <code>HMethod</code>s.
 * <p> Used for the <code>clone()</code> method of an array object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HArrayMethod.java,v 1.6 2002-02-25 21:03:02 cananian Exp $
 * @see HArrayField
 * @see HArrayConstructor
 */
class HArrayMethod extends HMethodImpl {
    
    /** Creates a <code>HArrayMethod</code>. */
    public HArrayMethod(HClass parent, String name, int modifiers,
			HClass returnType, HClass[] parameterTypes,
			String[] parameterNames, HClass[] exceptionTypes,
			boolean isSynthetic) {
        this.parent = parent;
	this.name = name;
	this.modifiers = modifiers;
	this.returnType = returnType;
	this.parameterTypes = parameterTypes;
	this.parameterNames = parameterNames;
	this.exceptionTypes = exceptionTypes;
	this.isSynthetic = isSynthetic;
    }
    
}
