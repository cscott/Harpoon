// ArrayCopyImplementer.java, created Tue Jan 23 16:26:48 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodMutator;
import harpoon.ClassFile.Linker;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
/**
 * <code>ArrayCopyImplementer</code> adds a pure-java implementation of
 * the <code>System.arraycopy()</code> method.  Our implementation is
 * defined in <code>harpoon.Runtime.ArrayCopy</code>.
 * <p>
 * Arguably, this class should belong in the
 * <code>harpoon.Analysis.Quads</code> package, but we'll leave it
 * here until someone other than the Transactions transformation
 * needs it.
 * <p>
 * When we implement better array bounds check elimination in loops,
 * the version implemented here should have performance equivalent
 * (or better than) the native version.  Better, we should be able
 * to inline this version and eliminate lots of checks in most cases.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayCopyImplementer.java,v 1.5 2003-10-03 19:46:33 cananian Exp $
 */
public class ArrayCopyImplementer extends CachingCodeFactory {
    /** Parent code factory. */
    final HMethod HMimpac;

    /** Creates a <code>ArrayCopyImplementer</code>. */
    public ArrayCopyImplementer(HCodeFactory parent, Linker l) {
	super(parent);
        this.HMimpac = l.forName("harpoon.Runtime.ArrayCopy").getMethod
	    ("arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V");
    }
    public HCode convert(HMethod m) {
	if (m.getName().equals("arraycopy") &&
	    m.getDescriptor().equals
	    ("(Ljava/lang/Object;ILjava/lang/Object;II)V") &&
	    (m.getDeclaringClass().getName().equals("java.lang.System") ||
	     m.getDeclaringClass().getName().equals("java.lang.VMSystem")))
	    try {
		// ensure method is non-native.
		m.getMutator().removeModifiers(Modifier.NATIVE);
		// copy implementation from harpoon.Runtime.ArrayCopy.
		return super.convert(HMimpac).clone(m).hcode();
	    } catch (CloneNotSupportedException e) {
		assert false : e; // shouldn't happen.
	    }
	return super.convert(m);
    }
}
