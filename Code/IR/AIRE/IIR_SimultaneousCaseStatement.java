// IIR_SimultaneousCaseStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_SimultaneousCaseStatement</code> class uses
 * an expression of discrete type to select a unique simulataneous
 * statement part for execution.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousCaseStatement.java,v 1.4 1998-10-11 02:37:24 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousCaseStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SIMULTANEOUS_CASE_STATEMENT).
     * @return <code>IR_Kind.IR_SIMULTANEOUS_CASE_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIMULTANEOUS_CASE_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_SimultaneousCaseStatement() { }
    //METHODS:  
    public void set_expression(IIR expression)
    { _expression = expression; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  
    public IIR_SimultaneousAlternativeList simultaneous_alternative_list;

// PROTECTED:
    IIR _expression;
} // END class

