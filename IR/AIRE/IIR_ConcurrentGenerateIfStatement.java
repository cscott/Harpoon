// IIR_ConcurrentGenerateIfStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_ConcurrentGenerateIfStatement</code> represents
 * a block which is either elaborated once or not at all, depending on the
 * value of a boolean condition.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentGenerateIfStatement.java,v 1.2 1998-10-10 09:21:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentGenerateIfStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONCURRENT_GENERATE_IF_STATEMENT
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

