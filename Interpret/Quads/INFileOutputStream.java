// INFileOutputStream.java, created Tue Dec 29 01:36:13 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
/**
 * <code>INFileOutputStream</code> provides implementations of the native
 * methods in <code>java.io.FileOutputStream</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INFileOutputStream.java,v 1.1.2.1 1998-12-30 04:39:39 cananian Exp $
 */
final class INFileOutputStream extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(fdConstructor());
	ss.register(open());
	ss.register(openAppend());
	ss.register(close());
	ss.register(write());
	ss.register(writeBytes());
    }
    // associate shadow FileOutputStream with every object.

    // disallow constructor from FileDescriptor
    private static final NativeMethod fdConstructor() {
	final HMethod hm=HCfostream.getConstructor(new HClass[] {HCfiledesc});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		ObjectRef fd_obj  = (ObjectRef) params[1];
		// get file descriptor int.
		HField hf = HCfiledesc.getField("fd");
		int fd = ((Integer)fd_obj.get(hf)).intValue();
		switch (fd) {
		case 2: // System.out
		    obj.putClosure(System.out); break;
		case 3: // System.err
		    obj.putClosure(System.err); break;
		default: // throw error
		    {
		    ObjectRef ex_obj =
			ss.makeThrowable(HCioE, "unsupported.");
		    throw new InterpretedThrowable(ex_obj, ss);
		    }
		}
		HField hf0 = HCfostream.getField("fd");
		obj.update(hf0, fd_obj);
		return null;
	    }
	};
    }
    private static final NativeMethod open() {
	final HMethod hm=HCfostream.getMethod("open", new HClass[]{HCstring});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		ObjectRef filename = (ObjectRef) params[1];
		System.err.println("OPENING "+filename);
		try {
		    obj.putClosure(new FileOutputStream(ss.ref2str(filename)));
		    // mark file descriptor to indicate it is 'valid'.
		    HField hf0 = HCfostream.getField("fd");
		    HField hf1 = HCfiledesc.getField("fd");
		    ((ObjectRef)obj.get(hf0)).update(hf1, new Integer(4));
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		} catch (SecurityException e) {
		    obj = ss.makeThrowable(HCsecurityE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod openAppend() {
	final HMethod hm =
	    HCfostream.getMethod("openAppend", new HClass[] { HCstring } );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		ObjectRef filename = (ObjectRef) params[1];
		System.err.println("OPENING "+filename);
		try {
		    obj.putClosure(new FileOutputStream(ss.ref2str(filename),
							true/*append*/));
		    // mark file descriptor to indicate it is 'valid'.
		    HField hf0 = HCfostream.getField("fd");
		    HField hf1 = HCfiledesc.getField("fd");
		    ((ObjectRef)obj.get(hf0)).update(hf1, new Integer(4));
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		} catch (SecurityException e) {
		    obj = ss.makeThrowable(HCsecurityE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod close() {
	final HMethod hm = HCfostream.getMethod("close", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		OutputStream os = (OutputStream) obj.getClosure();
		HField hf = HCfostream.getField("fd");
		try {
		    os.close();
		    obj.putClosure(null);
		    obj.update(hf, null);
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod write() {
	final HMethod hm =
	    HCfostream.getMethod("write", new HClass[] { HClass.Int });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		Integer b = (Integer) params[1];
		OutputStream os = (OutputStream) obj.getClosure();
		try {
		    os.write(b.intValue());
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod writeBytes() {
	final HMethod hm =
	    HCfostream.getMethod("writeBytes", "([BII)V");
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

		OutputStream os = (OutputStream) obj.getClosure();
		try {
		    os.write(b, 0, len);
		    return null;
		} catch (IOException e) {
		    obj = ss.makeThrowable(HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
}
