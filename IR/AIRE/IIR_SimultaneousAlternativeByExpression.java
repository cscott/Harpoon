// IIR_SimultaneousAlternativeByExpression.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousAlternativeByExpression</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousAlternativeByExpression.java,v 1.2 1998-10-11 00:32:25 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousAlternativeByExpression extends IIR_SimultaneousAlternative
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_SIMULTANEOUS_ALTERNATIVE_BY_EXPRESSION; }
    //CONSTRUCTOR:
    public IIR_SimultaneousAlternativeByExpression() { }
    //METHODS:  
    public void set_choice(IIR choice)
    { _choice = choice; }
 
    public IIR get_choice()
    { return _choice; }
 
    //MEMBERS:  

// PROTECTED:
    IIR _choice;
} // END class

