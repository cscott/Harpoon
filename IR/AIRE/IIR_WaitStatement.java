// IIR_WaitStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_WaitStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_WaitStatement.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_WaitStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_WAIT_STATEMENT
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
    IIR_SignalNameList sensitivity_list;

// PROTECTED:
    IIR _condition_clause;
    IIR _timeout_clause;
} // END class

