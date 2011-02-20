// IIR_CaseStatementAlternativeByOthers.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatementAlternativeByOthers</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatementAlternativeByOthers.java,v 1.4 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CaseStatementAlternativeByOthers extends IIR_CaseStatementAlternative
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS).
     * @return <code>IR_Kind.IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CASE_STATEMENT_ALTERNATIVE_BY_OTHERS; }
    //CONSTRUCTOR:
    public IIR_CaseStatementAlternativeByOthers() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

