// IIR_LowAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LowAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LowAttribute.java,v 1.2 1998-10-11 00:32:22 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LowAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_LOW_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_LowAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

