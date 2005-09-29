// AbstractClassFixupRelinker.java, created Fri Jul  5 20:25:45 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HInitializer;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Relinker;
import harpoon.Util.ArrayIterator;
import net.cscott.jutil.UniqueVector;
import net.cscott.jutil.WorkSet;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
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
 * @version $Id: AbstractClassFixupRelinker.java,v 1.12 2005-09-29 04:05:18 salcianu Exp $
 */
public class AbstractClassFixupRelinker extends Relinker {
    
    /** Creates a <code>AbstractClassFixupRelinker</code>. */
    public AbstractClassFixupRelinker(Linker linker) {
	super(linker);
    }

    public HClass forDescriptor(String descriptor) {
	// We want to make sure that Linker.forDescriptor completes
	// entirely (entering the new class in the descriptor cache)
	// before we invoke it again.  HOWEVER, Linker.forDescriptor
	// will call *us* recursively e.g. to resolve arrays.  So it's
	// not safe to do our resolve() stuff (which will indirectly
	// cause further calls to Linker.forDescriptor) until we're
	// sure the top-level call to Linker.forDescriptor has finished.
	// The 'depth' variable here basically just keeps track of
	// whether this is a recursive invocation via Linker.forDescriptor
	// or a 'top-level' one; we only resolve after top-level calls.
	depth++;
	HClass hc = super.forDescriptor(descriptor);
	depth--;
	deferred.add(hc);
	if (depth==0) // this is a top-level call, it's safe to resolve.
	    while (!deferred.isEmpty())
		resolve(deferred.removeFirst());
	return hc;
    }
    /** Set of classes waiting to be fixed up. */
    WorkSet<HClass> deferred = new WorkSet<HClass>();
    /** Current level of recursion. */
    int depth=0;

    public HClass resolve(HClass hc) {
	// okay, now scan the class and fix it up.
	if (!hc.isInterface() && !done.contains(hc)) {
	    done.add(hc);// properly handle incidental recursion inside fixup()
	    // we should first fix up the superclass(es), if any.
	    HClass sc = hc.getSuperclass();
	    if (sc!=null) resolve(sc); // recurse!
	    // okay, now fix this class up.
	    fixup(hc);

	    // and to prevent fixup from happening at inopportune times
	    // (such as after we start modifying method signatures)
	    // we're going to be aggressive in fixing up other classes
	    // mentioned in fields and methods of this one.

	    // fix up classes mentioned in method signatures
	    for (HMethod hm : hc.getDeclaredMethods()) {
		resolve(hm.getReturnType());
		for (Iterator<HClass> it2=new ArrayIterator<HClass>
			 (hm.getParameterTypes()); it2.hasNext(); )
		    resolve(it2.next());
	    }
	    // and those in field signatures.
	    for (Iterator<HField> it=new ArrayIterator<HField>
		     (hc.getDeclaredFields()); it.hasNext(); )
		resolve(it.next().getType());
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
	for (HMethod hm : anInterface.getDeclaredMethods()) {
	    // could be a static initializer of the interface.
	    if (hm instanceof HInitializer) continue;
	    // otherwise this should be an interface method.
	    assert hm.isInterfaceMethod();
	    // okay, look this up as a method of hc.  if it doesn't exist,
	    // create it as a public abstract method.
	    HMethod hmm = hc.getMethod(hm.getName(), hm.getParameterTypes());
	    if (hmm.isInterfaceMethod()) {
		// NOT IMPLEMENTED IN CLASS!  this better be an abstract class:
		assert Modifier.isAbstract(hc.getModifiers()) :
		    "interface method "+hm+" not implemented in non-abstract "+
		    "class "+hc + "; hmm=" + hmm;
		// okay, it's an abstract class, so make an abstract
		// implementation method.
		HMethod nm=hc.getMutator().addDeclaredMethod
		    (hm.getName(), hm.getDescriptor());
		nm.getMutator().setModifiers
		    (Modifier.PUBLIC | Modifier.ABSTRACT);
		nm.getMutator().setExceptionTypes(hm.getExceptionTypes());
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
