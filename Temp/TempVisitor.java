// TempVisitor.java, created Wed Aug  4 15:00:52 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

/**
   <code>TempVisitor</code> is a visitor class for traversing a set of
   <code>Temp</code>s and performing some action depending on the type
   of <code>Temp</code> visited.  Subclasses should implement a
   <code>visit</code> method for generic <code>Temp</code>s.

   <BR>
   While <code>TempVisitor</code> is of dubious usefulness for the
   majority of the system, it is very helpful for the various backends
   which add classes that extend <code>Temp</code> for representing
   registers, multi-word temps, etc.  Such backends should also extend
   <code>TempVisitor</code>, adding <code>visit</code> methods for
   each additional type of <code>Temp</code> that the backend
   defines.  (It is <B>imperative</B> that backends using such
   extensions of <code>TempVisitor</code> declare them according to
   their extended type and not just as a <code>TempVisitor</code>;
   otherwise the java compiler will resolve calls to
   <code>accept(visitor)</code> to link to 
   <code>Temp.accept(TempVisitor)</code>, which will then call
   <code>visitor.visit(Temp)</code> on <b>all</b> instances of
   <code>Temp</code>, instead of calling the specialized method for
   handling extensions of <code>Temp</code>.  An example of this
   pattern of extending Visitors with new types can be found in
   <code>RegAlloc</code> and its uses of
   <code>RegInstrVisitor</code> to handle internal extensions
   of <code>Instr</code>. ) 

 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: TempVisitor.java,v 1.1.2.1 1999-08-04 19:58:59 pnkfelix Exp $
 */
public abstract class TempVisitor {
    public abstract void visit(Temp t);
    
}
