// IIR_UserAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_UserAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_UserAttribute.java,v 1.2 1998-10-11 00:32:28 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_UserAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    public IR_Kind get_kind()
    { return IR_Kind.IR_USER_ATTRIBUTE; }
    //CONSTRUCTOR:
    public IIR_UserAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

