// INFloatDouble.java, created Fri Jan  1 11:29:34 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;

/**
 * <code>INFloatDouble</code> provides implementations of the native methods
 * in <code>java.lang.Float</code> and <code>java.lang.Double</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INFloatDouble.java,v 1.1.2.5 1999-08-07 06:59:53 cananian Exp $
 */
public class INFloatDouble extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(intBitsToFloat());
	ss.register(longBitsToDouble());
	ss.register(floatToIntBits());
	ss.register(doubleToLongBits());
	try {
	ss.register(valueOf0());
	} catch (NoSuchMethodError e) { /* JDK 1.2 */ }
    }
    // convert int to float
    private static final NativeMethod intBitsToFloat() {
	final HMethod hm =
	    HCfloat.getMethod("intBitsToFloat", new HClass[] { HClass.Int });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Integer i = (Integer) params[0];
		return new Float(Float.intBitsToFloat(i.intValue()));
	    }
	};
    }
    // convert float to int
    private static final NativeMethod floatToIntBits() {
	final HMethod hm =
	    HCfloat.getMethod("floatToIntBits", new HClass[] { HClass.Float });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Float f = (Float) params[0];
		return new Integer(Float.floatToIntBits(f.floatValue()));
	    }
	};
    }
    // convert int to double
    private static final NativeMethod longBitsToDouble() {
	final HMethod hm =
	    HCdouble.getMethod("longBitsToDouble", new HClass[]{HClass.Long});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
	        Long l = (Long) params[0];
		return new Double(Double.longBitsToDouble(l.longValue()));
	    }
	};
    }
    // convert double to int
    private static final NativeMethod doubleToLongBits() {
	final HMethod hm =
	    HCdouble.getMethod("doubleToLongBits",new HClass[]{HClass.Double});
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Long(Double.doubleToLongBits(d.doubleValue()));
	    }
	};
    }
    // convert string to a double
    private static final NativeMethod valueOf0() {
	final HMethod hm =
	    HCdouble.getMethod("valueOf0",new HClass[]{ HCstring });
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		return Double.valueOf(ss.ref2str(obj));
	    }
	};
    }
}
