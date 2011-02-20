// IIR_FunctionCall.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_FunctionCall</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_FunctionCall.java,v 1.4 1998-10-11 02:37:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_FunctionCall extends IIR_Expression
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_FUNCTION_CALL).
     * @return <code>IR_Kind.IR_FUNCTION_CALL</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_FUNCTION_CALL; }
    //CONSTRUCTOR:
    public IIR_FunctionCall() { }
    //METHODS:  
    public void set_implementation(IIR_SubprogramDeclaration implementation)
    { _implementation = implementation; }
 
    public IIR_SubprogramDeclaration get_implementation()
    { return _implementation; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_SubprogramDeclaration _implementation;
} // END class

