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
 * @version $Id: INString.java,v 1.2 2002-02-25 21:05:55 cananian Exp $
 */
public class INString {
    static final void register(StaticState ss) {
	ss.register(intern(ss));
    }
    private static final NativeMethod intern(StaticState ss0) {
	final HMethod hm = 
	    ss0.HCstring.getMethod("intern", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		return ss.intern(obj);
	    }
	};
    }
}
