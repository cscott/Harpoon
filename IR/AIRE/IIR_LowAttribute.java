// IIR_LowAttribute.java, created by cananian
package harpoon.IR.AIRE;

/**
 * <code>IIR_LowAttribute</code> 
 * @author C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: IIR_LowAttribute.java,v 1.1 1998-10-10 07:53:38 cananian Exp $
 */

//-----------------------------------------------------------
public class IIR_LowAttribute extends IIR_Attribute
{

// PUBLIC:
    public void accept(IIR_Visitor visitor ){visitor.visit(this);}
    //IR_KIND = IR_LOW_ATTRIBUTE
    //CONSTRUCTOR:
    public IIR_LowAttribute() { }

    //METHODS:  
    //MEMBERS:  

// PROTECTED:
} // END class

