// IIR_CaseStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_CaseStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_CaseStatement.java,v 1.2 1998-10-10 11:05:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_CaseStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CASE_STATEMENT
    //CONSTRUCTOR:
    public IIR_CaseStatement() { }
    //METHODS:  
    public void set_expression(IIR expression)
    { _expression = expression; }
 
    public IIR get_expression()
    { return _expression; }
 
    //MEMBERS:  
    public IIR_CaseStatementAlternativeList case_statement_alternatives;

// PROTECTED:
    IIR _expression;
} // END class

