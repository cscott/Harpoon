// INObject.java, created Thu Dec 31 17:25:09 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.*;

/**
 * <code>INObject</code> provides implementations of the native methods in
 * <code>java.lang.Object</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INObject.java,v 1.1.2.1 1999-01-03 03:01:43 cananian Exp $
 */
public class INObject extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(_getClass_());
	ss.register(_hashCode_());
	ss.register(_clone_());
    }
    // Object.getClass()
    private static final NativeMethod _getClass_() {
	final HMethod hm = HCobject.getMethod("getClass", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
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
	       ObjectRef obj = (ObjectRef) params[0];
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
		ObjectRef obj = (ObjectRef) params[0];
		// throw exception if doesn't implement Cloneable
	        if (!obj.type.isInstanceOf(HCcloneable)) {
		    obj = ss.makeThrowable(HCclonenotsupportedE,
					 obj.type.toString());
		  throw new InterpretedThrowable(obj, ss);
		}
		return obj.clone();
	    }
	};
    }
}
