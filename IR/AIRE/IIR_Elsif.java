// IIR_Elsif.java, created by cananian
package harpoon.IR.AIRE;

/**
 * A predefined <code>IIR_Elsif</code> class represents one step within
 * a recursive if-then-else statement.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_Elsif.java,v 1.2 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_Elsif extends IIR_Tuple
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_ELSIF; }
    //CONSTRUCTOR:
    public IIR_Elsif() { }
    //METHODS:  
    public void set_condition(IIR condition)
    { _condition = condition; }
 
    public IIR get_condition()
    { return _condition; }
 
    public void set_else_clause(IIR_Elsif else_clause)
    { _else_clause = else_clause; }
 
    public IIR_Elsif get_else_clause()
    { return _else_clause; }
 
    //MEMBERS:  
    public IIR_SequentialStatementList then_sequence_of_statements;

// PROTECTED:
    IIR _condition;
    IIR_Elsif _else_clause;
} // END class

