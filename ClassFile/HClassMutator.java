// HClassMutator.java, created Mon Jan 10 16:10:45 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * An <code>HClassMutator</code> allows you to change members and
 * properties of an <code>HClass</code>.
 * @see HClass#getMutator
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassMutator.java,v 1.2 2002-02-25 21:03:03 cananian Exp $
 */
public interface HClassMutator {
    public HField addDeclaredField(String name, HClass type)
	throws DuplicateMemberException;
    public HField addDeclaredField(String name, String descriptor)
	throws DuplicateMemberException;
    public HField addDeclaredField(String name, HField template)
	throws DuplicateMemberException;
    public void removeDeclaredField(HField f)
	throws NoSuchMemberException;

    public HInitializer addClassInitializer()
	throws DuplicateMemberException;
    public void removeClassInitializer(HInitializer m)
	throws NoSuchMemberException;

    public HConstructor addConstructor(String descriptor)
	throws DuplicateMemberException;
    public HConstructor addConstructor(HClass[] paramTypes)
	throws DuplicateMemberException;
    public HConstructor addConstructor(HConstructor template)
	throws DuplicateMemberException;
    public void removeConstructor(HConstructor c)
	throws NoSuchMemberException;

    public HMethod addDeclaredMethod(String name, String descriptor)
	throws DuplicateMemberException;
    public HMethod addDeclaredMethod(String name, HClass[] paramTypes,
				     HClass returnType)
	throws DuplicateMemberException;
    public HMethod addDeclaredMethod(String name, HMethod template)
	throws DuplicateMemberException;
    public void removeDeclaredMethod(HMethod m)
	throws NoSuchMemberException;

    public void addInterface(HClass in);
    public void removeInterface(HClass in)
	throws NoSuchClassException;
    public void removeAllInterfaces();

    public void addModifiers(int m);
    public void setModifiers(int m);
    public void removeModifiers(int m);

    public void setSuperclass(HClass sc);
    public void setSourceFile(String sourcefilename);
}
