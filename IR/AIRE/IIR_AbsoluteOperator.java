// IIR_AbsoluteOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AbsoluteOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AbsoluteOperator.java,v 1.3 1998-10-11 01:24:52 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AbsoluteOperator extends IIR_MonadicOperator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_ABSOLUTE_OPERATOR).
     * @return <code>IR_Kind.IR_ABSOLUTE_OPERATOR</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_ABSOLUTE_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_AbsoluteOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

