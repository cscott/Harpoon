// IIR_RightAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_RightAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_RightAttribute.java,v 1.1 1998-10-10 07:53:42 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_RightAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_RIGHT_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_RightAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

