// IIR_ConcurrentBreakStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentBreakStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentBreakStatement.java,v 1.3 1998-10-11 01:24:54 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentBreakStatement extends IIR_SimultaneousStatement
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_CONCURRENT_BREAK_STATEMENT).
     * @return <code>IR_Kind.IR_CONCURRENT_BREAK_STATEMENT</code>
     */
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

