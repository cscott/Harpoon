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
 * @version $Id: HArrayField.java,v 1.3 1998-10-16 06:21:02 cananian Exp $
 * @see HArrayMethod
 * @see HArrayConstructor
 */
class HArrayField extends HField {
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
