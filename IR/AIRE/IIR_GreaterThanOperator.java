// IIR_GreaterThanOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_GreaterThanOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_GreaterThanOperator.java,v 1.3 1998-10-11 01:24:57 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_GreaterThanOperator extends IIR_DyadicOperator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_GREATER_THAN_OPERATOR).
     * @return <code>IR_Kind.IR_GREATER_THAN_OPERATOR</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_GREATER_THAN_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_GreaterThanOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

