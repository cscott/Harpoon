// Typed.java, created Thu Jan 14 18:59:52 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

/**
 * The <code>Typed</code> interface allows access to type information for
 * <code>TEMP</code>, <code>MEM</code>, <code>CONST</code>,
 * <code>OPER</code>, and <code>UNOP</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Typed.java,v 1.1.2.6 1999-08-12 03:37:27 cananian Exp $
 */
public interface Typed  {
    // enumerated constants.
    public int INT=Type.INT;
    public int LONG=Type.LONG;
    public int FLOAT=Type.FLOAT;
    public int DOUBLE=Type.DOUBLE;
    public int POINTER=Type.POINTER;

    /** Returns enumerated constant (INT, LONG, FLOAT, DOUBLE, or POINTER)
     *  corresponding to the type of the expression. */
    public int type();

    /** Returns <code>true</code> if the expression corresponds to a
     *  64-bit value. */
    public boolean isDoubleWord();
    /** Returns <code>true</code> if the expression corresponds to a
     *  floating-point value. */
    public boolean isFloatingPoint();
}
