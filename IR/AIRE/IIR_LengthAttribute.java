// IIR_LengthAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LengthAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LengthAttribute.java,v 1.3 1998-10-11 01:24:58 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LengthAttribute extends IIR_Attribute
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_LENGTH_ATTRIBUTE).
     * @return <code>IR_Kind.IR_LENGTH_ATTRIBUTE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_LENGTH_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_LengthAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

