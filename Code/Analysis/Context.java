// Context.java, created Tue Feb 26 02:59:44 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import java.util.Collections;
import java.util.List;
// USED IN Analysis/SizeOpt/BitWidthAnalysis.java
//  and    Analysis/PointsToAnalysis.java
/**
 * A <code>Context</code> object is an opaque representation of a
 * method's <i>calling context</i>, intended to make it easier to
 * generalize across context-sensitive and context-insensitive
 * analyses.  The <code>Context</code> consists of the last <i>n</i>
 * "objects" (call-sites or methods).  Only two operations are
 * defined: we can make a new (empty) context (with a factory; see for
 * example the <code>GenericContextFactory</code> class), and we can
 * create a new context by adding another element on to the end of an
 * existing context.  Context-insensitive analyses have a singleton
 * <code>Context</code> object, which is returned unchanged by the
 * <code>addElement()</code> call. Usually a <i>n</i>-level context
 * will be created, where <code>Context</code>s are represented by an
 * <i>n</i>-tuple holding the last <i>n</i> items given to
 * <code>addElement()</code> (last <i>n</i> method calls, call-sites,
 * etc).  Behind the scenes, it will often be desirable to keep a
 * context cache to avoid creating more than one live object
 * representing a given context.  <p>
 *
 * Note that, for context-sensitive analyses, there remain two main
 * flavors of contexts: <i>caller-based</i>, and
 * <i>call-site-based</i>.  A caller-based <code>Context</code> will
 * pass an <code>HMethod</code> to <code>addElement()</code>, and will
 * group together all call-sites in this method which share the same
 * destination.  A call-site based <code>Context</code> will pass a
 * <code>Quads.CALL</code> (or similar IR object) to
 * <code>addElement()</code>, to distinguish different call-sites
 * leaving to the same destination method.  <p>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Context.java,v 1.2 2003-06-17 16:44:56 cananian Exp $
 * @see GenericContextFactory
 */
public abstract class Context<E> {
    protected Context() { }
    
    /** Add a new element to the end of this <code>Context</code>,
     *  'forgetting' about elements at the start of the <code>Context</code>
     *  if necessary.  For context-insensitivity, this method should
     *  return <code>this</code> unmodified.  Context-sensitive
     *  analyses may wish to implement a context cache.  Or not.
     *  Depends on how long you're planning on keeping those
     *  <code>Context</code> objects around, don't it. */
    public abstract Context<E> addElement(E o);

    /** Return the elements of this <code>Context</code> as a tuple,
     *  as represented by an unmodifiable <code>List</code>.
     *  This is to support interoperability between different
     *  <code>Context</code> implementations.  The object passed to
     *  the <code>allElement()</code> call which created this
     *  <code>Context</code> will be the last element of the
     *  returned <code>List</code>.
     *  <p>
     *  The length of this <code>List</code> will usually be bounded by
     *  a small constant. */
    public abstract List<E> asList();

    // for convenience and hashmap good behaviour.
    /** @return <code>asList().hashCode()</code> */
    public int hashCode() { return asList().hashCode(); }
    /** @return <code>true</code> iff the <code>asList()</code>
     *  representations of the two <code>Context</code>s are
     *  equal. */
    public boolean equals(Object o) {
	if (this==o) return true; // efficiency!
	if (!(o instanceof Context)) return false;
	return asList().equals(((Context)o).asList());
    }
    /** @return <code>asList().toString()</code> */
    public String toString() { return asList().toString(); }

    // some common cases.
    // XXX bivariant.
    public final static Context CONTEXT_INSENSITIVE = new Context() {
	    public Context addElement(Object o) { return this; }
	    public List asList() { return Collections.EMPTY_LIST; }
	};
    // XXX empty list should be bivariant; adding something to it
    // should make the context invariant.
    public final static Context CONTEXT_SENSITIVE_1 =
	new GenericContextFactory(1, false).makeEmptyContext();
    public final static Context CONTEXT_SENSITIVE_2 =
	new GenericContextFactory(2, false).makeEmptyContext();
}
