// IIR_ExponentiationOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ExponentiationOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ExponentiationOperator.java,v 1.2 1998-10-11 00:32:19 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ExponentiationOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_EXPONENTIATION_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_ExponentiationOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

