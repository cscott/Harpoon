// IIR_ConcurrentGenerateIfStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ConcurrentGenerateIfStatement</code> represents
 * a block which is either elaborated once or not at all, depending on the
 * value of a boolean condition.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentGenerateIfStatement.java,v 1.3 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentGenerateIfStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_CONCURRENT_GENERATE_IF_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ConcurrentGenerateIfStatement() { }
    
    //METHODS:  
    public void set_if_condition(IIR condition)
    { _if_condition = condition; }
 
    public IIR get_if_condition()
    { return _if_condition; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _if_condition;
} // END class

