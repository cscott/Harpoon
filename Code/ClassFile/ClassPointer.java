// ClassPointer.java, created Thu Dec 10 23:40:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import harpoon.Util.Util;
/**
 * A <code>ClassPointer</code> is a <i>pointer to</i> an HClass, without being
 * the HClass itself.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassPointer.java,v 1.4 2002-04-10 03:04:12 cananian Exp $
 */
class ClassPointer extends HPointer implements java.io.Serializable {
    final Linker linker;
    final String descriptor;
    ClassPointer(Linker linker, String descriptor) {
	assert descriptor.indexOf('.')==-1 ; // slashes, not dots.
	// trim descriptor.
	int i;
	for (i=0; i<descriptor.length(); i++) {
	    char c = descriptor.charAt(i);
	    if (c=='[') continue;
	    if (c=='L') i = descriptor.indexOf(';', i);
	    assert c!='(' && c!=')' && c!='/' && c!=';' : descriptor;
	    break;
	}
	descriptor = descriptor.substring(0,i+1);
	// assign.
	this.linker = linker;
	this.descriptor = descriptor;
    }
    ClassPointer(HClass hc) { this(hc.getLinker(), hc.getDescriptor()); }
    HClass actual() { return linker.forDescriptor(descriptor); }
    String getDescriptor() { return descriptor; }
    String getName() {
	// yes, unfortunately we can have HPointers to primitives and arrays.
	char first = descriptor.charAt(0);
	if (first=='L')
	    return descriptor
		.substring(1, descriptor.indexOf(';'))
		.replace('/','.');
	else if (first=='[')
	    return descriptor; // how sun's implementation works.
	else if (first=='Z') return "boolean";
	else if (first=='B') return "byte";
	else if (first=='S') return "short";
	else if (first=='I') return "int";
	else if (first=='J') return "long";
	else if (first=='F') return "float";
	else if (first=='D') return "double";
	else if (first=='C') return "char";
	else if (first=='V') return "void";
	else assert false : "Illegal descriptor.";
	return null; // javac is stupid.
    }
}
