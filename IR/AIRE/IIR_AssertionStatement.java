// IIR_AssertionStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_AssertionStatement</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AssertionStatement.java,v 1.5 1998-10-11 02:37:13 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AssertionStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ASSERTION_STATEMENT).
     * @return <code>IR_Kind.IR_ASSERTION_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ASSERTION_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_AssertionStatement(){}
    //METHODS:  
    public void set_assertion_condition( IIR assertion_condition){
	_assertion_condition = assertion_condition;
    }
    public IIR get_assertion_condition()
    { return _assertion_condition;}

    public void set_report_expression( IIR report_expression){
	_report_expression = report_expression;
    }
    public IIR get_report_expression()
    { return _report_expression;}

    public void set_severity_expression( IIR severity_expression){
	_severity_expression = severity_expression;
    }
    public IIR get_severity_expression( )
    { return _severity_expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _assertion_condition;
    IIR _report_expression;
    IIR _severity_expression;
} // END class

