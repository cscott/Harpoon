// IIR_ReportStatement.java, created by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.AIRE;

/**
 * <code>IIR_ReportStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ReportStatement.java,v 1.5 1998-10-11 02:37:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ReportStatement extends IIR_SequentialStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_REPORT_STATEMENT).
     * @return <code>IR_Kind.IR_REPORT_STATEMENT</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_REPORT_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ReportStatement() { }
    //METHODS:  
    public void set_report_expression(IIR report_expression)
    { _report_expression = report_expression; }
    public IIR get_report_expression()
    { return _report_expression; }
 
    public void set_severity_expression(IIR severity_expression)
    { _severity_expression = severity_expression; }
    public IIR get_severity_expression()
    { return _severity_expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _report_expression;
    IIR _severity_expression;
} // END class

