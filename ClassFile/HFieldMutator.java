// HFieldMutator.java, created Mon Jan 10 20:05:22 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * <code>HFieldMutator</code> allows you to change properties of
 * an <code>HField</code>.
 * @see HField#getMutator
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HFieldMutator.java,v 1.1.4.1 2000-01-13 23:47:46 cananian Exp $
 */
public interface HFieldMutator {
    public void addModifiers(int m);
    public void setModifiers(int m);
    public void removeModifiers(int m);

    public void setType(HClass type);
    public void setConstant(Object co);
    public void setSynthetic(boolean isSynthetic);
}
