// IIR_AboveAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_AboveAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_AboveAttribute.java,v 1.1 1998-10-10 07:53:31 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_AboveAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_ABOVE_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_AboveAttribute() {}

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

