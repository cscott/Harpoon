// IIR_WhileLoopStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_WhileLoopStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_WhileLoopStatement.java,v 1.2 1998-10-10 09:21:39 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_WhileLoopStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_WHILE_LOOP_STATEMENT
    //CONSTRUCTOR:
    public IIR_WhileLoopStatement() { }
    //METHODS:  
    public void set_while_condition(IIR while_condition)
    { _while_condition = while_condition; }
 
    public IIR get_while_condition()
    { return _while_condition; }
 
    //MEMBERS:  
    IIR_SequentialStatementList sequence_of_statements;
    IIR_DeclarationList loop_declarations;

// PROTECTED:
    IIR _while_condition;
} // END class

