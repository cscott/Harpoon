// IIR_DelayedAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_DelayedAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_DelayedAttribute.java,v 1.1 1998-10-10 07:53:35 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_DelayedAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_DELAYED_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_DelayedAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

