// INClass.java, created Mon Dec 28 21:24:34 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;

import java.lang.reflect.Modifier;
import java.util.Hashtable;
/**
 * <code>INClass</code> provides implementations of the native methods in
 * <code>java.lang.Class</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INClass.java,v 1.1.2.2 1999-08-04 05:52:35 cananian Exp $
 */
public class INClass extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(forName());
	ss.register(getComponentType());
	ss.register(getInterfaces());
	ss.register(getModifiers());
	ss.register(getName());
	ss.register(getPrimitiveClass());
	ss.register(getSuperclass());
	ss.register(isArray());
	ss.register(isInterface());
	ss.register(isPrimitive());
	ss.register(newInstance());
	// registry for name->class mapping
	ss.putNativeClosure(HCclass, new Hashtable());
    }
    static final ObjectRef forClass(StaticState ss, HClass hc)
	throws InterpretedThrowable {
	Hashtable registry = (Hashtable) ss.getNativeClosure(HCclass);
	ObjectRef obj = (ObjectRef) registry.get(hc);
	if (obj!=null) return obj;
	obj = new ObjectRef(ss, HCclass);
	Method.invoke(ss, HCclass.getConstructor(new HClass[0]),
		      new Object[] { obj } );
	obj.putClosure(hc);
	registry.put(hc, obj);
	return obj;
    }
    private static final NativeMethod forName() {
	final HMethod hm =
	    HCclass.getMethod("forName", new HClass[] { HCstring });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    // throws ClassNotFoundException
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		String clsname = ss.ref2str((ObjectRef)params[0]);
		try {
		    return forClass(ss, HClass.forName(clsname));
		} catch (NoClassDefFoundError e) {
		    ObjectRef obj = ss.makeThrowable(HCclassnotfoundE);
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    private static final NativeMethod getPrimitiveClass() {
	final HMethod hm =
	    HCclass.getMethod("getPrimitiveClass", new HClass[] {HCstring});
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
		ObjectRef obj = ss.makeThrowable(HCclassnotfoundE);
		throw new InterpretedThrowable(obj, ss);
	    }
	};
    }
    private static final NativeMethod getName() {
	final HMethod hm = HCclass.getMethod("getName", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		String name = ((HClass)obj.getClosure()).getName();
		return ss.makeString(name);
	    }
	};
    }
    private static final NativeMethod getSuperclass() {
	final HMethod hm = HCclass.getMethod("getSuperclass", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return forClass(ss, hc.getSuperclass());
	    }
	};
    }
    private static final NativeMethod getInterfaces() {
	final HMethod hm = HCclass.getMethod("getInterfaces", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		HClass in[] = hc.getInterfaces();
		ArrayRef af=new ArrayRef(ss, HCclassA, new int[]{ in.length });
		for (int i=0; i<in.length; i++)
		    af.update(i, forClass(ss, in[i]));
		return af;
	    }
	};
    }
    private static final NativeMethod getComponentType() {
	final HMethod hm = HCclass.getMethod("getComponentType",new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return forClass(ss, hc.getComponentType());
	    }
	};
    }
    private static final NativeMethod getModifiers() {
	final HMethod hm = HCclass.getMethod("getModifiers",new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return new Integer(hc.getModifiers());
	    }
	};
    }
    private static final NativeMethod newInstance() {
	final HMethod hm0 = HCclass.getMethod("newInstance",new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm0; }
	    private InterpretedThrowable inst(StaticState ss)
		throws InterpretedThrowable {
		ObjectRef obj = ss.makeThrowable(HCinstantiationE);
		return new InterpretedThrowable(obj, ss);
	    }
	    private InterpretedThrowable illacc(StaticState ss)
		throws InterpretedThrowable {
		ObjectRef obj = ss.makeThrowable(HCillegalaccessE);
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
		    if (!Modifier.isPublic(hm.getModifiers()))
			throw illacc(ss);
		    obj = new ObjectRef(ss, hc);
		    Method.invoke(ss, hm, new Object[] { obj } );
		    return obj;
		} catch (InterpretedThrowable e) {
		    throw inst(ss);
		}
	    }
	};
    }
    private static final NativeMethod isInterface() {
	final HMethod hm = HCclass.getMethod("isInterface", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return new Boolean(hc.isInterface());
	    }
	};
    }
    private static final NativeMethod isArray() {
	final HMethod hm = HCclass.getMethod("isArray", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return new Boolean(hc.isArray());
	    }
	};
    }
    private static final NativeMethod isPrimitive() {
	final HMethod hm = HCclass.getMethod("isPrimitive", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		HClass hc = (HClass) obj.getClosure();
		return new Boolean(hc.isPrimitive());
	    }
	};
    }
}
