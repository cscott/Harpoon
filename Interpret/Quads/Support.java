// Support.java, created Mon Dec 28 10:30:00 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.NoSuchClassException;

/**
 * <code>Support</code> provides some native method implementations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Support.java,v 1.1.2.7.2.1 2000-01-11 12:37:26 cananian Exp $
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
	INFile.register(ss);
	INFileInputStream.register(ss);
	INFileOutputStream.register(ss);
	INRandomAccessFile.register(ss);
	// miscellaneous.
	ss.register(fillInStackTrace());
	// JDK 1.1 only
	try { ss.register(initSystemFD()); } catch (NoSuchMethodError e) { }
	// JDK 1.2 only
	try { ss.register(initIDs()); } catch (NoSuchMethodError e) { }
	try { ss.register(doPrivileged()); } catch (NoSuchClassException e) { }
	try { ss.register(registerNatives()); } catch (NoSuchMethodError e) { }
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
    // "initialize JNI offsets" for JDK 1.2. Currently a NOP.
    private static final NativeMethod initIDs() {
	final HMethod hm =
	    HCfiledesc.getMethod("initIDs", new HClass[0]);
	return new NullNativeMethod(hm);
    }
    // do a privileged action.
    private static final NativeMethod doPrivileged() {
	final HClass HCsecAC=HClass.forName("java.security.AccessController");
	final HClass HCprivA=HClass.forName("java.security.PrivilegedAction");
	final HMethod hm = HCsecAC.getMethod("doPrivileged",
					     new HClass[] { HCprivA });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// invoke PrivilegedAction.run()
		ObjectRef obj = (ObjectRef) params[0]; // PrivilegedAction
		return Method.invoke(ss,
				     obj.type.getMethod("run", new HClass[0]),
				     new Object[] { obj } );
	    }
	};
    }
    // JDK 1.2 only: Thread.registerNatives()
    private static final NativeMethod registerNatives() {
	final HMethod hm = HClass.forName("java.lang.Thread")
	    .getMethod("registerNatives",new HClass[0]);
	return new NullNativeMethod(hm);
    }
}
