// IIR_ExitStatement.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ExitStatement</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ExitStatement.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ExitStatement extends IIR_SequentialStatement
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_EXIT_STATEMENT
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

