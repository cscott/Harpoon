// IIR_MultiplicationOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_MultiplicationOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_MultiplicationOperator.java,v 1.2 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_MultiplicationOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_MULTIPLICATION_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_MultiplicationOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

