// INString.java, created Mon Dec 28 21:22:06 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>INString</code> provides implementations of the native methods in
 * <code>java.lang.String</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INString.java,v 1.1.2.2 1999-08-04 05:52:35 cananian Exp $
 */
public class INString extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(intern());
    }
    private static final NativeMethod intern() {
	final HMethod hm = 
	    HCstring.getMethod("intern", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		return ss.intern(obj);
	    }
	};
    }
}
