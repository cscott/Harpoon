// INFloatDouble.java, created Fri Jan  1 11:29:34 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;

/**
 * <code>INFloatDouble</code> provides implementations of the native methods
 * in <code>java.lang.Float</code> and <code>java.lang.Double</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INFloatDouble.java,v 1.2 2002-02-25 21:05:53 cananian Exp $
 */
public class INFloatDouble {
    static final void register(StaticState ss) {
	ss.register(intBitsToFloat(ss));
	ss.register(longBitsToDouble(ss));
	ss.register(floatToIntBits(ss));
	ss.register(doubleToLongBits(ss));
	try {
	ss.register(valueOf0(ss));
	} catch (NoSuchMethodError e) { /* JDK 1.2 */ }
    }
    // convert int to float
    private static final NativeMethod intBitsToFloat(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfloat.getMethod("intBitsToFloat",new HClass[]{ HClass.Int });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Integer i = (Integer) params[0];
		return new Float(Float.intBitsToFloat(i.intValue()));
	    }
	};
    }
    // convert float to int
    private static final NativeMethod floatToIntBits(StaticState ss0) {
	final HMethod hm =
	    ss0.HCfloat.getMethod("floatToIntBits",
				  new HClass[] { HClass.Float });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Float f = (Float) params[0];
		return new Integer(Float.floatToIntBits(f.floatValue()));
	    }
	};
    }
    // convert int to double
    private static final NativeMethod longBitsToDouble(StaticState ss0) {
	final HMethod hm =
	    ss0.HCdouble.getMethod("longBitsToDouble",
				   new HClass[] { HClass.Long });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
	        Long l = (Long) params[0];
		return new Double(Double.longBitsToDouble(l.longValue()));
	    }
	};
    }
    // convert double to int
    private static final NativeMethod doubleToLongBits(StaticState ss0) {
	final HMethod hm =
	    ss0.HCdouble.getMethod("doubleToLongBits",
				   new HClass[] { HClass.Double });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Long(Double.doubleToLongBits(d.doubleValue()));
	    }
	};
    }
    // convert string to a double
    private static final NativeMethod valueOf0(StaticState ss0) {
	final HMethod hm =
	    ss0.HCdouble.getMethod("valueOf0",new HClass[]{ ss0.HCstring });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		return Double.valueOf(ss.ref2str(obj));
	    }
	};
    }
}
