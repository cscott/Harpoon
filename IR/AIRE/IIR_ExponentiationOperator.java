// IIR_ExponentiationOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ExponentiationOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ExponentiationOperator.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ExponentiationOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_EXPONENTIATION_OPERATOR
    //CONSTRUCTOR:
    public IIR_ExponentiationOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

