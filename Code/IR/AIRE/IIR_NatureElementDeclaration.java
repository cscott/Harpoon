// IIR_NatureElementDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_NatureElementDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NatureElementDeclaration.java,v 1.5 1998-10-11 02:37:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NatureElementDeclaration extends IIR_Declaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_NATURE_ELEMENT_DECLARATION).
     * @return <code>IR_Kind.IR_NATURE_ELEMENT_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_NATURE_ELEMENT_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_NatureElementDeclaration() { }
    //METHODS:  
    public void set_subnature(IIR_NatureDefinition subnature)
    { _subnature = subnature; }
 
    public IIR_NatureDefinition get_subnature()
    { return _subnature; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_NatureDefinition _subnature;
} // END class

