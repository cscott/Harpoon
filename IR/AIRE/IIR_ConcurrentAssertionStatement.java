// IIR_ConcurrentAssertionStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ConcurrentAssertionStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ConcurrentAssertionStatement.java,v 1.1 1998-10-10 07:53:33 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ConcurrentAssertionStatement extends IIR_ConcurrentStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_CONCURRENT_ASSERTION_STATEMENT
    //CONSTRUCTOR:
    public IIR_ConcurrentAssertionStatement() { }
    //METHODS:  
    public void set_postponed(boolean predicate)
    { _postponed = predicate; }
 
    public boolean get_postponed()
    { return _postponed; }
 
    public void set_assertion_condition(IIR condition)
    { _assertion_condition = condition; }
 
    //MEMBERS:  

// PROTECTED:
    boolean _predicate;
    IIR _condition;
} // END class

