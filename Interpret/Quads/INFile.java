// INFile.java, created Sat Nov 13 01:00:13 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>INFile</code> provides implementations for (some of) the native
 * methods in <code>java.io.File</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INFile.java,v 1.1.2.1 1999-11-13 06:25:07 cananian Exp $
 */
public class INFile extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(isFile0());
    }
    // test existence of a file.
    private static final NativeMethod isFile0() {
	final HMethod hm =
	    HCfile.getMethod("isFile0", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HField hf = HCfile.getField("path");
		String path = ss.ref2str((ObjectRef)obj.get(hf));
		return new Boolean(new java.io.File(path).isFile());
	    }
	};
    }
}
