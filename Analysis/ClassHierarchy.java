// ClassHierarchy.java, created Wed Sep  8 14:34:46 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HClass;

import java.util.Set;
/**
 * A <code>ClassHierarchy</code> enumerates reachable/usable classes
 * and methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassHierarchy.java,v 1.1.2.2 1999-10-14 20:22:08 cananian Exp $
 */
public abstract class ClassHierarchy {
    // tree of callable classes
    /** Returns all usable/reachable children of an <code>HClass</code>.
     *	@return <code>Set</code> of <code>HClass</code>es.
     */
    public abstract Set children(HClass c);
    /** Returns the parent of an <code>HClass</code>. */
    public abstract HClass parent(HClass c);

    // other methods.
    /** Returns set of all callable methods. 
     *	@return <code>Set</code> of <code>HMethod</code>s.
     */
    public abstract Set callableMethods();
    /** Returns the set of all reachable/usable classes.
     *  If any method in a class is callable (including static methods),
     *  then the class will be a member of the returned set.
     *  @return <code>Set</code> of <code>HClass</code>es.
     */
    public abstract Set classes();
    /** Returns the set of all *instantiated* classes.
     *  This is a subset of the set returned by the <code>classes()</code>
     *  method.  A class is included in the return set only if an
     *  object of that type is at some point created.
     */
    public abstract Set instantiatedClasses();
}
