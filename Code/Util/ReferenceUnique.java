// ReferenceUnique.java, created Sun Sep 12 18:11:40 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

/**
 * <code>ReferenceUnique</code> is a property indicating that, for
 * all instances of a class, <code>(a==b)==(a.equals(b))</code>.
 * That is, two equal objects are always reference equal.
 * <code>HClass</code> and <code>Temp</code> are good examples
 * where this is true; <code>HMethod</code> and <code>String</code>
 * are examples where it is not true.<p>
 * Tagging classes with <code>ReferenceUnique</code> allows
 * automated checkers to more accurately discrimate legitimate
 * uses of <code>==</code> on objects from unsafe uses.
 *
 * @see harpoon.ClassFile.HClass
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ReferenceUnique.java,v 1.2 2002-02-25 21:08:47 cananian Exp $
 */
public interface ReferenceUnique { }
