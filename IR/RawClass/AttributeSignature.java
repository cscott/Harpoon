// AttributeSignature.java, created Wed Mar 27 04:23:21 2002 by cananian
// Copyright (C) 2002 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.RawClass;

/**
 * Classfiles need to carry generic type information in a backwards-compatible
 * way.  This is accomplished by introducing a new "Signature" attribute for
 * classes, methods, and fields.  (GJ)
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AttributeSignature.java,v 1.1.2.1 2002-03-27 21:32:07 cananian Exp $
 * @see Attribute
 * @see ClassFile
 */
public class AttributeSignature extends Attribute {
    /** The value of the <code>signature_index</code> item must be a
	valid index into the <code>constant_pool</code> table.  The
	constant pool entry at that index must be a
	<code>CONSTANT_Utf8_info</code> structure representing the
	string giving the name of the GJ signature for this method,
	field, or class. */
    public int signature_index;
     
    /** Creates a <code>AttributeSignature</code>. */
    AttributeSignature(ClassFile parent, ClassDataInputStream in,
		       int attribute_name_index) throws java.io.IOException {
	super(parent, attribute_name_index);
	long attribute_length = in.read_u4();
	if (attribute_length != 2)
	    throw new ClassDataException("Signature attribute with length "
					 + attribute_length);
	signature_index = in.read_u2();
    }
    /** Constructor. */
    public AttributeSignature(ClassFile parent, int attribute_name_index,
			      int signature_index) {
	super(parent, attribute_name_index);
	this.signature_index = signature_index;
    }
    public long attribute_length() { return 2; }
    
    // convenience
    public ConstantUtf8 signature_index()
    { return (ConstantUtf8) parent.constant_pool[signature_index]; }
    public String signature() { return signature_index().val; }
    
    /** Write to bytecode stream. */
    public void write(ClassDataOutputStream out) throws java.io.IOException {
	out.write_u2(attribute_name_index);
	out.write_u4(attribute_length());
	out.write_u2(signature_index);
    }
    
    /** Pretty-print this attribute structure. 
     *  @param indent the indentation level to use.
     */
    public void print(java.io.PrintWriter pw, int indent) {
	indent(pw, indent, "Signature Attribute: " + 
	       signature() + " {" + signature_index + "}");
    }
}
