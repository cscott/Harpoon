// INString.java, created Mon Dec 28 21:22:06 1998 by cananian
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;

/**
 * <code>INString</code> provides implementations of the native methods in
 * <code>java.lang.String</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INString.java,v 1.1.2.1 1999-03-27 22:05:08 duncan Exp $
 */
public class INString extends HCLibrary {
    static final void register(StaticState ss) {
	ss.register(intern());
    }
    private static final NativeMethod intern() {
	final HMethod hm = 
	    HCstring.getMethod("intern", new HClass[0]);
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef obj = (ObjectRef) params[0];
		return ss.intern(obj);
	    }
	};
    }
}
