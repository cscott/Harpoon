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
 * @version $Id: HClassMutator.java,v 1.3 2003-03-18 03:52:53 salcianu Exp $
 */
public interface HClassMutator {

    /** Adds a field to the underlying class.  Use
	<code>HFieldMutator</code> to set the modifiers for the added
	field.
	@param name name of the added field
	@param type type of the added field
	@return handle for the added field
	@see HFieldMutator */
    public HField addDeclaredField(String name, HClass type)
	throws DuplicateMemberException;

    /** Adds a field to the underlying class.  Use
	<code>HFieldMutator</code> to set the modifiers for the added
	field.
	@param name name of the added field
	@param descriptor descriptor for the type of the added field,
	formatted as specified in 
	<a href="http://java.sun.com/docs/books/vmspec/html/ClassFile.doc.html#1169">Section 4.3</a> of the JVM specification.
	@return handle for the added field
	@see HFieldMutator */
    public HField addDeclaredField(String name, String descriptor)
	throws DuplicateMemberException;

    /** Adds a new field named <code>name</code> to the underlying
        class.  The type and the modifiers are taken from
        <code>template</code>.  */
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

    /** Adds a method to the underlying <code>HClass</code>.  Use
	<code>HMethodMutator</code> to set the method modifiers
	(public, static etc.)
	@param name name of the added method
	@param descriptor descriptor for the type of
	the added method, as specified
	in <a href="http://java.sun.com/docs/books/vmspec/html/ClassFile.doc.html#1169">Section 4.3</a> of the JVM specification.
	@return handler for the added method
	@see HMethodMutator */
    public HMethod addDeclaredMethod(String name, String descriptor)
	throws DuplicateMemberException;

    /** Adds a method to the underlying <code>HClass</code>.  Later,
	you can use <code>HMethodMutator</code> to set the method
	modifiers (public, static etc.)
	@param name name of the added method
	@param paramTypes parameter types for the added method
	@param returnType return type for the added method
	@return handler for the added method
	@see HMethodMutator */
    public HMethod addDeclaredMethod(String name, HClass[] paramTypes,
				     HClass returnType)
	throws DuplicateMemberException;

    /** Adds a method named <code>name</code> to the underlying
        <code>HClass</code>.  The method type and modifiers are taken
        from <code>template</code>. */
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
