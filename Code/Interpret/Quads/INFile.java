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
 * @version $Id: INFile.java,v 1.2 2002-02-25 21:05:45 cananian Exp $
 */
public class INFile {
    static final void register(StaticState ss) {
	// JDK 1.1 only
	try { ss.register(isFile0(ss)); } catch (NoSuchMethodError e) {}
	try { ss.register(isDirectory0(ss)); } catch (NoSuchMethodError e) {}
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
    // verify that the File is a directory.
    private static final NativeMethod isDirectory0(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfile.getMethod("isDirectory0", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HField hf = ss.HCfile.getField("path");
		String path = ss.ref2str((ObjectRef)obj.get(hf));
		return new Boolean(new java.io.File(path).isDirectory());
	    }
	};
    }
}
