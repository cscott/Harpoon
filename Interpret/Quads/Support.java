// Support.java, created Mon Dec 28 10:30:00 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

import java.util.Enumeration;
import java.util.Properties;
/**
 * <code>Support</code> provides some native method implementations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Support.java,v 1.1.2.1 1998-12-28 23:43:21 cananian Exp $
 */
final class Support  {
    static final void registerNative(StaticState ss) {
	ss.register(currentTimeMillis());
	ss.register(initProperties());
	ss.register(arraycopy());
	ss.register(initSystemFD());
	ss.register(setIn0());
	ss.register(setOut0());
	ss.register(setErr0());
	ss.register(intern());
	ss.register(getPrimitiveClass());
    }

    static final ObjectRef makeThrowable(StaticState ss, HClass HCex) 
	throws InterpretedThrowable {
	ObjectRef obj = new ObjectRef(ss, HCex);
	Method.invoke(ss, HCex.getConstructor(new HClass[0]),
		      new Object[] { obj } );
	return obj;
    }
    //--------------------------------------------------------
    static final HClass 
	HCcharA = HClass.forDescriptor("[C"),
	HCstring = HClass.forName("java.lang.String"),
	HCstringA = HClass.forDescriptor("[Ljava/lang/String;"),
	HCclass = HClass.forName("java.lang.Class"),
	HCobject  = HClass.forName("java.lang.Object"),
	HCsystem = HClass.forName("java.lang.System"),
	HCfiledesc = HClass.forName("java.io.FileDescriptor"),
	HCproperties = HClass.forName("java.util.Properties"),
	HCarraystoreE = HClass.forName("java.lang.ArrayStoreException"),
	HCarrayindexE = HClass.forName("java.lang.ArrayIndexOutOfBounds"+
				       "Exception");
    //--------------------------------------------------------
    private static final NativeMethod currentTimeMillis() {
	final HMethod hm = HCsystem.getMethod("currentTimeMillis","()J");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params, Object closure) {
		return new Long(System.currentTimeMillis());
	    }
	};
    }
    private static final NativeMethod initProperties() {
	final HMethod HMput =
	    HCproperties.getMethod("put", new HClass[] { HCobject, HCobject });
	final HMethod hm =
	    HCsystem.getMethod("initProperties", new HClass[]{ HCproperties });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params, Object closure) 
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
		ObjectRef obj = makeThrowable(ss, HCarraystoreE);
		return new InterpretedThrowable(obj, ss);
	    }
	    private InterpretedThrowable aie(StaticState ss)
		throws InterpretedThrowable {
		ObjectRef obj = makeThrowable(ss, HCarrayindexE);
		return new InterpretedThrowable(obj, ss);
	    }
	    Object invoke(StaticState ss, Object[] params, Object closure)
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
		    if (o instanceof ObjectRef &&
			!dstCT.isSuperclassOf(((ObjectRef)o).type))
			throw ase(ss);
		    else dst.update(i+dst_position, o);
		}
		return null; /* void */
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
	    Object invoke(StaticState ss, Object[] params, Object closure) {
		ObjectRef obj = (ObjectRef) params[0];
		Integer fd = (Integer) params[1];
		// set 'fd' field
		HField hf = HCfiledesc.getField("fd");
		obj.update(hf, fd);
		return obj;
	    }
	};
    }
    private static final NativeMethod setIn0() {
	final HMethod hm =
	    HCsystem.getMethod("setIn0","(Ljava/io/InputStream;)V");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params, Object closure) {
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
	    Object invoke(StaticState ss, Object[] params, Object closure) {
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
	    Object invoke(StaticState ss, Object[] params, Object closure) {
		// set final var behind compiler's back.
		ObjectRef obj = (ObjectRef) params[0];
		ss.update(HCsystem.getField("err"), obj);
		return null;
	    }
	};
    }
    private static final NativeMethod intern() {
	final HMethod hm = 
	    HCstring.getMethod("intern", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params, Object closure) {
		ObjectRef obj = (ObjectRef) params[0];
		return ss.intern(obj);
	    }
	};
    }
    private static final NativeMethod getPrimitiveClass() {
	final HMethod hm =
	    HCclass.getMethod("getPrimitiveClass", new HClass[] {HCstring});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params, Object closure) {
		ObjectRef obj = (ObjectRef) params[0];
		System.err.println("Punting request for " +
				   ss.ref2str(obj) + ".class");
		return null; // we're too lazy to do this right.
	    }
	};
    }
}
