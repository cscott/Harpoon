// IIR_ConcurrentAssertionStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ConcurrentAssertionStatement</code> represents
 * a process containing a sequential assertion statement and a wait
 * statement.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentAssertionStatement.java,v 1.3 1998-10-11 00:32:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentAssertionStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
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

