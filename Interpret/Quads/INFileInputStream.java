// INFileInputStream.java, created Wed Dec 30 02:01:00 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
/**
 * <code>INFileInputStream</code> provides implementations of the native
 * methods in <code>java.io.FileInputStream</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INFileInputStream.java,v 1.1.2.9 2001-09-28 21:51:53 cananian Exp $
 */
final class INFileInputStream {
    static final void register(StaticState ss) {
	ss.registerOverride(fdConstructor(ss));
	ss.register(open(ss));
	ss.register(close(ss));
	ss.register(read(ss));
	ss.register(readBytes(ss));
	ss.register(skip(ss));
	ss.register(available(ss));
	// JDK 1.2 only
	try { ss.register(initIDs(ss)); } catch (NoSuchMethodError e) { }
    }
    // associate shadow InputStream with every object.
    // Make a serializable wrapper for it.
    static class InputStreamWrapper implements java.io.Serializable {
	transient InputStream is;
	InputStreamWrapper(InputStream is) { this.is = is; }
	private void writeObject(java.io.ObjectOutputStream out)
	    throws java.io.IOException {
	    if (is==System.in) out.writeByte(0);
	    else throw new java.io.NotSerializableException();
	}
	private void readObject(java.io.ObjectInputStream in)
	    throws java.io.IOException {
	    switch(in.readByte()) {
	    case 0: is = System.in; break;
	    default: throw new java.io.InvalidObjectException("Unknown input stream.");
	    }
	}
    }
    // helper method: create a new interpreted FileInputStream from a real one.
    static ObjectRef openInputStream(StaticState ss, InputStream is) {
	// make fileinputstream object
	ObjectRef fis_obj = new ObjectRef(ss, ss.HCfistream);
	fis_obj.putClosure(new InputStreamWrapper(is));
	// make file descriptor object
	ObjectRef fd_obj = new ObjectRef(ss, ss.HCfiledesc);
	// mark file descriptor to indicate it is 'valid'.
	HField hf1 = ss.HCfiledesc.getField("fd");
	fd_obj.update(hf1, new Integer(4));
	// set fileinputstream's filedescriptor.
	HField hf0 = ss.HCfistream.getField("fd");
	fis_obj.update(hf0, fd_obj);
	// done.
	return fis_obj;
    }

    private static final ObjectRef security(StaticState ss) 
	throws InterpretedThrowable {
	HMethod hm=ss.HCsystem.getMethod("getSecurityManager", new HClass[0]);
	return (ObjectRef) Method.invoke(ss, hm, new Object[0]);
    }

    // disallow constructor from FileDescriptor
    private static final NativeMethod fdConstructor(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfistream.getConstructor(new HClass[] { ss0.HCfiledesc });
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
		    HMethod HMcr =
			ss.HCsmanager.getMethod("checkRead",
					     new HClass[] { ss.HCfiledesc });
		    Method.invoke(ss, HMcr, new Object[] { fd_obj });
		}
		// compare supplied file descriptor to FileDescriptor.in
		HField hf = ss.HCfiledesc.getField("in");
		if (fd_obj == ss.get(hf)) { // System.in
		    obj.putClosure(new InputStreamWrapper(System.in));
		    HField hf0 = ss.HCfistream.getField("fd");
		    obj.update(hf0, fd_obj);
		    return null;
		} else {
		    ObjectRef ex_obj =
			ss.makeThrowable(ss.HCioE, "unsupported.");
		    throw new InterpretedThrowable(ex_obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod open(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfistream.getMethod("open", new HClass[] { ss0.HCstring });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		String filename = ss.ref2str((ObjectRef) params[1]);
		if (ss.TRACE)
		System.err.println("OPENING "+filename);
		try {
		    obj.putClosure(new InputStreamWrapper(new FileInputStream(filename)));
		    // mark file descriptor to indicate it is 'valid'.
		    HField hf0 = ss.HCfistream.getField("fd");
		    HField hf1 = ss.HCfiledesc.getField("fd");
		    ((ObjectRef)obj.get(hf0)).update(hf1, new Integer(4));
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable
			(ss.linker.forName("java.io.FileNotFoundException"),
			 e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		} catch (SecurityException e) {
		    obj = ss.makeThrowable(ss.HCsecurityE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod close(StaticState ss0) {
	final HMethod hm = ss0.HCfistream.getMethod("close", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		InputStream is = ((InputStreamWrapper) obj.getClosure()).is;
		HField hf = ss.HCfistream.getField("fd");
		try {
		    is.close();
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
    private static final NativeMethod read(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfistream.getMethod("read", "()I");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		InputStream is = ((InputStreamWrapper) obj.getClosure()).is;
		try {
		    return new Integer(is.read());
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod readBytes(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfistream.getMethod("readBytes", "([BII)I");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		ArrayRef ba = (ArrayRef) params[1];
		int off = ((Integer) params[2]).intValue();
		int len = ((Integer) params[3]).intValue();
		InputStream is = ((InputStreamWrapper) obj.getClosure()).is;
		try {
		    byte[] b = new byte[len];
		    len = is.read(b, 0, len);
		    // copy into byte array.
		    for (int i=0; i<len; i++)
			ba.update(off+i, new Byte(b[i]));
		    return new Integer(len);
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod skip(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfistream.getMethod("skip", "(J)J" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		Long n = (Long) params[1];
		InputStream is = ((InputStreamWrapper) obj.getClosure()).is;
		try {
		    return new Long(is.skip(n.longValue()));
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod available(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfistream.getMethod("available", "()I" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		InputStream is = ((InputStreamWrapper) obj.getClosure()).is;
		try {
		    return new Integer(is.available());
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
	    ss0.HCfistream.getMethod("initIDs", new HClass[0]);
	return new NullNativeMethod(hm);
    }
}
