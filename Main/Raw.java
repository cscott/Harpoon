// Raw.java, created Mon Nov 16 22:03:38 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import java.io.InputStream;
import harpoon.ClassFile.Loader;
/**
 * <code>Raw</code> prints out the raw data structures from the
 * class file corresponding to the class name given on the
 * command-line.
 * 
 * @author  C. Scott Ananian <cananian@lesser-magoo.lcs.mit.edu>
 * @version $Id: Raw.java,v 1.2 2002-02-25 21:06:09 cananian Exp $
 */

public abstract class Raw /*extends harpoon.IR.Registration*/ {
    public static final void main(String args[]) {
	String classname = args[0];

	InputStream is = 
	    Loader.getResourceAsStream(Loader.classToResource(classname));
	if (is==null) throw new NoClassDefFoundError(classname);
	harpoon.IR.RawClass.ClassFile raw =
	    new harpoon.IR.RawClass.ClassFile(is);
	raw.print(new java.io.PrintWriter(System.out, true));
    }
}
