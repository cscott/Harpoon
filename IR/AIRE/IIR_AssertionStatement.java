// IIR_AssertionStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AssertionStatement</code>
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AssertionStatement.java,v 1.2 1998-10-10 09:21:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AssertionStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ASSERTION_STATEMENT
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

