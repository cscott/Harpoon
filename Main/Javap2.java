// Javap.java, created by cananian
// Copyright (C) 2002 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;

import java.lang.reflect.Modifier;

/**
 * <code>Javap2</code> is a clone of the Sun <code>javap</code> tool,
 * using our higher-level bytecode file infrastructure.
 * 
 * @author  C. Scott Ananian <cananian@lesser-magoo.lcs.mit.edu>
 * @version $Id: Javap2.java,v 1.2 2002-04-10 03:06:09 cananian Exp $
 */
public class Javap2 {
    public static void main(String[] args) throws ClassNotFoundException {
	Linker linker = Loader.systemLinker;

	HClass c = linker.forName(args[0]);
	System.out.print(modString(c.getModifiers(), true));
	System.out.print(getTypeName(c));
	// supertypes
	HClass sc = c.getSuperclass();
	if (sc!=null && !sc.getName().equals("java.lang.Object")) {
	    System.out.print(" extends ");
	    System.out.print(getTypeName(sc));
	}
	// interfaces
	HClass in[] = c.getInterfaces();
	if (in.length>0) System.out.print(" implements ");
	for (int i=0; i<in.length; i++) {
	    System.out.print(getTypeName(in[i]));
	    if (i+1<in.length) System.out.print(", ");
	}
	System.out.println();
	System.out.println("{");
	// methods
	HMethod[] m = c.getDeclaredMethods();
	for (int i=0; i<m.length; i++) {
	    System.out.print("    ");
	    System.out.print(modString(m[i].getModifiers(), false));
	    System.out.print(getTypeName(m[i].getReturnType()));
	    System.out.print(" ");
	    System.out.print(m[i].getName());
	    System.out.print("(");
	    // parameters
	    HClass[] p = m[i].getParameterTypes();
	    for (int j=0; j<p.length; j++) {
		System.out.print(getTypeName(p[j]));
		if (j+1<p.length) System.out.print(", ");
	    }
	    System.out.print(")");
	    // exceptions
	    HClass[] e = m[i].getExceptionTypes();
	    if (e.length > 0) System.out.print(" throws ");
	    for (int j=0; j<e.length; j++) {
		System.out.print(getTypeName(e[j]));
		if (j+1<e.length) System.out.print(", ");
	    }
	    System.out.print(";");
	    System.out.println();
	}
	System.out.println("}");
    }
    static String modString(int mods, boolean isClass) {
	StringBuffer sb = new StringBuffer();
	if (Modifier.isPrivate(mods)) sb.append("private ");
	if (Modifier.isProtected(mods)) sb.append("protected ");
	if (Modifier.isPublic(mods)) sb.append("public ");

	if (Modifier.isAbstract(mods) && !Modifier.isInterface(mods))
	    sb.append("abstract ");
	if (Modifier.isFinal(mods)) sb.append("final ");
	if (Modifier.isInterface(mods)) sb.append("interface ");
	else if (isClass) sb.append("class ");
	if (Modifier.isNative(mods)) sb.append("native ");
	if (Modifier.isStatic(mods)) sb.append("static ");
	if (Modifier.isStrict(mods)) sb.append("strict ");
	if (Modifier.isSynchronized(mods)) sb.append("synchronized ");
	if (Modifier.isTransient(mods)) sb.append("transient ");
	if (Modifier.isVolatile(mods)) sb.append("volatile ");
	return sb.toString();
    }
    static String getTypeName(HClass hc) {
	if (hc.isArray()) {
	    StringBuffer r = new StringBuffer();
	    HClass sup = hc;
	    int i=0;
	    for (; sup.isArray(); sup = sup.getComponentType())
		i++;
	    r.append(sup.getName());
	    for (int j=0; j<i; j++)
		r.append("[]");
	    return r.toString();
	}
	return hc.getName();
    }
}
