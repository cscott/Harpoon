// INRuntime.java, created Fri Jan  1 12:17:30 1999 by cananian
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>INRuntime</code> provides implementations of the native methods in
 * <code>java.lang.Runtime</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INRuntime.java,v 1.1.2.1 1999-03-27 22:05:08 duncan Exp $
 */
public class INRuntime extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(privateConstructor());
	ss.register(gc());
	ss.register(runFinalization());
	ss.register(freeMemory());
	ss.register(totalMemory());
    }
    // the runtime for the interpreter is identical to the current runtime.
    private static final NativeMethod privateConstructor() {
	final HMethod hm = HCruntime.getConstructor(new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
	        ObjectRef obj = (ObjectRef) params[0];
		obj.putClosure(Runtime.getRuntime());
		return null;
	    }
	};
    }
    private static final NativeMethod gc() {
	final HMethod hm=HCruntime.getMethod("gc","()V");
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
    private static final NativeMethod runFinalization() {
	final HMethod hm=HCruntime.getMethod("runFinalization","()V");
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
    private static final NativeMethod freeMemory() {
	final HMethod hm=HCruntime.getMethod("freeMemory","()J");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Runtime r = (Runtime) obj.getClosure();
		return new Long(r.freeMemory());
	    }
	};
    }
    private static final NativeMethod totalMemory() {
	final HMethod hm=HCruntime.getMethod("totalMemory","()J");
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
