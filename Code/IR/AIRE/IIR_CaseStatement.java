// IIR_CaseStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatement.java,v 1.5 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CaseStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CASE_STATEMENT).
     * @return <code>IR_Kind.IR_CASE_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CASE_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_CaseStatement() { }
    //METHODS:  
    public void set_expression(IIR expression)
    { _expression = expression; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  
    public IIR_CaseStatementAlternativeList case_statement_alternatives;

// PROTECTED:
    IIR _expression;
} // END class

