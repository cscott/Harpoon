// IIR_BreakStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_BreakStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_BreakStatement.java,v 1.3 1998-10-11 00:32:16 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_BreakStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_BREAK_STATEMENT; }
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

