// IIR_ExitStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ExitStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ExitStatement.java,v 1.2 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ExitStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_EXIT_STATEMENT; }
    //CONSTRUCTOR:
    public IIR_ExitStatement() { }
    //METHODS:  
    public void set_enclosing_loop(IIR_SequentialStatement enclosing_loop)
    { _enclosing_loop = enclosing_loop; }
 
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

