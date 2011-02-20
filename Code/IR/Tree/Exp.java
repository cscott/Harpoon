// Exp.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;

import java.util.Collections;
import java.util.Set;

/**
 * <code>Exp</code> objects are expressions which stand for the computation
 * of some value (possibly with side effects).
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: Exp.java,v 1.2 2002-02-25 21:05:31 cananian Exp $
 */
abstract public class Exp extends Tree implements Typed {
    protected Exp(TreeFactory tf, HCodeElement source, int arity) {
	super(tf, source, arity);
    }
  
    /** Build an <code>Exp</code> of this type from the given list of
     *  subexpressions. */
    public final Exp build(ExpList kids) { return build(this.tf, kids); }
    abstract public Exp build(TreeFactory tf, ExpList kids);

    // Typed interface:
    /** Result type. */
    public abstract int type();
    /** Returns <code>true</code> if the expression corresponds to a
     *  64-bit value. */
    public boolean isDoubleWord() { return Type.isDoubleWord(tf, type()); }
    /** Returns <code>true</code> if the expression corresponds to a
     *  floating-point value. */
    public boolean isFloatingPoint() { return Type.isFloatingPoint(type()); }

}


