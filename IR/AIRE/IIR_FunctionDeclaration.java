// IIR_FunctionDeclaration.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_FunctionDeclaration</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FunctionDeclaration.java,v 1.5 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FunctionDeclaration extends IIR_SubprogramDeclaration
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_FUNCTION_DECLARATION).
     * @return <code>IR_Kind.IR_FUNCTION_DECLARATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_FUNCTION_DECLARATION; }
    //CONSTRUCTOR:
    public IIR_FunctionDeclaration() { }
    //METHODS:  
    public void set_pure(IR_Pure purity)
    { _purity = purity; }
 
    public IR_Pure get_pure()
    { return _purity; }
 
    public void set_return_type(IIR_TypeDefinition return_type)
    { _return_type = return_type; }
 
    public IIR_TypeDefinition get_return_type()
    { return _return_type; }
 
    //MEMBERS:  

// PROTECTED:
    IR_Pure _purity;
    IIR_TypeDefinition _return_type;
} // END class

