// IIR_ReturnStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ReturnStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ReturnStatement.java,v 1.4 1998-10-11 02:37:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ReturnStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_RETURN_STATEMENT).
     * @return <code>IR_Kind.IR_RETURN_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_RETURN_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ReturnStatement() { }
    //METHODS:  
    public void set_enclosing_subprogram(IIR_SubprogramDeclaration enclosing_subprogram)
    { _enclosing_subprogram = enclosing_subprogram; }
 
    public IIR_SubprogramDeclaration get_enclosing_subprogram()
    { return _enclosing_subprogram; }
 
    public void set_return_expression(IIR return_expression)
    { _return_expression = return_expression; }
 
    public IIR get_return_expression()
    { return _return_expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_SubprogramDeclaration _enclosing_subprogram;
    IIR _return_expression;
} // END class

