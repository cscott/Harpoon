// Support.java, created Mon Dec 28 10:30:00 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

/**
 * <code>Support</code> provides some native method implementations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Support.java,v 1.1.2.2 1998-12-30 04:39:40 cananian Exp $
 */
final class Support extends HCLibrary {
    static final void registerNative(StaticState ss) {
	INSystem.register(ss);
	INString.register(ss);
	INClass.register(ss);
	INFileOutputStream.register(ss);
	INRandomAccessFile.register(ss);
	ss.register(initSystemFD());
	ss.register(fillInStackTrace());
	ss.register(_getClass_());
    }

    //--------------------------------------------------------

    // Object.getClass() method.
    private static final NativeMethod _getClass_() {
	final HMethod hm = HCobject.getMethod("getClass", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		return INClass.forClass(ss, obj.type);
	    }
	};
    }
    // fill in stack trace for exceptions.
    private static final NativeMethod fillInStackTrace() {
	final HMethod hm =
	    HCthrowable.getMethod("fillInStackTrace", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		String[] st = ss.stackTrace();
		// leave stack trace field empty.
		obj.putClosure(st);
		return obj; // return 'this'
	    }
	};
    }
    // initialize file descriptors.
    private static final NativeMethod initSystemFD() {
	final HMethod hm =
	    HCfiledesc.getMethod("initSystemFD",
				 new HClass[] { HCfiledesc, HClass.Int });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		Integer fd = (Integer) params[1];
		// set 'fd' field
		HField hf = HCfiledesc.getField("fd");
		obj.update(hf, new Integer(fd.intValue()+1));
		return obj;
	    }
	};
    }
}
