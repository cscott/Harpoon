// INClassLoader.java, created Thu Jan 27 22:18:13 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.NoSuchClassException;

/**
 * <code>INClassLoader</code> provides implementations for (some of) the native
 * methods in <code>java.lang.ClassLoader</code> and 
 * <code>java.lang.ClassLoader.NativeLibrary</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INClassLoader.java,v 1.1.2.3 2000-01-30 04:58:39 cananian Exp $
 */
public class INClassLoader {
    static final void register(StaticState ss) {
	// JDK 1.2 only
	try{ss.register(getCallerClassLoader(ss));}catch(NoSuchMethodError e){}
	try { // the following are ClassLoader.NativeLibrary methods.
	try { ss.register(NLload(ss)); } catch (NoSuchMethodError e) {}
	try { ss.register(NLunload(ss)); } catch (NoSuchMethodError e) {}
	} catch (NoSuchClassException e) { /* ignore */ }
    }
    // Returns the caller's class loader
    private static final NativeMethod getCallerClassLoader(StaticState ss0) {
	final HClass hc = ss0.linker.forName("java.lang.ClassLoader");
	final HMethod hm =
	    hc.getMethod("getCallerClassLoader", new HClass[0] );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// always return null.
		return null;
	    }
	};
    }

    // ------------ ClassLoader.NativeLibrary implementations ---------

    static long handle=0; // help make unique non-zero handles
    // Load the named native library.
    private static final NativeMethod NLload(StaticState ss0) {
	final HClass hc = 
	    ss0.linker.forName("java.lang.ClassLoader$NativeLibrary");
	final HMethod hm =
	    hc.getMethod("load", new HClass[] { ss0.HCstring } );
	final HField handleF = hc.getField("handle");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef _this = (ObjectRef) params[0];
		ObjectRef _name = (ObjectRef) params[1];
		String name = ss.ref2str(_name);
		System.err.println("LOADING NATIVE LIBRARY "+name);
		// don't actually load it; just do nothing.
		// we have to set the handle to be non-zero, though.
		_this.update(handleF, new Long(++handle));
		return null;
	    }
	};
    }
    // Unload the named native library.
    private static final NativeMethod NLunload(StaticState ss0) {
	final HClass hc = 
	    ss0.linker.forName("java.lang.ClassLoader$NativeLibrary");
	final HMethod hm =
	    hc.getMethod("unload", new HClass[0] );
	final HField nameF = hc.getField("name");
	final HField handleF = hc.getField("handle");
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		ObjectRef _this = (ObjectRef) params[0];
		ObjectRef _name = (ObjectRef) _this.get(nameF);
		String name = ss.ref2str(_name);
		System.err.println("UNLOADING NATIVE LIBRARY "+name);
		// don't actually unload it; just do nothing.
		// we will clear the handle field to zero, though.
		_this.update(handleF, new Long(0));
		return null;
	    }
	};
    }
}
