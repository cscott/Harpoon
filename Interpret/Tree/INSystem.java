// INSystem.java, created Mon Dec 28 20:25:36 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

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
 * @version $Id: INSystem.java,v 1.2 2002-02-25 21:05:55 cananian Exp $
 */
final class INSystem {
    static final void register(StaticState ss) {
	ss.register(currentTimeMillis(ss));
	ss.register(initProperties(ss));
	ss.register(arraycopy(ss));
	ss.register(setIn0(ss));
	ss.register(setOut0(ss));
	ss.register(setErr0(ss));
	// JDK 1.2 only
	try { ss.register(registerNatives(ss)); } catch (NoSuchMethodError e){}
    }
    private static final NativeMethod currentTimeMillis(StaticState ss0) {
	final HMethod hm=ss0.HCsystem.getMethod("currentTimeMillis","()J");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		return new Long(System.currentTimeMillis());
	    }
	};
    }
    private static final NativeMethod initProperties(StaticState ss0) {
	final HMethod HMput =
	    ss0.HCproperties.getMethod("put", new HClass[]{ ss0.HCobject,
							    ss0.HCobject });
	final HMethod hm =
	    ss0.HCsystem.getMethod("initProperties",
				   new HClass[]{ ss0.HCproperties });
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
    private static final NativeMethod arraycopy(StaticState ss0) {
	final HMethod hm =
	    ss0.HCsystem.getMethod("arraycopy",
				   new HClass[] { ss0.HCobject, HClass.Int,
						  ss0.HCobject, HClass.Int,
						  HClass.Int });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    private InterpretedThrowable ase(StaticState ss) 
		throws InterpretedThrowable {
		ObjectRef obj = ss.makeThrowable(ss.HCarraystoreE);
		return new InterpretedThrowable(obj, ss);
	    }
	    private InterpretedThrowable aie(StaticState ss)
		throws InterpretedThrowable {
		ObjectRef obj = ss.makeThrowable(ss.HCarrayindexE);
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
    private static final NativeMethod setIn0(StaticState ss0) {
	final HMethod hm =
	    ss0.HCsystem.getMethod("setIn0","(Ljava/io/InputStream;)V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// set final var behind compiler's back.
		ObjectRef obj = (ObjectRef) params[0];
		ss.update(ss.HCsystem.getField("in"), obj);
		return null;
	    }
	};
    }
    private static final NativeMethod setOut0(StaticState ss0) {
	final HMethod hm =
	    ss0.HCsystem.getMethod("setOut0","(Ljava/io/PrintStream;)V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// set final var behind compiler's back.
		ObjectRef obj = (ObjectRef) params[0];
		ss.update(ss.HCsystem.getField("out"), obj);
		return null;
	    }
	};
    }
    private static final NativeMethod setErr0(StaticState ss0) {
	final HMethod hm =
	    ss0.HCsystem.getMethod("setErr0","(Ljava/io/PrintStream;)V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// set final var behind compiler's back.
		ObjectRef obj = (ObjectRef) params[0];
		ss.update(ss.HCsystem.getField("err"), obj);
		return null;
	    }
	};
    }
    // JDK 1.2 only: System.registerNatives()
    private static final NativeMethod registerNatives(StaticState ss0) {
	final HMethod hm =
	    ss0.HCsystem.getMethod("registerNatives",new HClass[0]);
	return new NullNativeMethod(hm);
    }
}
