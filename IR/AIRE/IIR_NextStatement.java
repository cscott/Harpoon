// IIR_NextStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_NextStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NextStatement.java,v 1.3 1998-10-11 00:32:23 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NextStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_NEXT_STATEMENT; }
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
    IIR_SequentialStatement _enclosing_loop;
    IIR _condition;
} // END class

