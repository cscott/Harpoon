// IIR_LessThanOrEqualOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LessThanOrEqualOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LessThanOrEqualOperator.java,v 1.3 1998-10-11 01:24:58 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LessThanOrEqualOperator extends IIR_DyadicOperator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_LESS_THAN_OR_EQUAL_OPERATOR).
     * @return <code>IR_Kind.IR_LESS_THAN_OR_EQUAL_OPERATOR</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_LESS_THAN_OR_EQUAL_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_LessThanOrEqualOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

