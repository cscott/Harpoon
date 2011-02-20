// INRandomAccessFile.java, created Tue Dec 29 01:30:33 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

import java.io.RandomAccessFile;
import java.io.IOException;
/**
 * <code>INRandomAccessFile</code> provides implementations of the native
 * methods in <code>java.io.RandomAccessFile</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INRandomAccessFile.java,v 1.2 2002-02-25 21:05:46 cananian Exp $
 */
final class INRandomAccessFile {
    static final void register(StaticState ss) {
	ss.register(open(ss));
	ss.register(read(ss));
	ss.register(readBytes(ss));
	ss.register(write(ss));
	ss.register(writeBytes(ss));
	ss.register(getFilePointer(ss));
	ss.register(seek(ss));
	ss.register(length(ss));
	ss.register(close(ss));
	// JDK 1.2 only
	try { ss.register(initIDs(ss)); } catch (NoSuchMethodError e) { }
    }
    // "initialize JNI offsets" for JDK 1.2. Currently a NOP.
    private static final NativeMethod initIDs(StaticState ss0) {
	final HMethod hm =
	    ss0.HCrafile.getMethod("initIDs", new HClass[0]);
	return new NullNativeMethod(hm);
    }
    private static final NativeMethod open(StaticState ss0) {
	final HMethod hm =
	    ss0.HCrafile.getMethod("open",
				   new HClass[] {ss0.HCstring,HClass.Boolean});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
	        String name = ss.ref2str((ObjectRef) params[1]);
		boolean writeable = ((Boolean) params[2]).booleanValue();
		System.err.println("OPENING "+name);
		try {
		    obj.putClosure(new RandomAccessFile(name,
							writeable?"rw":"r"));
		    // mark file descriptor to indicate it is 'valid'.
		    HField hf0 = ss.HCrafile.getField("fd");
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
    private static final NativeMethod read(StaticState ss0) {
	final HMethod hm =
	    ss0.HCrafile.getMethod("read", "()I" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		RandomAccessFile raf = (RandomAccessFile) obj.getClosure();
		try {
		    return new Integer(raf.read());
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod readBytes(StaticState ss0) {
	final HMethod hm =
	    ss0.HCrafile.getMethod("readBytes", "([BII)I" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		ArrayRef ba = (ArrayRef) params[1];
		int off = ((Integer) params[2]).intValue();
		int len = ((Integer) params[3]).intValue();
		RandomAccessFile raf = (RandomAccessFile) obj.getClosure();
		try {
		    byte[] b = new byte[len];
		    len = raf.read(b, 0, len);
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
    private static final NativeMethod write(StaticState ss0) {
	final HMethod hm =
	    ss0.HCrafile.getMethod("write", "(I)V" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		int b = ((Integer) params[1]).intValue();
		RandomAccessFile raf = (RandomAccessFile) obj.getClosure();
		try {
		    raf.write(b);
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
	    ss0.HCrafile.getMethod("writeBytes", "([BII)V" );
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

		RandomAccessFile raf = (RandomAccessFile) obj.getClosure();
		try {
		    raf.write(b, 0, len);
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod getFilePointer(StaticState ss0) {
	final HMethod hm =
	    ss0.HCrafile.getMethod("getFilePointer", "()J" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		RandomAccessFile raf = (RandomAccessFile) obj.getClosure();
		try {
		    return new Long(raf.getFilePointer());
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod seek(StaticState ss0) {
	final HMethod hm =
	    ss0.HCrafile.getMethod("seek", "(J)V" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		Long pos = (Long) params[1];

		RandomAccessFile raf = (RandomAccessFile) obj.getClosure();
		try {
		    raf.seek(pos.longValue());
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod length(StaticState ss0) {
	final HMethod hm =
	    ss0.HCrafile.getMethod("length", "()J" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		RandomAccessFile raf = (RandomAccessFile) obj.getClosure();
		try {
		    return new Long(raf.length());
		} catch (IOException e) {
		    obj = ss.makeThrowable(ss.HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod close(StaticState ss0) {
	final HMethod hm =
	    ss0.HCrafile.getMethod("close", "()V" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		RandomAccessFile raf = (RandomAccessFile) obj.getClosure();
		HField hf = ss.HCrafile.getField("fd");
		try {
		    raf.close();
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
}
