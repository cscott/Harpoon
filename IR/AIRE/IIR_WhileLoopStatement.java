// IIR_WhileLoopStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_WhileLoopStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_WhileLoopStatement.java,v 1.6 1998-10-11 02:37:26 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_WhileLoopStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_WHILE_LOOP_STATEMENT).
     * @return <code>IR_Kind.IR_WHILE_LOOP_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_WHILE_LOOP_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_WhileLoopStatement() { }
    //METHODS:  
    public void set_while_condition(IIR while_condition)
    { _while_condition = while_condition; }
 
    public IIR get_while_condition()
    { return _while_condition; }
 
    //MEMBERS:  
    public IIR_SequentialStatementList sequence_of_statements;
    public IIR_DeclarationList loop_declarations;

// PROTECTED:
    IIR _while_condition;
} // END class

