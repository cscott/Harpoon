// INFileOutputStream.java, created Tue Dec 29 01:36:13 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
/**
 * <code>INFileOutputStream</code> provides implementations of the native
 * methods in <code>java.io.FileOutputStream</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INFileOutputStream.java,v 1.2 2002-02-25 21:05:46 cananian Exp $
 */
final class INFileOutputStream {
    static final void register(StaticState ss) {
	ss.registerOverride(fdConstructor(ss));
	ss.register(open(ss));
	ss.register(openAppend(ss));
	ss.register(close(ss));
	ss.register(write(ss));
	ss.register(writeBytes(ss));
	// JDK 1.2 only
	try { ss.register(initIDs(ss)); } catch (NoSuchMethodError e) { }
    }
    // associate shadow OutputStream with every object.
    // Make a serializable wrapper for it.
    static class OutputStreamWrapper implements java.io.Serializable {
	transient OutputStream os;
	OutputStreamWrapper(OutputStream os) { this.os = os; }
	private void writeObject(java.io.ObjectOutputStream out)
	    throws java.io.IOException {
	    if (os==System.out) out.writeByte(1);
	    else if (os==System.err) out.writeByte(2);
	    else throw new java.io.NotSerializableException();
	}
	private void readObject(java.io.ObjectInputStream in)
	    throws java.io.IOException {
	    switch(in.readByte()) {
	    case 1: os = System.out; break;
	    case 2: os = System.err; break;
	    default: throw new java.io.InvalidObjectException("Unknown output stream.");
	    }
	}
    }

    private static final ObjectRef security(StaticState ss) 
	throws InterpretedThrowable {
	HMethod hm= ss.HCsystem.getMethod("getSecurityManager", new HClass[0]);
	return (ObjectRef) Method.invoke(ss, hm, new Object[0]);
    }

    // disallow constructor from FileDescriptor
    private static final NativeMethod fdConstructor(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfostream.getConstructor(new HClass[] { ss0.HCfiledesc });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		ObjectRef fd_obj  = (ObjectRef) params[1];
		// check safety.
		if (fd_obj==null) {
		    ObjectRef ex_obj =
			ss.makeThrowable(ss.HCnullpointerE);
		    throw new InterpretedThrowable(ex_obj, ss);
		}
		ObjectRef security = security(ss);
		if (security != null) {
		    HMethod HMcw =
			ss.HCsmanager.getMethod("checkWrite",
					     new HClass[] { ss.HCfiledesc });
		    Method.invoke(ss, HMcw, new Object[] { fd_obj });
		}
		// compare given file descriptor.
		if (fd_obj == ss.get(ss.HCfiledesc.getField("out"))) {
		    // System.out
		    obj.putClosure(new OutputStreamWrapper(System.out));
		} else if (fd_obj == ss.get(ss.HCfiledesc.getField("err"))) {
		    // System.err
		    obj.putClosure(new OutputStreamWrapper(System.err));
		} else { // throw error
		    ObjectRef ex_obj =
			ss.makeThrowable(ss.HCioE, "unsupported.");
		    throw new InterpretedThrowable(ex_obj, ss);
		}
		HField hf0 = ss.HCfostream.getField("fd");
		obj.update(hf0, fd_obj);
		return null;
	    }
	};
    }
    private static final NativeMethod open(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfostream.getMethod("open", new HClass[] { ss0.HCstring });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		String filename = ss.ref2str((ObjectRef) params[1]);
		System.err.println("OPENING "+filename);
		try {
		    obj.putClosure(new OutputStreamWrapper(new FileOutputStream(filename)));
		    // mark file descriptor to indicate it is 'valid'.
		    HField hf0 = ss.HCfostream.getField("fd");
		    HField hf1 = ss.HCfiledesc.getField("fd");
		    ((ObjectRef)obj.get(hf0)).update(hf1, new Integer(4));
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		} catch (SecurityException e) {
		    obj = ss.makeThrowable(ss.HCsecurityE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod openAppend(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfostream.getMethod("openAppend", new HClass[]{ss0.HCstring});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		String filename = ss.ref2str((ObjectRef) params[1]);
		System.err.println("OPENING "+filename);
		try {
		    obj.putClosure(new OutputStreamWrapper(new FileOutputStream(filename, true)));
		    // mark file descriptor to indicate it is 'valid'.
		    HField hf0 = ss.HCfostream.getField("fd");
		    HField hf1 = ss.HCfiledesc.getField("fd");
		    ((ObjectRef)obj.get(hf0)).update(hf1, new Integer(4));
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		} catch (SecurityException e) {
		    obj = ss.makeThrowable(ss.HCsecurityE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod close(StaticState ss0) {
	final HMethod hm = ss0.HCfostream.getMethod("close", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		OutputStream os = ((OutputStreamWrapper) obj.getClosure()).os;
		HField hf = ss.HCfostream.getField("fd");
		try {
		    os.close();
		    obj.putClosure(null);
		    obj.update(hf, null);
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod write(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfostream.getMethod("write", new HClass[] { HClass.Int });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		Integer b = (Integer) params[1];
		OutputStream os = ((OutputStreamWrapper) obj.getClosure()).os;
		try {
		    os.write(b.intValue());
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod writeBytes(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfostream.getMethod("writeBytes", "([BII)V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		ArrayRef ba = (ArrayRef) params[1];
		int off = ((Integer) params[2]).intValue();
		int len = ((Integer) params[3]).intValue();
		// repackage byte array.
		byte[] b = new byte[len];
		for (int i=0; i<b.length; i++)
		    b[i] = ((Byte) ba.get(off+i)).byteValue();

		OutputStream os = ((OutputStreamWrapper) obj.getClosure()).os;
		try {
		    os.write(b, 0, len);
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }

    // "initialize JNI offsets" for JDK 1.2. Currently a NOP.
    private static final NativeMethod initIDs(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfostream.getMethod("initIDs", new HClass[0]);
	return new NullNativeMethod(hm);
    }
}
