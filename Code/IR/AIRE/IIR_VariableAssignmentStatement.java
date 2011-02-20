// IIR_VariableAssignmentStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The <code>IIR_VariableAssignmentStatement</code> updates the value
 * of a variable with the value specified in an expression.  Such
 * statements may appear anywhere a sequential statement may appear.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_VariableAssignmentStatement.java,v 1.5 1998-10-11 02:37:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_VariableAssignmentStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_VARIABLE_ASSIGNMENT_STATEMENT).
     * @return <code>IR_Kind.IR_VARIABLE_ASSIGNMENT_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_VARIABLE_ASSIGNMENT_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_VariableAssignmentStatement() { }
    //METHODS:  
    public void set_target(IIR target)
    { _target = target; }
 
    public IIR get_target()
    { return _target; }
 
    public void set_expression(IIR expression)
    { _expression = expression; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _target;
    IIR _expression;
} // END class

