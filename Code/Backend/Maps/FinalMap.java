// FinalMap.java, created Sat Jan 16 21:07:48 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Maps;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
/**
 * A <code>FinalMap</code> determines whether a class or method is
 * <code>final</code>.  A simple implementation (for example,
 * <code>DefaultFinalMap</code>) simply looks at the <code>final</code>
 * tag on the class or method.  A more sophisticated implementation
 * (for example, <code>CHFinalMap</code>)
 * takes a particular <code>ClassHierarchy</code> as an argument
 * and returns <code>true</code> if the given class or method is never
 * subclassed/overridden <i>in context</i>, even if it does not have a
 * <code>final</code> access modifier tag.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FinalMap.java,v 1.2 2002-02-25 21:01:58 cananian Exp $ */

public abstract class FinalMap  {
    /** Returns <code>true</code> if the class is never subclassed. */
    public abstract boolean isFinal(HClass hc);
    /** Returns <code>true</code> if the method is never overridden. */
    public abstract boolean isFinal(HMethod hm);
    /** Returns <code>true</code> if the field is never modified after
     *  declaration. */
    public abstract boolean isFinal(HField hf);
}
