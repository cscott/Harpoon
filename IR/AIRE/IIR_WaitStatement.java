// IIR_WaitStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The <code>IIR_WaitStatement</code> suspends execution pending a signal
 * event, boolean condition, and/or time out interval.  Such statements
 * may appear almost anywhere a sequential statement may appear
 * (some restrictions in subprograms).
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_WaitStatement.java,v 1.3 1998-10-11 00:32:28 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_WaitStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
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

