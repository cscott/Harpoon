// ClassPointer.java, created Thu Dec 10 23:40:21 1998 by cananian
package harpoon.ClassFile;

import harpoon.Util.Util;
/**
 * A <code>ClassPointer</code> is a <i>pointer to</i> an HClass, without being
 * the HClass itself.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassPointer.java,v 1.1.2.1 1998-12-11 06:54:50 cananian Exp $
 */
class ClassPointer extends HPointer {
    final String descriptor;
    ClassPointer(String descriptor) {
	Util.assert(descriptor.indexOf('.')==-1); // slashes, not dots.
	// trim descriptor.
	int i;
	for (i=0; i<descriptor.length(); i++) {
	    char c = descriptor.charAt(i);
	    if (c=='[') continue;
	    if (c=='L') i = descriptor.indexOf(';', i);
	    break;
	}
	descriptor = descriptor.substring(0,i+1);
	// assign.
	this.descriptor = descriptor;
    }
    HClass actual() { return HClass.forDescriptor(descriptor); }
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
	else Util.assert(false, "Illegal descriptor.");
	return null; // javac is stupid.
    }
}
