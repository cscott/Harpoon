// INFile.java, created Sat Nov 13 01:00:13 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>INFile</code> provides implementations for (some of) the native
 * methods in <code>java.io.File</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INFile.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
public class INFile {
    static final void register(StaticState ss) {
	ss.register(isFile0(ss));
    }
    // test existence of a file.
    private static final NativeMethod isFile0(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfile.getMethod("isFile0", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HField hf = ss.HCfile.getField("path");
		String path = ss.ref2str((ObjectRef)obj.get(hf));
		return new Boolean(new java.io.File(path).isFile());
	    }
	};
    }
}
