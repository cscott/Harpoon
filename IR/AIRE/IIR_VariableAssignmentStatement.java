// IIR_VariableAssignmentStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The <code>IIR_VariableAssignmentStatement</code> updates the value
 * of a variable with the value specified in an expression.  Such
 * statements may appear anywhere a sequential statement may appear.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_VariableAssignmentStatement.java,v 1.2 1998-10-10 09:21:39 cananian Exp $
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
 
    public void set_expression(IIR expression)
    { _expression = expression; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _target;
    IIR _expression;
} // END class

