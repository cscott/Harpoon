// IIR_GreaterThanOrEqualOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_GreaterThanOrEqualOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_GreaterThanOrEqualOperator.java,v 1.1 1998-10-10 07:53:36 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_GreaterThanOrEqualOperator extends IIR_DyadicOperator
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_GREATER_THAN_OR_EQUAL_OPERATOR
    //CONSTRUCTOR:
    public IIR_GreaterThanOrEqualOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

