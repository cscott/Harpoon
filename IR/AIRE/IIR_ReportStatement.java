// IIR_ReportStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ReportStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ReportStatement.java,v 1.1 1998-10-10 07:53:42 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ReportStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_REPORT_STATEMENT
    //CONSTRUCTOR:
    public IIR_ReportStatement() { }
    //METHODS:  
    public IIR get_report_expression()
    { return _report_expression; }
 
    public void set_severity_expression(IIR severity_expression)
    { _severity_expression = severity_expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _severity_expression;
} // END class

