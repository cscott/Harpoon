// IIR_ConcurrentAssertionStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ConcurrentAssertionStatement</code> represents
 * a process containing a sequential assertion statement and a wait
 * statement.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentAssertionStatement.java,v 1.5 1998-10-11 02:37:14 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentAssertionStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CONCURRENT_ASSERTION_STATEMENT).
     * @return <code>IR_Kind.IR_CONCURRENT_ASSERTION_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONCURRENT_ASSERTION_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ConcurrentAssertionStatement() { }
    //METHODS:  
    public void set_postponed(boolean predicate)
    { _postponed = predicate; }
    public boolean get_postponed()
    { return _postponed; }
 
    public void set_assertion_condition(IIR condition)
    { _assertion_condition = condition; }
    public IIR get_assertion_condition()
    { return _assertion_condition; }

    public void set_report_expression(IIR expression)
    { _report_expression = expression; }
    public IIR get_report_expression()
    { return _report_expression; }
 
    public void set_severity_expression(IIR expression)
    { _severity_expression = expression; }
    public IIR get_severity_expression()
    { return _severity_expression; }
    //MEMBERS:  

// PROTECTED:
    boolean _postponed;
    IIR _assertion_condition;
    IIR _report_expression;
    IIR _severity_expression;
} // END class

