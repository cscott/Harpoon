// IIR_ConcurrentBreakStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentBreakStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentBreakStatement.java,v 1.2 1998-10-11 00:32:17 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentBreakStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONCURRENT_BREAK_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ConcurrentBreakStatement() { }
    //METHODS:  
    public void set_condition(IIR condition)
    { _condition = condition; }
 
    public IIR get_condition()
    { return _condition; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _condition;
} // END class

