// HFieldMutator.java, created Mon Jan 10 20:05:22 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * <code>HFieldMutator</code> allows you to change properties of
 * an <code>HField</code>.
 * @see HField#getMutator
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HFieldMutator.java,v 1.2 2002-02-25 21:03:03 cananian Exp $
 */
public interface HFieldMutator {
    /** Add the modifiers specified by <code>m</code> to the field.
     *  The <code>java.lang.reflect.Modifier</code> class should be used
     *  to encode the modifiers.
     * @see java.lang.reflect.Modifier */
    public void addModifiers(int m);
    /** Set the field's modifiers to those specified by <code>m</code>.
     *  The <code>java.lang.reflect.Modifier</code> class should be used
     *  to encode the modifiers.
     * @see java.lang.reflect.Modifier */
    public void setModifiers(int m);
    /** Remove the modifiers specified by <code>m</code> from the field.
     *  The <code>java.lang.reflect.Modifier</code> class should be used
     *  to encode the modifiers.
     * @see java.lang.reflect.Modifier */
    public void removeModifiers(int m);

    /** Set the type of this field to <code>type</code>. */
    public void setType(HClass type);
    /** Mark this field as constant with the constant value given
     *  by <code>co</code>. */
    public void setConstant(Object co);
    /** Set the 'synthetic' property of this field. */
    public void setSynthetic(boolean isSynthetic);
}
