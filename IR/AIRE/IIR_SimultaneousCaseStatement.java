// IIR_SimultaneousCaseStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_SimultaneousCaseStatement</code> class uses
 * an expression of discrete type to select a unique simulataneous
 * statement part for execution.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousCaseStatement.java,v 1.1 1998-10-10 07:53:43 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousCaseStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SIMULTANEOUS_CASE_STATEMENT
    //CONSTRUCTOR:
    public IIR_SimultaneousCaseStatement() { }
    //METHODS:  
    public void set_expression(IIR expression)
    { _expression = expression; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  
    public IIR_SimultaneousAlternativeList simultaneous_alternative_list;

// PROTECTED:
    IIR _expression;
} // END class

