// IIR_SRAOperator.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SRAOperator</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SRAOperator.java,v 1.3 1998-10-11 01:25:01 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SRAOperator extends IIR_DyadicOperator
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SRA_OPERATOR).
     * @return <code>IR_Kind.IR_SRA_OPERATOR</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SRA_OPERATOR; }
    //CONSTRUCTOR:
    public IIR_SRAOperator() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

