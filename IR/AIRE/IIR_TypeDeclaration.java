// IIR_TypeDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_TypeDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_TypeDeclaration.java,v 1.5 1998-10-11 02:37:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_TypeDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_TYPE_DECLARATION).
     * @return <code>IR_Kind.IR_TYPE_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_TYPE_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_TypeDeclaration() { }
    //METHODS:  
    public void set_type(IIR_TypeDefinition type)
    { _type = type; }
 
    public IIR_TypeDefinition get_type()
    { return _type; }
 
    //MEMBERS:  
    public IIR_AttributeSpecificationList attributes;

// PROTECTED:
    IIR_TypeDefinition _type;
} // END class

