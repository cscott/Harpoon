// HArrayField.java, created Sat Aug  8 09:54:55 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.lang.reflect.Modifier;
/**
 * <code>HArrayField</code> provides information about a 'phantom' field
 * of an array class.  From outside this package, <code>HArrayField</code>s
 * should appear identical to 'real' <code>HField</code>s.
 * <p> Used for the <code>length()</code> field of an array object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HArrayField.java,v 1.4 2002-02-25 21:03:01 cananian Exp $
 * @see HArrayMethod
 * @see HArrayConstructor
 */
class HArrayField extends HFieldImpl {
    /** Creates a <code>HArrayField</code>. */
    HArrayField(HClass parent, 
		String name, HClass type, int modifiers) {
	this.parent = parent;
	this.type = type;
	this.name = name;
	this.modifiers = modifiers;
	this.constValue = null;
	this.isSynthetic = false;
    }
}
