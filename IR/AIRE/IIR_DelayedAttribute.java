// IIR_DelayedAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DelayedAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DelayedAttribute.java,v 1.2 1998-10-11 00:32:18 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DelayedAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_DELAYED_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_DelayedAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

