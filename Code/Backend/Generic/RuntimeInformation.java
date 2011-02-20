// RuntimeInformation.java, created Mon Jan 17 00:51:55 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import java.util.HashSet;
import java.util.Set;
/**
 * <code>RuntimeInformation</code> is an abstract encapsulation of
 * analysis information about native methods as executed by
 * a particular runtime system.  Examples of runtime systems include
 * Sun's <A HREF="http://java.sun.com/products/jdk/1.1/">JDK 1.1</A>,
 * <A HREF="http://java.sun.com/products/jdk/1.2/">JDK1.2</A>, or
 * <A HREF="http://java.sun.com/products/jdk/1.3/">JDK1.3</A>;
 * GNU <A HREF="http://www.classpath.org">classpath</a>, and etc.
 * Note that there may be additional behaviors introduced by the
 * particular <code>Generic.Runtime</code> the code is executed
 * by: native code executed via the
 * <A HREF="http://www.java.sun.com/products/jdk/1.2/docs/guide/jni/index.html">Java Native Interface</A>
 * may have additional behaviors due to the particular implementation
 * of the JNI being used.  JNI implementations are typically tied to
 * a particular JVM; for example Sun's JDKs,
 * <A HREF="http://www.transvirtual.com/">Transvirtual</A>'s
 * <A HREF="http://www.transvirtual.com/products/index.html">Kaffe</A>,
 * or our own 
 * <A HREF="http://lesser-magoo.lcs.mit.edu/~cananian/hypermail/java-dev/0341.html">FLEX native code runtime implementation</A>.
 * <p>
 * <code>Generic.RuntimeInformation</code> class is designed to use
 * inheritance and proxy-ing to intelligently represent common behaviors.
 * Behaviors discovered missing from an implementation should be added
 * to the most general <b>appropriate</b> class (which may be either
 * a superclass of the instance used, or a class proxied by the instance
 * or its superclasses) in order to reduce code duplication.  The
 * root <code>Generic.RuntimeInformation</code> class should always
 * be kept <b>completely abstract</b>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: RuntimeInformation.java,v 1.2 2002-02-25 21:01:28 cananian Exp $
 * @see JDK11RuntimeImplementation
 * @see JDK12RuntimeImplementation
 */
public abstract class RuntimeInformation {
    /** The <code>Linker</code> to use for all non-primitive elements
     *  of the returned <code>Set</code>s. */
    public final Linker linker; // nothing makes sense without one
    protected RuntimeInformation(Linker linker) {
	this.linker = linker;
    }

    /** Returns the set of methods called during the initialization
     *  of the runtime system.  These methods are called <i>before</i>
     *  the "main" method of the program.  Instantiation of any classes
     *  during the runtime system initialization should be indicated
     *  via inclusion of an appropriate <code>HConstructor</code> in
     *  the set of callable methods.
     *  @return a <code>Set</code> of <code>HMethod</code>s.
     */
    public abstract Set initiallyCallableMethods();
    /** Returns the set of methods called during the execution of the
     *  specified method.  The method may or may not be native; a
     *  particular run-time may do something "tricky" during the
     *  execution of an otherwise normal method which can be described
     *  by the results of this call.  The specified method may not
     *  be null.  To indicate that a particular method may cause the
     *  instantiation of one of more classes, appropriate
     *  <code>HConstructor</code>s should be included in the returned
     *  set.
     *  @return a <code>Set</code> of <code>HMethod</code>s.
     */
    public abstract Set methodsCallableFrom(HMethod m);

    /**
     * Returns the set of basic classes which may be referenced
     * in some way by the runtime (for example, as the component type of an
     * array).  This does not include classes which may be
     * <i>instantiated</i> by the runtime, constructors for which
     * ought to be included as callable methods instead of including
     * them in the result set from this method.  The only classes I can
     * think of that ought to be included in this set are the
     * 8 primitive type classes, which are sometimes referenced
     * via reflection (as opposed to instantiation) via the
     * <code>java.lang.Class.forName()</code> method; feel free to 
     * extend your implementation if I happen to be missing some
     * possibilities.
     * @return a <code>Set</code> of <code>HClass</code>es.
     */
    public abstract Set baseClasses();

    /** Convenience method to union two sets, either or both of which
     *  may be unmodifiable.  If either set is modifiable, it adds
     *  the contents of the other to it and returns the modified set.
     *  If neither is modifiable, it creates a new modifiable set and
     *  adds both sets to it before returning it. */
    protected final Set union(Set s1, Set s2) {
	// first try to add elements to s1
	try {
	    s1.addAll(s2); return s1;
	} catch (UnsupportedOperationException e) { /* ignore */ }
	// then try to add elements to s2
	try {
	    s2.addAll(s1); return s2;
	} catch (UnsupportedOperationException e) { /* ignore */ }
	// okay, then make a new set.
	if (s1.size() > s2.size()) {
	    Set s3 = new HashSet(s1); s3.addAll(s2); return s3;
	} else {
	    Set s4 = new HashSet(s2); s4.addAll(s1); return s4;
	}
    }
}
