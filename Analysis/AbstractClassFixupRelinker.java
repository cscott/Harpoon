// AbstractClassFixupRelinker.java, created Fri Jul  5 20:25:45 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Relinker;
import harpoon.Util.ArrayIterator;
import harpoon.Util.Collections.UniqueVector;

import java.lang.reflect.Modifier;
import java.util.*;
/**
 * <code>AbstractClassFixupRelinker</code> is an extension of <code>Relinker</code>
 * which fixes up abstract classes so that they implement all the methods
 * of their interfaces (even if this implementation is via an abstract
 * method declaration).  The newer JDK1.4 compiler that Sun provides does
 * not put these method declarations in by default, unlike earlier
 * compilers, and this violates several assumptions made in FLEX.
 * The <code>AbstractClassFixupRelinker</code> remedies the situation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AbstractClassFixupRelinker.java,v 1.3 2002-07-10 18:11:03 cananian Exp $
 */
public class AbstractClassFixupRelinker extends Relinker {
    
    /** Creates a <code>AbstractClassFixupRelinker</code>. */
    public AbstractClassFixupRelinker(Linker linker) {
	super(linker);
    }
    public HClass forDescriptor(String descriptor) {
	HClass hc = super.forDescriptor(descriptor);
	// okay, now scan the class and fix it up.
	if (!hc.isInterface() && !done.contains(hc)) {
	    done.add(hc);// properly handle incidental recursion inside fixup()
	    // we should first fix up the superclass(es), if any.
	    HClass sc = hc.getSuperclass();
	    if (sc!=null) forDescriptor(sc.getDescriptor()); // recurse!
	    // okay, now fix this class up.
	    fixup(hc);
	}
	// done!
	return hc;
    }
    private static Set<HClass> done = new HashSet<HClass>();
    
    private void fixup(HClass hc) {
	for (Iterator<HClass> it=collectInterfaces(hc).iterator();
	     it.hasNext(); )
	    fixupOne(hc, it.next());
    }
    private void fixupOne(HClass hc, HClass anInterface) {
	assert !hc.isInterface();
	assert anInterface.isInterface();
	// get list of interface methods.
	for (Iterator<HMethod> it=new ArrayIterator<HMethod>
		 (anInterface.getDeclaredMethods()); it.hasNext(); ) {
	    HMethod hm = it.next();
	    assert hm.isInterfaceMethod();
	    // okay, look this up as a method of hc.  if it doesn't exist,
	    // create it as a public abstract method.
	    HMethod hmm = hc.getMethod(hm.getName(), hm.getParameterTypes());
	    if (hmm.isInterfaceMethod()) {
		// NOT IMPLEMENTED IN CLASS!  this better be an abstract class:
		assert Modifier.isAbstract(hc.getModifiers()) :
		    "interface method "+hm+" not implemented in non-abstract "+
		    "class "+hc;
		// okay, it's an abstract class, so make an abstract
		// implementation method.
		HMethod nm=hc.getMutator().addDeclaredMethod
		    (hm.getName(), hm.getDescriptor());
		nm.getMutator().setModifiers
		    (Modifier.PUBLIC | Modifier.ABSTRACT);
		nm.getMutator().setExceptionTypes(hm.getExceptionTypes());
		// okay, done with this one.
		System.err.println("INFO: needed to add "+nm);
	    }
	}
    }

    private static Set<HClass> collectInterfaces(HClass hc) {
	UniqueVector<HClass> uv = new UniqueVector<HClass>();
	for (Iterator<HClass> it=new ArrayIterator<HClass>(hc.getInterfaces());
	     it.hasNext(); )
	    uv.add(it.next());
	// okay, uv now has the basic interfaces.  we just have to
	// go through it from beginning to end to add all superinterfaces.
	for (int i=0; i<uv.size(); i++)
	    for (Iterator<HClass> it=new ArrayIterator<HClass>
		     (uv.get(i).getInterfaces()); it.hasNext(); )
		uv.add(it.next());
	// done!
	return uv;
    }
}
