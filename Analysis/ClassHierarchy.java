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
 * @version $Id: ClassHierarchy.java,v 1.1.2.1 1999-09-08 19:30:06 cananian Exp $
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
     *  @return <code>Set</code> of <code>HClass</code>es.
     */
    public abstract Set classes();
}
