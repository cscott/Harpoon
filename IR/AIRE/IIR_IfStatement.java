// IIR_IfStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_IfStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_IfStatement.java,v 1.1 1998-10-10 07:53:37 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_IfStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_IF_STATEMENT
    //CONSTRUCTOR:
    public IIR_IfStatement() { }
    //METHODS:  
    public void set_elsif(IIR_Elsif condition)
    { _elsif = condition; }
 
    public IIR_Elsif get_elsif()
    { return _elsif; }
 
    //MEMBERS:  
    IIR_SequentialStatementList then_sequence;
    IIR_SequentialStatementList else_sequence;

// PROTECTED:
    IIR_Elsif _condition;
} // END class

