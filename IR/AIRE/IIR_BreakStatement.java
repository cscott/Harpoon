// IIR_BreakStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_BreakStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BreakStatement.java,v 1.2 1998-10-10 11:05:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BreakStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_BREAK_STATEMENT
    //CONSTRUCTOR:
    public IIR_BreakStatement() { }
    //METHODS:  
    public void set_condition(IIR condition)
    { _condition = condition; }
 
    public IIR get_condition()
    { return _condition; }
 
    //MEMBERS:  
    public IIR_BreakList break_list;

// PROTECTED:
    IIR _condition;
} // END class

