// IIR_ElementDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ElementDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ElementDeclaration.java,v 1.4 1998-10-11 02:37:16 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ElementDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ELEMENT_DECLARATION).
     * @return <code>IR_Kind.IR_ELEMENT_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ELEMENT_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_ElementDeclaration() { }
    //METHODS:  
    public void set_subtype(IIR_TypeDefinition subtype)
    { _subtype = subtype; }
 
    public IIR_TypeDefinition get_subtype()
    { return _subtype; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_TypeDefinition _subtype;
} // END class

