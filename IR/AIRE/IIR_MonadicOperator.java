// IIR_MonadicOperator.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_MonadicOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_MonadicOperator.java,v 1.5 1998-10-11 02:37:20 cananian Exp $
 */

//-----------------------------------------------------------
public abstract class IIR_MonadicOperator extends IIR_Expression
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    
    
    //METHODS:  
    public void set_implementation(IIR_SubprogramDeclaration implementation)
    { _implementation = implementation; }
 
    public IIR_SubprogramDeclaration get_implementation()
    { return _implementation; }
 
    public void set_operand(IIR operand)
    { _operand = operand; }
 
    public IIR get_operand()
    { return _operand; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_SubprogramDeclaration _implementation;
    IIR _operand;
} // END class

