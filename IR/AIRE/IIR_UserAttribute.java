// IIR_UserAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_UserAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_UserAttribute.java,v 1.1 1998-10-10 07:53:46 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_UserAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_USER_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_UserAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

