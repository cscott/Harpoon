// INClassLoader.java, created Thu Jan 27 22:18:13 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Loader;
import harpoon.ClassFile.NoSuchClassException;

import java.io.InputStream;

/**
 * <code>INClassLoader</code> provides implementations for (some of) the native
 * methods in <code>java.lang.ClassLoader</code> and 
 * <code>java.lang.ClassLoader.NativeLibrary</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INClassLoader.java,v 1.2 2002-02-25 21:05:45 cananian Exp $
 */
public class INClassLoader {
    static final void register(StaticState ss) {
	try{ ss.register(findSystemClass0(ss)); } catch (NoSuchMethodError e){}
	/*try*/{ ss.register(getSystemResourceAsStream0(ss)); }/* catch (NoSuchMethodError e) {}*/
	try{ ss.register(init(ss)); } catch (NoSuchMethodError e) {}
	// JDK 1.2 only
	try{ss.register(getCallerClassLoader(ss));}catch(NoSuchMethodError e){}
	try { // the following are ClassLoader.NativeLibrary methods.
	try { ss.register(NLload(ss)); } catch (NoSuchMethodError e) {}
	try { ss.register(NLunload(ss)); } catch (NoSuchMethodError e) {}
	} catch (NoSuchClassException e) { /* ignore */ }
    }
    // ClassLoader.findSystemClass0 behaves (in our implementation) exactly
    // like Class.forName
    private static final NativeMethod findSystemClass0(StaticState ss0) {
	final HClass hc = ss0.linker.forName("java.lang.ClassLoader");
	final HMethod hm =
	    hc.getMethod("findSystemClass0", new HClass[] { ss0.HCstring } );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    // implementation borrowed from INClass.forName
	    // throws ClassNotFoundException
	    Object invoke(StaticState ss, Object[] params)
		throws InterpretedThrowable {
		// params[0] is 'this' because findSystemClass is non-static
		String clsname = ss.ref2str((ObjectRef)params[1]);
		try {
		    return INClass.forClass(ss, ss.linker.forName(clsname));
		} catch (NoSuchClassException e) {
		    ObjectRef obj = ss.makeThrowable(ss.HCclassnotfoundE);
		    throw new InterpretedThrowable(obj, ss);
		}
	    }
	};
    }
    // getSystemResourceAsStream0 is identical in functionality to
    // Loader.getResourceAsStream().  But we must wrap the result.
    private static final NativeMethod getSystemResourceAsStream0(StaticState ss0) {
	final HClass hc = ss0.linker.forName("java.lang.ClassLoader");
	final HMethod hm =
	    hc.getMethod("getSystemResourceAsStream0", new HClass[] { ss0.HCstring } );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		String resname = ss.ref2str((ObjectRef)params[0]);
		InputStream is = Loader.getResourceAsStream(resname);
		if (is==null) return null; // unsuccessful.
		// okay, now create new 'interpreted' FileInputStream from
		// the real one.
		return INFileInputStream.openInputStream(ss, is);
	    }
	};
    }
    // do-nothing 'init' method (initialization of class loader)
    private static final NativeMethod init(StaticState ss0) {
	final HClass hc = ss0.linker.forName("java.lang.ClassLoader");
	final HMethod hm =
	    hc.getMethod("init", new HClass[0] );
	return new NativeMethod() {
	    HMethod getMethod() { return hm; }
	    Object invoke(StaticState ss, Object[] params) {
		// do nothing.
		return null;
	    }
	};
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
