// IIR_InequalityOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_InequalityOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_InequalityOperator.java,v 1.3 1998-10-11 01:24:58 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_InequalityOperator extends IIR_DyadicOperator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_INEQUALITY_OPERATOR).
     * @return <code>IR_Kind.IR_INEQUALITY_OPERATOR</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_INEQUALITY_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_InequalityOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

