// IIR_CaseStatementAlternativeByExpression.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatementAlternativeByExpression</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatementAlternativeByExpression.java,v 1.4 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CaseStatementAlternativeByExpression extends IIR_CaseStatementAlternative
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CASE_STATEMENT_ALTERNATIVE_BY_EXPRESSION).
     * @return <code>IR_Kind.IR_CASE_STATEMENT_ALTERNATIVE_BY_EXPRESSION</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CASE_STATEMENT_ALTERNATIVE_BY_EXPRESSION; }
    //CONSTRUCTOR:
    public IIR_CaseStatementAlternativeByExpression() { }
    //METHODS:  
    public void set_choice(IIR choice)
    { _choice = choice; }
 
    public IIR get_choice()
    { return _choice; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _choice;
} // END class

