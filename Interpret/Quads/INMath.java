// INMath.java, created Sat Jan 29 23:01:14 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>INMath</code> provides implementations for (some of) the native
 * methods in <code>java.lang.Math</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INMath.java,v 1.2 2002-02-25 21:05:46 cananian Exp $
 */
public class INMath {
    static final void register(StaticState ss) {
	ss.register(acos(ss));
	ss.register(asin(ss));
	ss.register(atan2(ss));
	ss.register(atan(ss));
	ss.register(ceil(ss));
	ss.register(cos(ss));
	ss.register(exp(ss));
	ss.register(floor(ss));
	ss.register(IEEEremainder(ss));
	ss.register(log(ss));
	ss.register(pow(ss));
	ss.register(rint(ss));
	ss.register(sin(ss));
	ss.register(sqrt(ss));
	ss.register(tan(ss));
    }

    private static final NativeMethod acos(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("acos","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.acos(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod asin(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("asin","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.asin(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod atan2(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("atan2","(DD)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		double a = ((Double) params[0]).doubleValue();
		double b = ((Double) params[1]).doubleValue();
		return new Double(Math.atan2(a,b));
	    }
	};
    }
    private static final NativeMethod atan(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("atan","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.atan(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod ceil(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("ceil","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.ceil(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod cos(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("cos","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.cos(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod exp(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("exp","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.exp(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod floor(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("floor","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.floor(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod IEEEremainder(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("IEEEremainder","(DD)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		double a = ((Double) params[0]).doubleValue();
		double b = ((Double) params[1]).doubleValue();
		return new Double(Math.IEEEremainder(a,b));
	    }
	};
    }
    private static final NativeMethod log(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("log","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.log(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod pow(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("pow","(DD)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		double a = ((Double) params[0]).doubleValue();
		double b = ((Double) params[1]).doubleValue();
		return new Double(Math.pow(a,b));
	    }
	};
    }
    private static final NativeMethod rint(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("rint","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.rint(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod sin(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("sin","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.sin(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod sqrt(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("sqrt","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.sqrt(d.doubleValue()));
	    }
	};
    }
    private static final NativeMethod tan(StaticState ss0) {
	final HMethod hm = ss0.HCmath.getMethod("tan","(D)D");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		Double d = (Double) params[0];
		return new Double(Math.tan(d.doubleValue()));
	    }
	};
    }
}
