// MethodGenerator.java, created Fri Jul 11 02:53:45 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Transactions;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HClassMutator;
import harpoon.ClassFile.HMethod;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
/**
 * A <code>MethodGenerator</code> creates methods with base names and
 * parameters/return types you specify.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MethodGenerator.java,v 1.2 2003-07-21 21:21:49 cananian Exp $
 */
class MethodGenerator {
    private final HClassMutator classMutator;
    private static final String separator = "_";

    /** Creates a <code>MethodGenerator</code> which creates methods
     *  in the given <code>HClass</code>. */
    public MethodGenerator(HClass baseClass) {
	this.classMutator = baseClass.getMutator();
    }

    /** Return the 'name' originally given to lookupMethod (which will
     *  differ from the name reported by the method via getName()). */
    public String baseName(HMethod hm) {
	assert generatedMethodSet.contains(hm);
	return descriptors.get(hm).namePrefix;
    }
    public HMethod lookupMethod(String name, HClass[] argTypes, HClass retType)
    {
	MethodDescriptor md = new MethodDescriptor(name, argTypes, retType);
	HMethod hm = methods.get(md);
	if (hm!=null) return hm;
	// add counter to name to guarantee uniqueness.
	hm = classMutator.addDeclaredMethod
	    (name+separator+(counter++), argTypes, retType);
	hm.getMutator().addModifiers(Modifier.FINAL | //Modifier.NATIVE |
				     Modifier.STATIC | Modifier.PUBLIC);
	methods.put(md, hm);
	descriptors.put(hm, md);
	return hm;
    }
    private final Map<MethodDescriptor,HMethod> methods =
	new HashMap<MethodDescriptor,HMethod>();
    private final Map<HMethod,MethodDescriptor> descriptors =
	new HashMap<HMethod,MethodDescriptor>();
    int counter = 0;

    /** The <code>Set</code> of all <code>HMethod</code>s generated
     *  by this <code>MethodGenerator</code>. */
    public final Set<HMethod> generatedMethodSet =
	Collections.unmodifiableSet(descriptors.keySet());

    /** The <code>MethodDescriptor</code> is a handy tuple type to serve
     *  as keys in the hashtable. */
    static class MethodDescriptor {
	final String namePrefix;
	final HClass[] argTypes;
	final HClass retType;
	MethodDescriptor(String namePrefix, HClass[] argTypes, HClass retType){
	    this.namePrefix = namePrefix;
	    this.argTypes = argTypes;
	    this.retType = retType;
	}
	public int hashCode() {
	    int hash = namePrefix.hashCode() + retType.hashCode();
	    hash += argTypes.length;
	    for(int i=0; i<argTypes.length; i++)
		hash += argTypes[i].hashCode();
	    return hash;
	}
	public boolean equals(Object o) {
	    if (this==o) return true;
	    if (!(o instanceof MethodDescriptor)) return false;
	    MethodDescriptor md = (MethodDescriptor) o;
	    if (!this.namePrefix.equals(md.namePrefix)) return false;
	    if (!this.retType.equals(md.retType)) return false;
	    if (this.argTypes.length != md.argTypes.length) return false;
	    for (int i=0; i<this.argTypes.length; i++)
		if (!this.argTypes[i].equals(md.argTypes[i])) return false;
	    return true; // exactly the same.
	}
    }
}
