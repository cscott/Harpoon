// INSystem.java, created Mon Dec 28 20:25:36 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

import java.util.Enumeration;
import java.util.Properties;
/**
 * <code>INSystem</code> provides implementations of the native methods in
 * <code>java.lang.System</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INSystem.java,v 1.1.2.5 1999-08-04 05:52:30 cananian Exp $
 */
final class INSystem extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(currentTimeMillis());
	ss.register(initProperties());
	ss.register(arraycopy());
	ss.register(setIn0());
	ss.register(setOut0());
	ss.register(setErr0());
    }
    private static final NativeMethod currentTimeMillis() {
	final HMethod hm=HCsystem.getMethod("currentTimeMillis","()J");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		return new Long(System.currentTimeMillis());
	    }
	};
    }
    private static final NativeMethod initProperties() {
	final HMethod HMput =
	    HCproperties.getMethod("put", new HClass[]{ HCobject, HCobject });
	final HMethod hm =
	    HCsystem.getMethod("initProperties", new HClass[]{ HCproperties });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) 
		throws InterpretedThrowable {
		ObjectRef obj = (ObjectRef) params[0];
		Properties p = System.getProperties();
		for (Enumeration e=p.propertyNames(); e.hasMoreElements(); ) {
		    String key = (String)e.nextElement();
		    String value = p.getProperty(key);
		    // now initialize the given property ref with these.
		    Object[] args = new Object[] { obj,
						   ss.makeString(key),
						   ss.makeString(value) };
		    Method.invoke(ss, HMput, args);
		}
		return obj;
	    }
	};
    }
    private static final NativeMethod arraycopy() {
	final HMethod hm =
	    HCsystem.getMethod("arraycopy",
			       new HClass[] { HCobject, HClass.Int,
					      HCobject, HClass.Int,
					      HClass.Int });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    private InterpretedThrowable ase(StaticState ss) 
		throws InterpretedThrowable {
		ObjectRef obj = ss.makeThrowable(HCarraystoreE);
		return new InterpretedThrowable(obj, ss);
	    }
	    private InterpretedThrowable aie(StaticState ss)
		throws InterpretedThrowable {
		ObjectRef obj = ss.makeThrowable(HCarrayindexE);
		return new InterpretedThrowable(obj, ss);
	    }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		if (!( params[0] instanceof ArrayRef && 
		       params[2] instanceof ArrayRef) )
		    throw ase(ss); // array store exception
		ArrayRef src = (ArrayRef) params[0];
		int src_position = ((Integer) params[1]).intValue();
		ArrayRef dst = (ArrayRef) params[2];
		int dst_position = ((Integer) params[3]).intValue();
		int length = ((Integer) params[4]).intValue();
		HClass srcCT = src.type.getComponentType();
		HClass dstCT = dst.type.getComponentType();
		if (srcCT.isPrimitive() != dstCT.isPrimitive() ||
		    (srcCT.isPrimitive() && dstCT.isPrimitive() &&
		     srcCT != dstCT) )
		    throw ase(ss); // array store exception
		if (src_position < 0 || dst_position < 0 || length < 0 ||
		    src_position+length > src.length() ||
		    dst_position+length > dst.length())
		    throw aie(ss); // array index out of bounds
		for (int i=0; i<length; i++) {
		    Object o = src.get(i+src_position);
		    if (o instanceof Ref &&
			!((Ref)o).type.isInstanceOf(dstCT))
			throw ase(ss);
		    else dst.update(i+dst_position, o);
		}
		return null; /* void */
	    }
	};
    }
    private static final NativeMethod setIn0() {
	final HMethod hm =
	    HCsystem.getMethod("setIn0","(Ljava/io/InputStream;)V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// set final var behind compiler's back.
		ObjectRef obj = (ObjectRef) params[0];
		ss.update(HCsystem.getField("in"), obj);
		return null;
	    }
	};
    }
    private static final NativeMethod setOut0() {
	final HMethod hm =
	    HCsystem.getMethod("setOut0","(Ljava/io/PrintStream;)V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// set final var behind compiler's back.
		ObjectRef obj = (ObjectRef) params[0];
		ss.update(HCsystem.getField("out"), obj);
		return null;
	    }
	};
    }
    private static final NativeMethod setErr0() {
	final HMethod hm =
	    HCsystem.getMethod("setErr0","(Ljava/io/PrintStream;)V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// set final var behind compiler's back.
		ObjectRef obj = (ObjectRef) params[0];
		ss.update(HCsystem.getField("err"), obj);
		return null;
	    }
	};
    }
}
