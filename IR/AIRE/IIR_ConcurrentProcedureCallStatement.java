// IIR_ConcurrentProcedureCallStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentProcedureCallStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentProcedureCallStatement.java,v 1.3 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentProcedureCallStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONCURRENT_PROCEDURE_CALL_STATEMENT; }
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
    public IIR_AssociationList actual_parameter_part;
    public IIR_SequentialStatementList process_statement_part;

// PROTECTED:
    boolean _postponed;
    IIR _procedure_name;
} // END class

