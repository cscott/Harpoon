// IIR_SliceName.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_SliceName</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_SliceName.java,v 1.3 1998-10-11 01:25:03 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_SliceName extends IIR_Name
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_SLICE_NAME).
     * @return <code>IR_Kind.IR_SLICE_NAME</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_SLICE_NAME; }
    //CONSTRUCTOR:
    public IIR_SliceName() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

