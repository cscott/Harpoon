// ClazNumbering.java, created Sun Mar 10 05:18:51 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.RuntimeTiny;

import harpoon.ClassFile.HClass;

/**
 * A <code>ClazNumbering</code> maps every instantiated
 * <code>HClass</code> in the program to a compact contiguous
 * set of small integers.  Every <code>HClass</code> should
 * have a unique numbering.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClazNumbering.java,v 1.1.2.1 2002-03-11 04:40:51 cananian Exp $
 */
abstract class ClazNumbering {
    /** Returns the number associated with the given <code>HClass</code>. */
    public abstract int clazNumber(HClass hc);
    /** Returns the smallest number which this <code>ClazNumbering</code>
     *  will associate with any <code>HClass</code>. */
    public abstract int minNumber();
    /** Returns the largest number which this <code>ClazNumbering</code>
     *  will associate with any <code>HClass</code>. */
    public abstract int maxNumber();
}
