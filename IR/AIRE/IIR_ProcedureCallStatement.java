// IIR_ProcedureCallStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ProcedureCallStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ProcedureCallStatement.java,v 1.2 1998-10-10 11:05:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ProcedureCallStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_PROCEDURE_CALL_STATEMENT
    //CONSTRUCTOR:
    public IIR_ProcedureCallStatement() { }
    //METHODS:  
    public void set_procedure_name(IIR procedure_name)
    { _procedure_name = procedure_name; }
 
    public IIR get_procedure_name()
    { return _procedure_name; }
 
    //MEMBERS:  
    public IIR_AssociationList actual_parameter_part;

// PROTECTED:
    IIR _procedure_name;
} // END class

