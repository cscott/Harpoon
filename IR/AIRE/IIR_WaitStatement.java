// IIR_WaitStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The <code>IIR_WaitStatement</code> suspends execution pending a signal
 * event, boolean condition, and/or time out interval.  Such statements
 * may appear almost anywhere a sequential statement may appear
 * (some restrictions in subprograms).
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_WaitStatement.java,v 1.5 1998-10-11 02:37:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_WaitStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_WAIT_STATEMENT).
     * @return <code>IR_Kind.IR_WAIT_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_WAIT_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_WaitStatement() { }
    //METHODS:  
    public void set_condition_clause(IIR condition_clause)
    { _condition_clause = condition_clause; }
    public IIR get_condition_clause()
    { return _condition_clause; }
 
    public void set_timeout_clause(IIR timeout_clause)
    { _timeout_clause = timeout_clause; }
    public IIR get_timeout_clause()
    { return _timeout_clause; }
 
    //MEMBERS:  
    public IIR_DesignatorList sensitivity_list;

// PROTECTED:
    IIR _condition_clause;
    IIR _timeout_clause;
} // END class

