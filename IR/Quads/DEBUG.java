// DEBUG.java, created Sat Nov 21 21:19:08 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>DEBUG</code> prints a debugging string to standard error.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DEBUG.java,v 1.5 2002-04-11 04:00:34 cananian Exp $
 */
public class DEBUG extends Quad {
    /** The debugging string. */
    final protected String str;

    /** Creates a <code>DEBUG</code> object. <code>DEBUG</code> prints a
     *  debugging string to standard error.
     * @param str
     *        the debugging string to print.
     */
    public DEBUG(QuadFactory qf, HCodeElement source,
		 String str) {
        super(qf, source);
	this.str = str;
	// VERIFY legality of this DEBUG
	assert str!=null;
    }
    // ACCESSOR METHODS:
    /** Returns the debugging string printed by this quad. */
    public String str() { return str; }

    public int kind() { return QuadKind.DEBUG; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new DEBUG(qqf, this, str);
    }

    public void accept(QuadVisitor v) { v.visit(this); }
    public <T> T accept(QuadValueVisitor<T> v) { return v.visit(this); }

    /** Returns a human-readable version of the <code>DEBUG</code> quad. */
    public String toString() { return "DEBUG: \""+Util.escape(str)+"\""; }
}
