// IIR_ArrayTypeDefinition.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ArrayTypeDefinition</code> class represents
 * base types containing zero or more instances of the same elemental
 * subtype.  Multi-dimensional arrays and arrays include record (or other
 * elements) may be presented using composite array elements.  For example, 
 * the first dimension of a two-dimensional array would have an element
 * which is itself an array.  Array type definitions denote unconstrained
 * steps in an array definition.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ArrayTypeDefinition.java,v 1.4 1998-10-11 02:37:13 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ArrayTypeDefinition extends IIR_TypeDefinition
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ARRAY_TYPE_DEFINITION).
     * @return <code>IR_Kind.IR_ARRAY_TYPE_DEFINITION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ARRAY_TYPE_DEFINITION; }
    //CONSTRUCTOR:
    /** The constructor method creates a valid array type definition. */
    public IIR_ArrayTypeDefinition() { }
    //METHODS:  
    public void set_index_subtype( IIR_ScalarTypeDefinition index_subtype)
    { _index_subtype = index_subtype; }
 
    public IIR_ScalarTypeDefinition get_index_subtype()
    { return _index_subtype; }
 
    public void set_element_subtype(IIR_TypeDefinition element_subtype)
    { _element_subtype = element_subtype; }
 
    public IIR_TypeDefinition get_element_subtype()
    { return _element_subtype; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_ScalarTypeDefinition _index_subtype;
    IIR_TypeDefinition _element_subtype;
} // END class

