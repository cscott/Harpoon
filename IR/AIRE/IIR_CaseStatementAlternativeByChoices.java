// IIR_CaseStatementAlternativeByChoices.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatementAlternativeByChoices</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatementAlternativeByChoices.java,v 1.5 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CaseStatementAlternativeByChoices extends IIR_CaseStatementAlternative
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES).
     * @return <code>IR_Kind.IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CASE_STATEMENT_ALTERNATIVE_BY_CHOICES; }
    //CONSTRUCTOR:
    public IIR_CaseStatementAlternativeByChoices() { }
    //METHODS:  
    //MEMBERS:  
    public IIR_ChoiceList choices;

// PROTECTED:
} // END class

