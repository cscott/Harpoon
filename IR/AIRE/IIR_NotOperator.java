// IIR_NotOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * The predefined <code>IIR_NotOperator</code> class represents the
 * logical NOT operator and its overloadings.
 *
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_NotOperator.java,v 1.3 1998-10-11 01:24:59 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_NotOperator extends IIR_MonadicOperator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_NOT_OPERATOR).
     * @return <code>IR_Kind.IR_NOT_OPERATOR</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_NOT_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_NotOperator() { }
    
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

