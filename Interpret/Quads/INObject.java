// INObject.java, created Thu Dec 31 17:25:09 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;

/**
 * <code>INObject</code> provides implementations of the native methods in
 * <code>java.lang.Object</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INObject.java,v 1.1.2.5 1999-08-07 06:59:53 cananian Exp $
 */
public class INObject extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(_getClass_());
	ss.register(_hashCode_());
	ss.register(_clone_());
	// JDK 1.2 only
	try { ss.register(registerNatives()); } catch (NoSuchMethodError e) { }
    }
    // Object.getClass()
    private static final NativeMethod _getClass_() {
	final HMethod hm = HCobject.getMethod("getClass", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Ref obj = (Ref) params[0];
		return INClass.forClass(ss, obj.type);
	    }
	};
    }
    // Object.hashCode()
    private static final NativeMethod _hashCode_() {
	final HMethod hm = HCobject.getMethod("hashCode", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Ref obj = (Ref) params[0];
		return new Integer(obj.hashCode());
	    }
	};
    }
    // Object.clone()
    private static final NativeMethod _clone_() {
	final HMethod hm = HCobject.getMethod("clone", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Ref obj = (Ref) params[0];
		// throw exception if doesn't implement Cloneable
	        if (!obj.type.isInstanceOf(HCcloneable)) {
		    obj = ss.makeThrowable(HCclonenotsupportedE,
					 obj.type.toString());
		    throw new InterpretedThrowable((ObjectRef)obj, ss);
		}
		return obj.clone();
	    }
	};
    }
    // JDK 1.2 only: Object.registerNatives()
    private static final NativeMethod registerNatives() {
	final HMethod hm =
	    HCobject.getMethod("registerNatives",new HClass[0]);
	return new NullNativeMethod(hm);
    }
}
