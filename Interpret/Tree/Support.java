// Support.java, created Mon Dec 28 10:30:00 1998 by cananian
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>Support</code> provides some native method implementations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Support.java,v 1.1.2.1 1999-03-27 22:05:09 duncan Exp $
 */
final class Support extends HCLibrary {
    static final void registerNative(StaticState ss) {
	// java.lang.*
	INClass.register(ss);
	INFloatDouble.register(ss);
	INObject.register(ss);
	INRuntime.register(ss);
	INString.register(ss);
	INSystem.register(ss);
	// java.io.*
	INFileInputStream.register(ss);
	INFileOutputStream.register(ss);
	INRandomAccessFile.register(ss);
	// miscellaneous.
	ss.register(initSystemFD());
	ss.register(fillInStackTrace());
    }

    //--------------------------------------------------------

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
