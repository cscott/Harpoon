// IIR_OthersInitialization.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_OthersInitialization</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_OthersInitialization.java,v 1.5 1998-10-11 02:37:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_OthersInitialization extends IIR_Expression
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_OTHERS_INITIALIZATION).
     * @return <code>IR_Kind.IR_OTHERS_INITIALIZATION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_OTHERS_INITIALIZATION; }
    //CONSTRUCTOR:
    public IIR_OthersInitialization() { }
    //METHODS:  
    public void set_expression(IIR v)
    { _expression = v; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _expression;
} // END class

