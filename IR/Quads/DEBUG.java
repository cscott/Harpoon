// DEBUG.java, created Sat Nov 21 21:19:08 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
/**
 * <code>DEBUG</code> prints a debugging string to standard error.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DEBUG.java,v 1.1.2.1 1998-12-01 12:36:41 cananian Exp $
 */

public class DEBUG extends Quad {
    /** The debugging string. */
    public String str;

    /** Creates a <code>DEBUG</code> object. <code>DEBUG</code> prints a
     *  debugging string to standard error. */
    public DEBUG(HCodeElement source, String str) {
        super(source);
	this.str = str;
    }
    public void visit(QuadVisitor v) { v.visit(this); }
    /** Returns a human-readable version of the <code>DEBUG</code> quad. */
    public String toString() { return "DEBUG: "+str; }
}
