// INResourceBundle.java, created Fri Sep 28 17:01:58 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

/**
 * <code>INResourceBundle</code> provides implementations for (some
 * of) the native methods in <code>java.util.ResourceBundle</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INResourceBundle.java,v 1.1.2.1 2001-09-28 21:54:41 cananian Exp $
 */
public class INResourceBundle {
    static final void register(StaticState ss) {
	try{ss.register(getClassContext(ss));}catch(NoSuchMethodError e){}
    }
    // punt on the implementation of getClassContext: always return an
    // array of three nulls.
    private static final NativeMethod getClassContext(StaticState ss0) {
	final HClass hc = ss0.linker.forName("java.util.ResourceBundle");
	final HMethod hm =
	    hc.getMethod("getClassContext", new HClass[0] );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// punt: return array of three nulls.
		return new ArrayRef(ss, ss.HCclassA, new int[] { 3 });
	    }
	};
    }
}
