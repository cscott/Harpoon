// TreeDerivation.java, created Tue Feb  1 00:35:43 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HClass;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.Maps.TypeMap.TypeNotKnownException;

/**
 * <code>TreeDerivation</code> provides a means to access type and
 * derivation information for any <code>Tree.Exp</code> in a code
 * representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TreeDerivation.java,v 1.2 2002-02-25 21:05:42 cananian Exp $
 * @see harpoon.Analysis.Maps.Derivation
 * @see harpoon.Analysis.Maps.TypeMap
 */
public interface TreeDerivation {
    
    /** Returns the type of a given tree expression <code>exp</code>.
     *  If the type of the <code>Tree.Exp</code> is not known, throws
     *  <code>TypeNotKnownException</code>.  If the <code>Tree.Exp</code>
     *  represents a derived pointer, <code>null</code> is returned, in
     *  which case the <code>derivation()</code> method <b>must</b>
     *  return a non-<code>null</code> value.  <code>HClass.Void</code>
     *  is returned to indicate the type of a null pointer; as a special
     *  case, it is also returned to indicate the type of an opaque
     *  pointer value which does not correspond to a java object pointer
     *  or some derivation thereof and thus should not be traced during
     *  garbage collection.
     * @param exp The tree expression to examine.
     * @return the type of <code>exp</code>
     * @exception TypeNotKnownException if the <code>TreeDerivation</code>
     *  does not have any information about <code>exp</code>.
     * @see harpoon.Analysis.Maps.TypeMap
     */
    public HClass typeMap(Exp exp) throws TypeNotKnownException;
    /** Returns the derivation of a given tree expression <code>exp</code>.
     *  If the derivation of the <code>Tree.Exp</code> is not known,
     *  throws <code>TypeNotKnownException</code>.  If the
     *  <code>Tree.Exp</code> represents a non-derived base pointer,
     *  returns <code>null</code>, in which case the <code>typeMap()</code>
     *  method <b>must</b> return a non-<code>null</code> value.
     * @param exp The tree expression to examine.
     * @return the derivation of <code>exp</code>
     * @exception TypeNotKnownException if the <code>TreeDerivation</code>
     *  does not have any information about <code>exp</code>.
     * @see harpoon.Analysis.Maps.Derivation
     */
    public DList  derivation(Exp exp) throws TypeNotKnownException;
}
