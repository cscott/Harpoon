// INClass.java, created Mon Dec 28 21:24:34 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.NoSuchClassException;

import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.Hashtable;
/**
 * <code>INClass</code> provides implementations of the native methods in
 * <code>java.lang.Class</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INClass.java,v 1.2 2002-02-25 21:05:45 cananian Exp $
 */
public class INClass {
    static final void register(StaticState ss) {
	try { // JDK 1.2 only
	    ss.register(forName0(ss));
	} catch (NoSuchMethodError e) { // JDK 1.1 fallback.
	    ss.register(forName(ss));
	}
	ss.register(getComponentType(ss));
	ss.register(getInterfaces(ss));
	ss.register(getModifiers(ss));
	ss.register(getName(ss));
	ss.register(getPrimitiveClass(ss));
	ss.register(getSuperclass(ss));
	ss.register(isArray(ss));
	ss.register(isInterface(ss));
	ss.register(isPrimitive(ss));
	try { // JDK 1.2 only
	    ss.register(newInstance0(ss));
	} catch (NoSuchMethodError e) { // JDK 1.1 fallback.
	    ss.register(newInstance(ss));
	}
	// registry for name->class mapping
	ss.putNativeClosure(ss.HCclass, new Hashtable());
	// JDK 1.2 only
	try { ss.register(registerNatives(ss)); } catch (NoSuchMethodError e){}
	try { ss.register(getClassLoader0(ss)); } catch (NoSuchMethodError e){}
    }
    static final ObjectRef forClass(StaticState ss, HClass hc)
	throws InterpretedThrowable {
	Hashtable registry = (Hashtable) ss.getNativeClosure(ss.HCclass);
	ObjectRef obj = (ObjectRef) registry.get(hc);
	if (obj!=null) return obj;
	obj = new ObjectRef(ss, ss.HCclass);
	Method.invoke(ss, ss.HCclass.getConstructor(new HClass[0]),
		      new Object[] { obj } );
	obj.putClosure(hc);
	registry.put(hc, obj);
	return obj;
    }
    private static final NativeMethod forName(StaticState ss0) {
	final HMethod hm =
	    ss0.HCclass.getMethod("forName", new HClass[] { ss0.HCstring });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    // throws ClassNotFoundException
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		String clsname = ss.ref2str((ObjectRef)params[0]);
		try {
		    return forClass(ss, ss.linker.forName(clsname));
		} catch (NoSuchClassException e) {
		    ObjectRef obj = ss.makeThrowable(ss.HCclassnotfoundE);
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    // JDK 1.2 version of forName()
    private static final NativeMethod forName0(StaticState ss0) {
	final HMethod hm = ss0.HCclass.getMethod
	    ("forName0", 
	     "(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    // throws ClassNotFoundException
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		String name = ss.ref2str((ObjectRef) params[0]);
		boolean initialize = ((Boolean) params[1]).booleanValue();
		ObjectRef loader = (ObjectRef) params[2];
		Util.assert(loader==null, "Haven't implemented class loading "+
			    "from a ClassLoader object.");
		Util.assert(initialize, "Haven't implemented uninitialized "+
			    "class loading.");
		try {
		    return forClass(ss, ss.linker.forName(name));
		} catch (NoSuchClassException e) {
		    ObjectRef obj = ss.makeThrowable(ss.HCclassnotfoundE);
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod getPrimitiveClass(StaticState ss0) {
	final HMethod hm =
	    ss0.HCclass.getMethod("getPrimitiveClass",
				  new HClass[] {ss0.HCstring});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		String name = ss.ref2str((ObjectRef)params[0]);
		if (name.equals("boolean")) return forClass(ss,HClass.Boolean);
		if (name.equals("byte"))    return forClass(ss,HClass.Byte);
		if (name.equals("char"))    return forClass(ss,HClass.Char);
		if (name.equals("double"))  return forClass(ss,HClass.Double);
		if (name.equals("float"))   return forClass(ss,HClass.Float);
		if (name.equals("int"))     return forClass(ss,HClass.Int);
		if (name.equals("long"))    return forClass(ss,HClass.Long);
		if (name.equals("short"))   return forClass(ss,HClass.Short);
		if (name.equals("void"))    return forClass(ss,HClass.Void);
		// oops.  throw exception.
		ObjectRef obj = ss.makeThrowable(ss.HCclassnotfoundE);
		throw new InterpretedThrowable(obj, ss);
	    }
	};
    }
    private static final NativeMethod getName(StaticState ss0) {
	final HMethod hm = ss0.HCclass.getMethod("getName", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		String name = ((HClass)obj.getClosure()).getName();
		return ss.makeString(name);
	    }
	};
    }
    private static final NativeMethod getSuperclass(StaticState ss0) {
	final HMethod hm=ss0.HCclass.getMethod("getSuperclass", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return forClass(ss, hc.getSuperclass());
	    }
	};
    }
    private static final NativeMethod getInterfaces(StaticState ss0) {
	final HMethod hm=ss0.HCclass.getMethod("getInterfaces", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		HClass in[] = hc.getInterfaces();
		ArrayRef af=new ArrayRef(ss,ss.HCclassA, new int[]{in.length});
		for (int i=0; i<in.length; i++)
		    af.update(i, forClass(ss, in[i]));
		return af;
	    }
	};
    }
    private static final NativeMethod getComponentType(StaticState ss0) {
	final HMethod hm = ss0.HCclass.getMethod("getComponentType",
						 new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return forClass(ss, hc.getComponentType());
	    }
	};
    }
    private static final NativeMethod getModifiers(StaticState ss0) {
	final HMethod hm = ss0.HCclass.getMethod("getModifiers",new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return new Integer(hc.getModifiers());
	    }
	};
    }
    private static final NativeMethod newInstance(StaticState ss0) {
	final HMethod hm0 = ss0.HCclass.getMethod("newInstance",new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm0; }
	    private InterpretedThrowable inst(StaticState ss)
		throws InterpretedThrowable {
		ObjectRef obj = ss.makeThrowable(ss.HCinstantiationE);
		return new InterpretedThrowable(obj, ss);
	    }
	    private InterpretedThrowable illacc(StaticState ss)
		throws InterpretedThrowable {
		ObjectRef obj = ss.makeThrowable(ss.HCillegalaccessE);
		return new InterpretedThrowable(obj, ss);
	    }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		try {
		    HClass hc = (HClass) obj.getClosure();
		    if (hc.isInterface() || 
			Modifier.isAbstract(hc.getModifiers()))
			throw inst(ss);
		    HMethod hm = hc.getConstructor(new HClass[0]);
		    int modf = hm.getModifiers();
		    if (Modifier.isPrivate(modf))
			throw illacc(ss);
		    if (!Modifier.isPublic(modf)) {
			// package or protected.
			HClass context = ss.getCaller().getDeclaringClass();
			if (context.getPackage().equals(hc.getPackage()) ||
			    (Modifier.isProtected(modf) &&
			     context.isInstanceOf(hc)))
			    /* this case is okay. */;
			else throw illacc(ss);
		    }
		    obj = new ObjectRef(ss, hc);
		    Method.invoke(ss, hm, new Object[] { obj } );
		    return obj;
		} catch (InterpretedThrowable e) {
		    throw inst(ss);
		}
	    }
	};
    }
    // JDK 1.2 stub.
    private static final NativeMethod newInstance0(StaticState ss0) {
	final HMethod hm = ss0.HCclass.getMethod("newInstance0",new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		return newInstance(ss).invoke(ss, params);
	    }
	};
    }
    private static final NativeMethod isInterface(StaticState ss0) {
	final HMethod hm = ss0.HCclass.getMethod("isInterface", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return new Boolean(hc.isInterface());
	    }
	};
    }
    private static final NativeMethod isArray(StaticState ss0) {
	final HMethod hm = ss0.HCclass.getMethod("isArray", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return new Boolean(hc.isArray());
	    }
	};
    }
    private static final NativeMethod isPrimitive(StaticState ss0) {
	final HMethod hm = ss0.HCclass.getMethod("isPrimitive", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return new Boolean(hc.isPrimitive());
	    }
	};
    }
    // JDK 1.2 only: Class.registerNatives()
    private static final NativeMethod registerNatives(StaticState ss0) {
	final HMethod hm =
	    ss0.HCclass.getMethod("registerNatives",new HClass[0]);
	return new NullNativeMethod(hm);
    }
    // JDK 1.2 only: Class.getClassLoader0()
    private static final NativeMethod getClassLoader0(StaticState ss0) {
	// always return 'null', indicating the boot class loader.
	final HMethod hm =
	    ss0.HCclass.getMethod("getClassLoader0",new HClass[0]);
	return new NullNativeMethod(hm);
    }
}
