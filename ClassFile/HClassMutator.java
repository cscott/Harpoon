// HClassMutator.java, created Mon Jan 10 16:10:45 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * An <code>HClassMutator</code> allows you to change members and
 * properties of an <code>HClass</code>.
 * @see HClass.getMutator()
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassMutator.java,v 1.1.2.1 2000-01-10 21:50:38 cananian Exp $
 */
public interface HClassMutator {
    public HField addDeclaredField(String name, HClass type);
    public HField addDeclaredField(String name, String descriptor);
    public HField addDeclaredField(HField template, boolean shouldRename);
    public void removeDeclaredField(HField f) throws NoSuchFieldError;

    public HMethod addDeclaredMethod(String name, String descriptor);
    public HMethod addDeclaredMethod(String name, HClass[] paramTypes,
				  HClass returnType);
    public HMethod addDeclaredMethod(HMethod template, boolean shouldRename);
    public void removeDeclaredMethod(HField f) throws NoSuchFieldError;

    public HInitializer addClassInitializer();
    public void removeClassInitializer(HInitializer m);

    public void addInterface(HClass in);
    public void removeInterface(HClass in);
    public void removeAllInterfaces();

    public void setModifiers(int m);
    public void setSuperclass(HClass sc);
    public void setSourceFile(String sourcefilename);
}
