// IIR_SimultaneousAlternativeByExpression.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SimultaneousAlternativeByExpression</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SimultaneousAlternativeByExpression.java,v 1.1 1998-10-10 07:53:43 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SimultaneousAlternativeByExpression extends IIR_SimultaneousAlternative
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_SIMULTANEOUS_ALTERNATIVE_BY_EXPRESSION
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

