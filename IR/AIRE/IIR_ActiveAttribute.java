// IIR_ActiveAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_ActiveAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_ActiveAttribute.java,v 1.1 1998-10-10 07:53:31 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_ActiveAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ACTIVE_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_ActiveAttribute() { }
    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

