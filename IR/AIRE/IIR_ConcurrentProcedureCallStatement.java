// IIR_ConcurrentProcedureCallStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentProcedureCallStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentProcedureCallStatement.java,v 1.1 1998-10-10 07:53:34 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentProcedureCallStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONCURRENT_PROCEDURE_CALL_STATEMENT
    //CONSTRUCTOR:
    public IIR_ConcurrentProcedureCallStatement() { }
    //METHODS:  
    public void set_postponed(boolean postponed)
    { _postponed = postponed; }
 
    public boolean get_postponed()
    { return _postponed; }
 
    public void set_procedure_name(IIR procedure_name)
    { _procedure_name = procedure_name; }
 
    //MEMBERS:  
    IIR_AssociationList actual_parameter_part;
    IIR_SequentialStatementList process_statement_part;

// PROTECTED:
    boolean _postponed;
    IIR _procedure_name;
} // END class

