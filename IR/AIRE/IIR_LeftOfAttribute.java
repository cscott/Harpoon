// IIR_LeftOfAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LeftOfAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LeftOfAttribute.java,v 1.2 1998-10-11 00:32:21 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LeftOfAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_LEFT_OF_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_LeftOfAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

