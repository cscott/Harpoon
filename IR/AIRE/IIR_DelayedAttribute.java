// IIR_DelayedAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DelayedAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DelayedAttribute.java,v 1.3 1998-10-11 01:24:55 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DelayedAttribute extends IIR_Attribute
{

// PUBLIC:
    /** Accept a visitor class. */
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    /**
     * Returns the <code>IR_Kind</code> of this class (IR_DELAYED_ATTRIBUTE).
     * @return <code>IR_Kind.IR_DELAYED_ATTRIBUTE</code>
     */
    public IR_Kind get_kind()
    { return IR_Kind.IR_DELAYED_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_DelayedAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

