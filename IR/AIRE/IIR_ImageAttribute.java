// IIR_ImageAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ImageAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ImageAttribute.java,v 1.2 1998-10-11 00:32:20 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ImageAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_IMAGE_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_ImageAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

