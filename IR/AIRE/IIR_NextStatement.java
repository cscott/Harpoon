// IIR_NextStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NextStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NextStatement.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NextStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_NEXT_STATEMENT
    //CONSTRUCTOR:
    public IIR_NextStatement() { }
    //METHODS:  
    public void set_enclosing_loop(IIR_SequentialStatement loop)
    { _enclosing_loop = loop; }
 
    public IIR_SequentialStatement get_enclosing_loop()
    { return _enclosing_loop; }
 
    public void set_condition(IIR condition)
    { _condition = condition; }
 
    public IIR get_condition()
    { return _condition; }
 
    //MEMBERS:  

// PROTECTED:
    IIR_SequentialStatement _loop;
    IIR _condition;
} // END class

