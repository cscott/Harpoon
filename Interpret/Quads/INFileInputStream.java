// INFileInputStream.java, created Wed Dec 30 02:01:00 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
/**
 * <code>INFileInputStream</code> provides implementations of the native
 * methods in <code>java.io.FileInputStream</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INFileInputStream.java,v 1.1.2.1 1999-01-03 03:01:43 cananian Exp $
 */
final class INFileInputStream extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(fdConstructor());
	ss.register(open());
	ss.register(close());
	ss.register(read());
	ss.register(readBytes());
	ss.register(skip());
	ss.register(available());
    }
    // associate shadow InputStream with every object.

    private static final ObjectRef security(StaticState ss) 
	throws InterpretedThrowable {
	HMethod hm = HCsystem.getMethod("getSecurityManager", new HClass[0]);
	return (ObjectRef) Method.invoke(ss, hm, new Object[0]);
    }

    // disallow constructor from FileDescriptor
    private static final NativeMethod fdConstructor() {
	final HMethod hm=HCfistream.getConstructor(new HClass[] {HCfiledesc});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		ObjectRef fd_obj  = (ObjectRef) params[1];
		// check safety.
		if (fd_obj==null) {
		    ObjectRef ex_obj =
			ss.makeThrowable(HCnullpointerE);
		    throw new InterpretedThrowable(ex_obj, ss);
		}
		ObjectRef security = security(ss);
		if (security != null) {
		    HMethod HMcr =
			HCsmanager.getMethod("checkRead",
					     new HClass[] { HCfiledesc });
		    Method.invoke(ss, HMcr, new Object[] { fd_obj });
		}
		// get file descriptor int.
		HField hf = HCfiledesc.getField("fd");
		int fd = ((Integer)fd_obj.get(hf)).intValue();
		if (fd==1) { // System.in
		    obj.putClosure(System.in);
		    HField hf0 = HCfistream.getField("fd");
		    obj.update(hf0, fd_obj);
		    return null;
		} else {
		    ObjectRef ex_obj =
			ss.makeThrowable(HCioE, "unsupported.");
		    throw new InterpretedThrowable(ex_obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod open() {
	final HMethod hm=HCfistream.getMethod("open", new HClass[]{HCstring});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		String filename = ss.ref2str((ObjectRef) params[1]);
		System.err.println("OPENING "+filename);
		try {
		    obj.putClosure(new FileInputStream(filename));
		    // mark file descriptor to indicate it is 'valid'.
		    HField hf0 = HCfistream.getField("fd");
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
	final HMethod hm = HCfistream.getMethod("close", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		InputStream is = (InputStream) obj.getClosure();
		HField hf = HCfistream.getField("fd");
		try {
		    is.close();
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
    private static final NativeMethod read() {
	final HMethod hm =
	    HCfistream.getMethod("read", "()I");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		InputStream is = (InputStream) obj.getClosure();
		try {
		    return new Integer(is.read());
		} catch (IOException e) {
		    obj = ss.makeThrowable(HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod readBytes() {
	final HMethod hm =
	    HCfistream.getMethod("readBytes", "([BII)I");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		ArrayRef ba = (ArrayRef) params[1];
		int off = ((Integer) params[2]).intValue();
		int len = ((Integer) params[3]).intValue();
		InputStream is = (InputStream) obj.getClosure();
		try {
		    byte[] b = new byte[len];
		    len = is.read(b, 0, len);
		    // copy into byte array.
		    for (int i=0; i<len; i++)
			ba.update(off+i, new Byte(b[i]));
		    return new Integer(len);
		} catch (IOException e) {
		    obj = ss.makeThrowable(HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod skip() {
	final HMethod hm =
	    HCfistream.getMethod("skip", "(J)J" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		Long n = (Long) params[1];
		InputStream is = (InputStream) obj.getClosure();
		try {
		    return new Long(is.skip(n.longValue()));
		} catch (IOException e) {
		    obj = ss.makeThrowable(HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod available() {
	final HMethod hm =
	    HCfistream.getMethod("available", "()I" );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		InputStream is = (InputStream) obj.getClosure();
		try {
		    return new Integer(is.available());
		} catch (IOException e) {
		    obj = ss.makeThrowable(HCioE, e.getMessage());
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
}
