// INRuntime.java, created Fri Jan  1 12:17:30 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>INRuntime</code> provides implementations of the native methods in
 * <code>java.lang.Runtime</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INRuntime.java,v 1.2 2002-02-25 21:05:55 cananian Exp $
 */
public class INRuntime {
    static final void register(StaticState ss) {
	ss.register(privateConstructor(ss));
	ss.register(gc(ss));
	ss.register(runFinalization(ss));
	ss.register(freeMemory(ss));
	ss.register(totalMemory(ss));
    }
    // the runtime for the interpreter is identical to the current runtime.
    private static final NativeMethod privateConstructor(StaticState ss0) {
	final HMethod hm = ss0.HCruntime.getConstructor(new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
	        ObjectRef obj = (ObjectRef) params[0];
		obj.putClosure(Runtime.getRuntime());
		return null;
	    }
	};
    }
    private static final NativeMethod gc(StaticState ss0) {
	final HMethod hm=ss0.HCruntime.getMethod("gc","()V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Runtime r = (Runtime) obj.getClosure();
		r.gc();
		return null;
	    }
	};
    }
    private static final NativeMethod runFinalization(StaticState ss0) {
	final HMethod hm=ss0.HCruntime.getMethod("runFinalization","()V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Runtime r = (Runtime) obj.getClosure();
		r.runFinalization();
		return null;
	    }
	};
    }
    private static final NativeMethod freeMemory(StaticState ss0) {
	final HMethod hm=ss0.HCruntime.getMethod("freeMemory","()J");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Runtime r = (Runtime) obj.getClosure();
		return new Long(r.freeMemory());
	    }
	};
    }
    private static final NativeMethod totalMemory(StaticState ss0) {
	final HMethod hm=ss0.HCruntime.getMethod("totalMemory","()J");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Runtime r = (Runtime) obj.getClosure();
		return new Long(r.totalMemory());
	    }
	};
    }
}
