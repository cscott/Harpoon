// HArrayMethod.java, created Sun Feb  7 15:40:47 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * <code>HArrayMethod</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HArrayMethod.java,v 1.5.2.1 1999-02-07 21:20:32 cananian Exp $
 */
public class HArrayMethod extends HMethod {
    
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
