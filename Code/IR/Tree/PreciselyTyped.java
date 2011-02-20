// PreciselyTyped.java, created Wed Aug 11 16:03:47 1999 by duncan
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * The <code>PreciselyTyped</code> interface allows access to type 
 * information for expressions which have a type which cannot be expressed
 * by the standard types in the <code>Typed</code> interface. 
 * Only <code>CONST</code>, <code>MEM</code>, and <code>ESEQ</code> implement
 * <code>PreciselyTyped</code>.  All other <code>Tree.Exp</code>s deal
 * with register-native types.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: PreciselyTyped.java,v 1.2 2002-02-25 21:05:40 cananian Exp $
 */
public interface PreciselyTyped extends Typed  {
    /** Returns <code>true</code> if this expression is a small type,
     *  <code>false</code> otherwise. */
    public boolean isSmall();

    /** Returns the size of the expression, in bits.
     *  Only valid if the <code>isSmall()==true</code>. */
    public int     bitwidth();
    
    /** Returns true if this is a signed expression, false otherwise.
     *  Only valid if the <code>isSmall()==true</code>. */
    public boolean signed();
}
