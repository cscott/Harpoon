// IIR_VariableAssignmentStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_VariableAssignmentStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_VariableAssignmentStatement.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_VariableAssignmentStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_VARIABLE_ASSIGNMENT_STATEMENT
    //CONSTRUCTOR:
    public IIR_VariableAssignmentStatement() { }
    //METHODS:  
    public void set_target(IIR target)
    { _target = target; }
 
    public IIR get_target()
    { return _target; }
 
    public void set_expression(IIR target)
    { _expression = target; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _target;
    IIR _target;
} // END class

