// RuntimeMethodCloner.java, created Thu Nov  1 17:45:29 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Counters;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HMethodMutator;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
import harpoon.Util.Collections.GenericInvertibleMultiMap;
import harpoon.Util.Collections.InvertibleMultiMap;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <code>RuntimeMethodCloner</code> creates 'shadow' copies of all
 * non-virtual methods called in <code>harpoon.Runtime.Counters</code>
 * and places them in the <code>Counters</code> class.  That way, we
 * can easily *not* add statistics to stuff in 
 * <code>harpoon.Runtime.Counters</code> to avoid modifying the
 * statistics as we're attempting to report them.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: RuntimeMethodCloner.java,v 1.1.2.1 2001-11-02 04:53:59 cananian Exp $
 */
public class RuntimeMethodCloner extends MethodMutator {
    private static final String classname = "harpoon.Runtime.Counters";
    // map old method names to the new methods they now will be.
    final Map old2new;
    // methods we'd like to relocate but can't.
    final Set badboys = new HashSet();
    // the linker.
    final Linker linker;
    
    /** Creates a <code>RuntimeMethodCloner</code>. */
    public RuntimeMethodCloner(final HCodeFactory parent, Linker linker) {
	this(parent, linker, new GenericInvertibleMultiMap());
    }
    private RuntimeMethodCloner(final HCodeFactory parent, Linker linker,
				// i'd like new2old to be an InvertibleMap,
				// but the type declarations aren't cooperating
				final InvertibleMultiMap new2old) {
        super(new HCodeFactory() {
		public void clear(HMethod m) { parent.clear(m); }
		public String getCodeName() { return parent.getCodeName(); }
		public HCode convert(HMethod m) {
		    if (!new2old.containsKey(m))
			return parent.convert(m);
		    // this is a new one for us.
		    HMethod old = (HMethod) new2old.get(m);
		    HCode hc = convert(old);
		    try {
			return hc.clone(m).hcode();
		    } catch (CloneNotSupportedException ex) {
			// XXX: we don't allow this.
			throw new RuntimeException(ex.toString());
		    }
		}
	    });
	this.linker = linker;
	this.old2new = new2old.invert();
    }
    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	// unless this belongs to harpoon.Runtime.Counters, we ignore it.
	if (!hc.getMethod().getDeclaringClass().getName().equals(classname))
	    return hc;
	// otherwise, we look for appropriate calls and rewrite them.
	HCodeElement[] el = hc.getElements();
	for (int i=0; i<el.length; i++)
	    if (el[i] instanceof CALL) {
		CALL call = (CALL) el[i];
		// do we want to relocate calls to this method?
		if (!shouldRelocate(call.method()))
		    continue;
		// can we?  [we will complain about virtual calls]
		// (note that Nonvirtualize is not invoked on methods
		//  in the Counters class, so we have to do our own
		//  final method nonvirtualization.)
		if (call.isVirtual() && !isFinal(call.method())) {
		    if (!badboys.contains(call.method()))
			System.err.println("WARNING: can't relocate "+
					   call.method()+" at "+
					   call.getSourceFile()+":"+
					   call.getLineNumber());
		    badboys.add(call.method());
		    continue;
		}
		// okay, relocate this one.
		HMethod nm = makeNewMethod(call.method());
		Quad.replace(call, new CALL(call.getFactory(), call, nm,
					    call.params(), call.retval(),
					    call.retex(), false/*non-virtual*/,
					    call.isTailCall(),
					    call.dst(), call.src()));
		// whoo-hoo! on to the next!
	    }
	// done!
	return hc;
    }
    // check whether method or class is final.
    private static boolean isFinal(HMethod hm) {
	return Modifier.isFinal(hm.getModifiers()) ||
	    Modifier.isFinal(hm.getDeclaringClass().getModifiers());
    }
    // a variety of empirical criterial for relocating a method.
    private boolean shouldRelocate(HMethod hm) {
	// don't relocate ourselves!
	if (hm.getDeclaringClass().getName().equals(classname))
	    return false;
	// native methods are safe to skip.
	if (Modifier.isNative(hm.getModifiers())) return false;
	// let's assume exceptions never occur.
	if (hm.getDeclaringClass().isInstanceOf
	    (linker.forName("java.lang.Throwable"))) return false;
	// okay, we'll relocate everything else.
	return true;
    }
    // create new method of harpoon.Runtime.Counters for given method.
    private HMethod makeNewMethod(HMethod hm) {
	if (!old2new.containsKey(hm)) {
	    HClass hc = linker.forName(classname);
	    // come up w/ uniq name for new method.
	    String name = "XXX."+
		hm.getDeclaringClass().getName()+".."+hm.getName();
	    name = replace(name, "_", "_1");
	    name = replace(name, ".", "_");
	    // come up with new descriptor.
	    String desc = hm.getDescriptor();
	    if (!hm.isStatic()) {
		// descriptor for original declaring class.
		String newpart = hm.getDeclaringClass().getDescriptor();
		// insert it after open paren.
		desc = replace(desc, "(", "("+newpart);
	    }
	    // create method.
	    HMethod newm = hc.getMutator().addDeclaredMethod(name, desc);
	    // make it static (and public, just for kicks)
	    newm.getMutator().addModifiers(Modifier.STATIC|Modifier.PUBLIC);
	    // done! add it to the cache.
	    old2new.put(hm, newm);
	}
	return (HMethod) old2new.get(hm);
    }
    // mutate
    private HCode makeCloned(HMethod hm) {
	return null;
    }
    private static String replace(String s, String oldstr, String newstr) {
	StringBuffer sb = new StringBuffer();
	while (true) {
	    // find oldstr
	    int idx = s.indexOf(oldstr);
	    // if not found, then done.
	    if (idx<0) break;
	    // split at idx
	    sb.append(s.substring(0, idx));
	    s = s.substring(idx+oldstr.length());
	    // add newstr.
	    sb.append(newstr);
	}
	sb.append(s);
	return sb.toString();
    }
}

