// DEBUG.java, created Sat Nov 21 21:19:08 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>DEBUG</code> prints a debugging string to standard error.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DEBUG.java,v 1.1.2.3 1998-12-11 22:21:04 cananian Exp $
 */
public class DEBUG extends Quad {
    /** The debugging string. */
    final protected String str;

    /** Creates a <code>DEBUG</code> object. <code>DEBUG</code> prints a
     *  debugging string to standard error.
     * @param str
     *        the debugging string to print.
     */
    public DEBUG(HCodeElement source, String str) {
        super(source);
	this.str = str;
	// VERIFY legality of this DEBUG
	Util.assert(str!=null);
    }
    // ACCESSOR METHODS:
    /** Returns the debugging string printed by this quad. */
    public String str() { return str; }

    public int kind() { return QuadKind.DEBUG; }

    public Quad rename(TempMap tm) {
	return new DEBUG(this, str);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable version of the <code>DEBUG</code> quad. */
    public String toString() { return "DEBUG: \""+Util.escape(str)+"\""; }
}
